package org.jactr.eclipse.runtime.session.impl;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.jactr.core.runtime.controller.debug.BreakpointType;
import org.jactr.eclipse.runtime.debug.ACTRDebugTarget;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.control.DefaultRunForContentProvider;
import org.jactr.eclipse.runtime.session.control.ISessionController2;
import org.jactr.tools.async.shadow.ShadowController;

public class ACTRControllerWrapper extends ControllerWrapper implements
    ISessionController2
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER  = LogFactory
                                                 .getLog(ACTRControllerWrapper.class);

  private ACTRSession                _actrSession;

  private ITreeContentProvider       _runFor = new DefaultRunForContentProvider();

  private ITreeContentProvider       _runTo;

  public ACTRControllerWrapper(ACTRSession oldSession, ISession session)
  {
    super(oldSession.getLaunch(), session);
    _actrSession = oldSession;
  }

  public ITreeContentProvider getRunToContentProvider()
  {
    return _runTo;
  }

  public ITreeContentProvider getRunForContentProvider()
  {
    return _runFor;
  }

  protected ShadowController getShadowController()
  {
    return _actrSession.getShadowController();
  }

  @Override
  protected ACTRDebugTarget getDebugTarget()
  {
    for (IDebugTarget target : _actrSession.getLaunch().getDebugTargets())
      if (target instanceof ACTRDebugTarget) return (ACTRDebugTarget) target;
    return null;
  }

  @Override
  public boolean isRunning()
  {
    return getShadowController().isRunning();
  }

  @Override
  public boolean isTerminated()
  {
    return !isRunning();
  }

  @Override
  public boolean canTerminate()
  {
    return isRunning();
  }

  @Override
  public boolean terminate() throws Exception
  {
    _actrSession.stop();
    return true;
  }

  @Override
  public boolean isSuspended()
  {
    return getShadowController().isSuspended();
  }

  @Override
  public boolean canSuspend()
  {
    IDebugTarget target = getDebugTarget();
    return target != null && isRunning() && target.canSuspend();
  }

  @Override
  public boolean suspend() throws Exception
  {
    getDebugTarget().suspend();
    return true;
  }

  @Override
  public boolean canResume()
  {
    IDebugTarget target = getDebugTarget();
    return target != null && isSuspended() && target.canResume();
  }

  @Override
  public boolean resume() throws Exception
  {
    IDebugTarget target = getDebugTarget();
    target.resume();
    return true;
  }

  @Override
  public boolean canStep()
  {
    IDebugTarget target = getDebugTarget();
    return target != null && target.isSuspended() && target.canResume();
  }

  @Override
  public boolean step() throws Exception
  {
    IDebugTarget target = getDebugTarget();
    target.resume();
    target.suspend();
    return true;
  }

  @Override
  public double getCurrentTime()
  {
    return _actrSession.getShadowController().getCurrentSimulationTime();
  }

  public boolean canRunTo(Object destination)
  {
    if (destination instanceof Number)
      return canRunToInternal(((Number) destination).doubleValue());
    return false;
  }

  public void runTo(Object destination) throws Exception
  {
    double targetTime = getCurrentTime();
    if (destination instanceof Number)
      targetTime = ((Number) destination).doubleValue();


    if (targetTime <= getCurrentTime())
      throw new IllegalArgumentException(String.format(
          "Target time %.2f has already passed %.2f", targetTime,
          getCurrentTime()));

    /*
     * set the break point and resume
     */
    _actrSession.getShadowController().addBreakpoint(BreakpointType.TIME,
        "all", String.format("%.3f", targetTime));
    resume();

  }

  public boolean canRunFor(Object duration)
  {
    if (duration instanceof Number)
    {
      double targetDuration = ((Number) duration).doubleValue();
      return targetDuration > getCurrentTime() && canResume();
    }
    return false;
  }

  public void runFor(Object duration) throws Exception
  {
    if (duration instanceof Number)
    {
      double targetDuration = ((Number) duration).doubleValue();
      double targetTime = targetDuration + getCurrentTime();

      /*
       * set the break point and run
       */
      _actrSession.getShadowController().addBreakpoint(BreakpointType.TIME,
          "all", String.format("%.3f", targetTime));
      resume();
    }
    else
      throw new IllegalArgumentException(String.format(
          "No clue how to run for %s", duration));
  }

  protected boolean canRunToInternal(double time)
  {
    return canResume() && time > getCurrentTime();
  }


}
