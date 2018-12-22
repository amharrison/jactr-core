package org.commonreality.sensors.motor.interpolator;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.motor.MovementCommand;
import org.commonreality.time.impl.BasicClock;

public class InterpolatorEvent
{
  /**
   * Logger definition
   */
  static private final transient Log                     LOGGER          = LogFactory
                                                                             .getLog(InterpolatorEvent.class);

  private MovementCommand                                _command;

  private double                                         _lastUpdateTime = 0;

  private double                                         _startTime;

  private double                                         _endTime;

  private boolean                                        _shouldAbort    = false;

  private boolean                                        _hasAborted     = false;

  private boolean                                        _hasStarted     = false;

  private boolean                                        _hasCompleted   = false;


  public InterpolatorEvent(MovementCommand command,
      double startTime, double endTime)
  {
    _command = command;
    _startTime = BasicClock.constrainPrecision(startTime);
    _endTime = BasicClock.constrainPrecision(endTime);
    _lastUpdateTime = _startTime;
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(command + " runs from " + _startTime + " to " + _endTime);
  }

  public double getStartTime()
  {
    return _startTime;
  }

  public double getEndTime()
  {
    return _endTime;
  }

  public MovementCommand getCommand()
  {
    return _command;
  }

  /**
   * returns a collection of delta trackers for all the efferents that have been
   * updated
   * 
   * @param currentTime
   * @return
   */
  final public void update(double currentTime)
  {
    if (_lastUpdateTime < currentTime)
    {
      /*
       * has it been started yet?
       */
      if (currentTime >= _startTime && !_hasStarted)
        start(currentTime);
      
      if (currentTime >= _endTime && !_hasCompleted)
        complete(currentTime);
      
      if (shouldAbort()) abort(currentTime);

      updateInternal(currentTime);
      
      _lastUpdateTime = currentTime;
    }
  }
  
  public double getLastUpdateTime()
  {
    return _lastUpdateTime;
  }

 
  protected void updateInternal(double currentTime)
  {

  }

  protected boolean shouldAbort()
  {
    return _shouldAbort;
  }

  /**
   * flag this as needing to be aborted
   */
  final public void abort()
  {
    _shouldAbort = true;
  }

  public boolean hasAborted()
  {
    return _hasAborted;
  }

  final protected void abort(double currentTime)
  {
    _hasAborted = true;
    abortInternal(currentTime);
  }

  protected void abortInternal(double currentTime)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Aborting " + _command);
  }

  final protected void start(double currentTime)
  {
    startInternal(currentTime);
    _hasStarted = true;
  }

  public boolean hasStarted()
  {
    return _hasStarted;
  }

  protected void startInternal(double currentTime)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Starting " + _command);
  }

  public boolean hasCompleted()
  {
    return _hasCompleted;
  }

  final protected void complete(double currentTime)
  {
    _hasCompleted = true;
    completeInternal(currentTime);
  }

  protected void completeInternal(double currentTime)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Completed " + _command);
  }
}
