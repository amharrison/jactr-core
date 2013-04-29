package org.jactr.eclipse.execution.internal;

/*
 * default logging
 */
import java.util.Date;
import java.util.concurrent.Executor;

import org.jactr.eclipse.execution.IExecutionControl;
import org.jactr.eclipse.execution.IExecutionSession;
import org.jactr.eclipse.execution.IExecutionSessionListener;

public abstract class AbstractExecutionSession implements IExecutionSession
{


  private final GeneralEventManager<IExecutionSessionListener, Object> _eventManager;

  private IExecutionControl                                             _controller;

  private Date                                                          _endTime;

  private float                                                         _progress = -1f;

  private Date                                                          _startTime;

  private State                                                         _state;

  private String                                                        _details;

  public AbstractExecutionSession()
  {
    _eventManager = new GeneralEventManager<IExecutionSessionListener, Object>(
        new GeneralEventManager.INotifier<IExecutionSessionListener, Object>() {

          public void notify(IExecutionSessionListener listener,
 Object signal)
          {
            if (Boolean.TRUE.equals(signal))
              listener.stateHasChanged(AbstractExecutionSession.this);
            else if (Boolean.FALSE.equals(signal))
              listener.detailsHaveChanged(AbstractExecutionSession.this);
            else
              listener.notificationReceived(AbstractExecutionSession.this,
                  signal);
          }
        });
  }

  public void addListener(IExecutionSessionListener listener, Executor executor)
  {
    _eventManager.addListener(listener, executor);
  }

  public IExecutionControl getControl()
  {
    return _controller;
  }

  protected void setControl(IExecutionControl control)
  {
    _controller = control;
  }

  public void removeListener(IExecutionSessionListener listener)
  {
    _eventManager.removeListener(listener);
  }

  protected void signalStateChange()
  {
    if (_eventManager.hasListeners()) _eventManager.notify(Boolean.TRUE);
  }

  protected void signalNotification(Object message)
  {
    if (_eventManager.hasListeners()) _eventManager.notify(message);
  }

  public Date getEndTime()
  {
    return _endTime;
  }

  protected void setEndTime(Date endTime)
  {
    _endTime = endTime;
  }

  public float getProgress()
  {
    return _progress;
  }

  protected void setProgress(float progress)
  {
    _progress = progress;
  }

  public Date getStartTime()
  {
    return _startTime;
  }

  protected void setStartTime(Date start)
  {
    _startTime = start;
  }

  public State getState()
  {
    return _state;
  }

  protected void setState(State state)
  {
    _state = state;
  }

  public String getStateDetails()
  {
    return _details;
  }

  protected void setStateDetails(String details)
  {
    _details = details;
  }

  public boolean isActive()
  {
    return State.COMPLETED != _state;
  }

  protected void started()
  {
    setState(State.RUNNING);
    setStartTime(new Date());
    signalStateChange();
  }

  protected void queued()
  {
    setState(State.QUEUED);
    setStartTime(new Date());
    signalStateChange();
  }

  protected void suspended()
  {
    setState(State.SUSPENDED);
    signalStateChange();
  }

  protected void resumed()
  {
    setState(State.RUNNING);
    signalStateChange();
  }

  protected void completed()
  {
    setState(State.COMPLETED);
    setEndTime(new Date());
    signalStateChange();
  }

}
