package org.jactr.eclipse.runtime.session.impl;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.control.ISessionController;

public class ControllerWrapper implements ISessionController
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ControllerWrapper.class);

  private final ILaunch              _launch;

  private final ISession             _session;

  public ControllerWrapper(ILaunch launch, ISession session)
  {
    _launch = launch;
    _session = session;
  }

  public ISession getSession()
  {
    return _session;
  }

  public boolean isRunning()
  {
    return !isTerminated();
  }

  public boolean isTerminated()
  {
    return _launch.isTerminated();
  }

  public boolean canTerminate()
  {
    return isRunning();
  }

  public boolean terminate() throws Exception
  {
    try
    {
      _launch.terminate();
      return true;
    }
    catch (Exception e)
    {
      LOGGER.error("could not terminate", e);
      return false;
    }
  }

  protected IDebugTarget getDebugTarget()
  {
    return _launch.getDebugTarget();
  }

  protected boolean hasDebugTarget()
  {
    return getDebugTarget() != null;
  }

  public boolean isSuspended()
  {
    if (!hasDebugTarget()) return false;
    return getDebugTarget().isSuspended();
  }

  public boolean canSuspend()
  {
    return hasDebugTarget() && getDebugTarget().canSuspend();
  }

  public boolean suspend() throws Exception
  {
    try
    {
      getDebugTarget().suspend();
      return true;
    }
    catch (Exception e)
    {
      LOGGER.error("Could not suspend", e);
      return false;
    }
  }

  public boolean canResume()
  {
    return hasDebugTarget() && getDebugTarget().canResume();
  }

  public boolean resume() throws Exception
  {
    try
    {
      getDebugTarget().resume();
      return true;
    }
    catch (Exception e)
    {
      LOGGER.error("Could not resume", e);
      return false;
    }
  }

  public boolean canStep()
  {
    return hasDebugTarget() && getDebugTarget().canResume();
  }

  public boolean step() throws Exception
  {
    try
    {
      getDebugTarget().resume();
      getDebugTarget().suspend();
      return true;
    }
    catch (Exception e)
    {
      LOGGER.error("Could not step", e);
      return false;
    }
  }

  public double getCurrentTime()
  {
    return 0;
  }

}
