package org.jactr.eclipse.execution.internal;

/*
 * default logging
 */
import java.util.Date;
import java.util.concurrent.Executor;

import org.jactr.eclipse.execution.IExecutionControl;
import org.jactr.eclipse.execution.IExecutionSession;
import org.jactr.eclipse.execution.IExecutionSessionListener;

public class DecoratingExecutionSession implements IExecutionSession
{
  private IExecutionSession _session;

  public DecoratingExecutionSession(IExecutionSession actual)
  {
    set(actual);
  }

  protected void set(IExecutionSession actual)
  {
    _session = actual;
  }

  public void addListener(IExecutionSessionListener listener, Executor executor)
  {
    _session.addListener(listener, executor);
  }

  public IExecutionControl getControl()
  {
    return _session.getControl();
  }

  public Date getEndTime()
  {
    return _session.getEndTime();
  }

  public float getProgress()
  {
    return _session.getProgress();
  }

  public Date getStartTime()
  {
    return _session.getStartTime();
  }

  public State getState()
  {
    return _session.getState();
  }

  public String getStateDetails()
  {
    return _session.getStateDetails();
  }

  public boolean isActive()
  {
    return _session.isActive();
  }

  public void removeListener(IExecutionSessionListener listener)
  {
    _session.removeListener(listener);
  }

  public Object getAdapter(Class adapter)
  {
    return _session.getAdapter(adapter);
  }

}
