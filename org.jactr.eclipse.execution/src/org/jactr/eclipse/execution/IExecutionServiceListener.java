package org.jactr.eclipse.execution;

/*
 * default logging
 */

public interface IExecutionServiceListener
{

  public void sessionCreated(IExecutionSession session);

  public void sessionStateChanged(IExecutionSession session);

  public void sessionDestroyed(IExecutionSession session);
}
