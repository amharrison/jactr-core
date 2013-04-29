package org.jactr.eclipse.execution;

import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.core.model.ITerminate;

/*
 * default logging
 */

public interface IExecutionControl extends ITerminate, ISuspendResume
{

  public boolean isRunning();

  /**
   * @return
   */
  public boolean canCancel();

  public void cancel() throws Exception;

  /**
   * possibly provides a stream to get messages off of
   * 
   * @return
   */
  public IRuntimeTrace getRuntimeTrace();
}
