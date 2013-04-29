package org.jactr.eclipse.runtime.session.control;

import org.jactr.eclipse.runtime.session.ISession;

/*
 * default logging
 */

/**
 * abstract interface to the control of a session. Not all sessions will support
 * this.
 * 
 * @author harrison
 */
public interface ISessionController
{

  public boolean isRunning();

  public boolean isTerminated();

  public boolean canTerminate();

  public boolean terminate() throws Exception;

  public boolean isSuspended();

  public boolean canSuspend();

  public boolean suspend() throws Exception;

  public boolean canResume();

  public boolean resume() throws Exception;

  public boolean canStep();

  public boolean step() throws Exception;

  public ISession getSession();

  public double getCurrentTime();
}
