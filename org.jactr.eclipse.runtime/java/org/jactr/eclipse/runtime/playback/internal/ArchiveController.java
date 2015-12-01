package org.jactr.eclipse.runtime.playback.internal;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.debug.elements.ACTRDebugElement;
import org.jactr.eclipse.runtime.marker.MarkerIndex.MarkerRecord;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.control.DefaultRunForContentProvider;
import org.jactr.eclipse.runtime.session.control.ISessionController2;

public class ArchiveController implements ISessionController2
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER        = LogFactory
                                                       .getLog(ArchiveController.class);

  private final ArchivalIndex        _index;

  private final EventPumper          _pumper;

  private double                     _currentTime;

  private boolean                    _terminated   = false;

  private final ISession             _session;

  private ITreeContentProvider       _runForContent;

  private ITreeContentProvider       _runToContent;

  private boolean                    _canSkipAhead = true;

  private double                     _currentStepSize;

  public ArchiveController(ISession session, ArchivalIndex index,
      EventPumper pumper)
  {
    _index = index;
    _session = session;
    _pumper = pumper;
    _currentTime = _index.getStartTime();
    _currentStepSize = RuntimePlugin.getDefault().getPreferenceStore()
        .getInt(RuntimePreferences.RUNTIME_DATA_WINDOW);
    // currently the number of rows, assuming 50ms per row, would be /20
    // let's give ourselves a little slop
    _currentStepSize = _currentStepSize / 30;
    setRunForContentProvider(new DefaultRunForContentProvider());
  }

  public void setRunForContentProvider(ITreeContentProvider provider)
  {
    _runForContent = provider;
  }

  public void setRunToContentProvider(ITreeContentProvider provider)
  {
    _runToContent = provider;
  }

  public double getCurrentTime()
  {
    return _currentTime;
  }

  public boolean isRunning()
  {
    return !isTerminated();
  }

  public boolean isTerminated()
  {
    return _terminated;
  }

  public boolean canTerminate()
  {
    return !isTerminated() && _currentTime >= _index.getStartTime()
        && _currentTime < _index.getEndTime();
  }

  public boolean terminate() throws Exception
  {
    if (!canTerminate()) return true;

    _currentTime = Double.MAX_VALUE;
    _terminated = true;

    _index.close();

    ACTRDebugElement.fireTerminateEvent(_session);

    return true;
  }

  public boolean isSuspended()
  {
    boolean isFree = _pumper.isFree();
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("event pumper is free: %s", isFree));
    return _pumper.isFree() && isRunning();
  }

  public boolean canSuspend()
  {
    return false;
  }

  public boolean suspend() throws Exception
  {
    return false;
  }

  public boolean canResume()
  {
    return isSuspended();
  }

  public void runFully() throws Exception
  {
    if (!canResume()) throw new RuntimeException("Must be suspended first");

    double startTime = _currentTime;

    ACTRDebugElement.fireResumeEvent(_session, 0);
    _index.pump(startTime, _index.getEndTime(), _pumper);
  }

  public boolean resume() throws Exception
  {
    if (!canResume()) throw new RuntimeException("Must be suspended first");

    double windowSize = _currentStepSize;
    double startTime = _currentTime;
    double endTime = _currentTime + windowSize;

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Resuming @ %.2f for %.2f", startTime,
          windowSize));

    ACTRDebugElement.fireResumeEvent(_session, 0);
    _index.pump(startTime, endTime, _pumper);

    return true;
  }

  public boolean canStep()
  {
    return false;
  }

  public boolean step() throws Exception
  {
    return false;
  }

  protected void setCurrentTime(double currentTime)
  {
    _currentTime = currentTime;
  }

  public void setStepSize(double stepSize)
  {
    _currentStepSize = stepSize;
  }

  public boolean canRunTo(Object destination)
  {
    if (destination instanceof Number)
      return canRunToInternal(((Number) destination).doubleValue());
    if (destination instanceof MarkerRecord)
      return canRunToInternal((MarkerRecord) destination);
    return false;
  }

  public void runTo(Object destination) throws Exception
  {
    double targetTime = _currentTime;
    if (destination instanceof Number)
      targetTime = ((Number) destination).doubleValue();
    if (destination instanceof MarkerRecord)
      targetTime = ((MarkerRecord) destination)._time + 0.1;

    if (targetTime <= _currentTime)
      throw new IllegalArgumentException(String.format(
          "Target time %.2f has already passed %.2f", targetTime, _currentTime));

    double startTime = _currentTime;
    if (_canSkipAhead)
    {
      double fullWindow = RuntimePlugin.getDefault().getPreferenceStore()
          .getInt(RuntimePreferences.RUNTIME_DATA_WINDOW);

      if (targetTime > startTime + fullWindow)
        startTime = targetTime - fullWindow;
    }

    _index.pump(startTime, targetTime, _pumper);

    // ACTRDebugElement.fireResumeEvent(_session, 0);
  }

  public boolean canRunFor(Object duration)
  {
    if (duration instanceof Number)
    {
      double targetDuration = ((Number) duration).doubleValue();
      return targetDuration + _currentTime <= _index.getEndTime()
          && canResume();
    }
    return false;
  }

  public void runFor(Object duration) throws Exception
  {
    if (duration instanceof Number)
    {
      double targetDuration = ((Number) duration).doubleValue();
      double targetTime = targetDuration + _currentTime;
      if (targetTime > _index.getEndTime())
        throw new IllegalArgumentException(String.format(
            "Running for %.2f will exceed total run time (%.2f)",
            targetDuration, _index.getEndTime()));

      _index.pump(_currentTime, targetTime, _pumper);
      // ACTRDebugElement.fireResumeEvent(_session, 0);
    }
    else
      throw new IllegalArgumentException(String.format(
          "No clue how to run for %s", duration));
  }

  protected boolean canRunToInternal(double time)
  {
    return canResume() && time > _currentTime && time <= _index.getEndTime();
  }

  protected boolean canRunToInternal(MarkerRecord record)
  {
    return canRunToInternal(record._time + 1);
  }

  public ITreeContentProvider getRunToContentProvider()
  {
    return _runToContent;
  }

  public ITreeContentProvider getRunForContentProvider()
  {
    return _runForContent;
  }

  public ISession getSession()
  {
    return _session;
  }
}
