package org.jactr.eclipse.execution;

/*
 * default logging
 */

public interface IExecutionSessionListener
{
  public void stateHasChanged(IExecutionSession session);

  public void detailsHaveChanged(IExecutionSession session);

  public void notificationReceived(IExecutionSession session, Object message);
}
