package org.jactr.eclipse.execution;

import java.util.Date;
import java.util.concurrent.Executor;

import org.eclipse.core.runtime.IAdaptable;

/*
 * default logging
 */

public interface IExecutionSession extends IAdaptable
{
  static public enum State {
    QUEUED, RUNNING, SUSPENDED, COMPLETED
  };



  /**
   * return the object that controls the execution of the session. may be null
   * if no control is possible.
   * 
   * @return
   */
  public IExecutionControl getControl();

  public void addListener(IExecutionSessionListener listener, Executor executor);

  public void removeListener(IExecutionSessionListener listener);

  /**
   * not terminated or completed
   * 
   * @return
   */
  public boolean isActive();

  public State getState();

  /**
   * additional info
   * 
   * @return
   */
  public String getStateDetails();

  /**
   * percent complete, or -1 if unknown
   * 
   * @return
   */
  public float getProgress();

  /**
   * time of queueing until state is running, then start time.
   * 
   * @return
   */
  public Date getStartTime();

  /**
   * actual endtime after completion, or estimated end time, or null
   * 
   * @return
   */
  public Date getEndTime();

}
