package org.jactr.eclipse.execution.internal;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.jactr.eclipse.execution.IExecutionControl;
import org.jactr.eclipse.execution.IExecutionService;
import org.jactr.eclipse.execution.IExecutionServiceListener;
import org.jactr.eclipse.execution.IExecutionSession;
import org.jactr.eclipse.execution.IExecutionSessionListener;

public abstract class AbstractExecutionService implements IExecutionService
{
  static private enum EventType {
    CREATED, CHANGED, DESTROYED
  };

  private final GeneralEventManager<IExecutionServiceListener, SessionEvent> _eventManager;

  /**
   * relays session changes to the service listeners
   */
  private final IExecutionSessionListener                                    _changeRelayListener;

  private final Collection<IExecutionSession>                                _activeSessions;

  private final LinkedList<IExecutionSession>                                _queue;

  public AbstractExecutionService()
  {
    _queue = new LinkedList<IExecutionSession>();
    _activeSessions = new ArrayList<IExecutionSession>();
    _eventManager = new GeneralEventManager<IExecutionServiceListener, SessionEvent>(
        new GeneralEventManager.INotifier<IExecutionServiceListener, SessionEvent>() {

          public void notify(IExecutionServiceListener listener,
              SessionEvent event)
          {
            switch (event._type)
            {
              case CREATED:
                listener.sessionCreated(event._session);
                break;
              case CHANGED:
                listener.sessionStateChanged(event._session);
                break;
              case DESTROYED:
                listener.sessionDestroyed(event._session);
                break;
            }
          }

        });

    _changeRelayListener = new IExecutionSessionListener() {

      public void detailsHaveChanged(IExecutionSession session)
      {

      }

      public void stateHasChanged(IExecutionSession session)
      {
        signalStateChanged(session);
        if (session.getState() == IExecutionSession.State.COMPLETED)
          checkQueue();
      }

      public void notificationReceived(IExecutionSession session, Object message)
      {
        
        
      }
    };
  }

  public void addListener(IExecutionServiceListener listener, Executor executor)
  {
    _eventManager.addListener(listener, executor);
  }

  public void getSessions(Collection<IExecutionSession> container)
  {
    synchronized (_activeSessions)
    {
      container.addAll(_activeSessions);
    }
  }

  public void removeListener(IExecutionServiceListener listener)
  {
    _eventManager.removeListener(listener);
  }

  public boolean hasListeners()
  {
    return _eventManager.hasListeners();
  }

  /**
   * create the actual session object
   * 
   * @param parameters
   * @return
   */
  abstract protected IExecutionSession createSession(IProject project,
      ILaunchConfiguration configuration, Object... parameters);

  /**
   * start the processing of the session - either by executing or queuing for
   * later
   * 
   * @param session
   */
  abstract protected void startSessionProcessing(IExecutionSession session)
      throws Exception;

  /**
   * destroy session resources
   * 
   * @param session
   */
  abstract protected void destroySession(IExecutionSession session);

  abstract protected void validate(IProject project,
      ILaunchConfiguration launchConfiguration) throws Exception;

  abstract protected boolean shouldQueue(IExecutionSession session,
      List<IExecutionSession> queue);

  protected void checkQueue()
  {
    IExecutionSession session = null;
    synchronized (_queue)
    {
      if (_queue.size() != 0) session = _queue.remove(0);
    }

    if (session != null) try
    {
      startSession(session, true);
    }
    catch (Exception e)
    {
      // ?? should log it..
    }
  }

  protected void queueSession(IExecutionSession session)
  {
    synchronized (_queue)
    {
      _queue.addLast(session);
    }

    ((AbstractExecutionSession) session).queued();
  }

  protected void startSession(IExecutionSession session,
      boolean callStartProcessing) throws Exception
  {
    try
    {
      if (callStartProcessing) startSessionProcessing(session);

      // should probably lock
      synchronized (_activeSessions)
      {
        _activeSessions.add(session);
      }
    }
    catch (Exception e)
    {
      destroy(session);
      throw e;
    }
  }

  /**
   * submit the execution request
   */
  public IExecutionSession submit(IProject project,
      ILaunchConfiguration launchConfiguration, Object... parameters)
      throws Exception
  {
    validate(project, launchConfiguration);

    IExecutionSession session = createSession(project, launchConfiguration,
        parameters);
    session.addListener(_changeRelayListener, null);

    _eventManager.notify(new SessionEvent(session, EventType.CREATED));

    boolean delayProcessing = false;
    synchronized (_queue)
    {
      delayProcessing = shouldQueue(session, _queue);
    }

    if (!delayProcessing)
      startSession(session, true);
    else
      queueSession(session);

    return session;
  }

  public IExecutionSession adopt(IProject project,
      ILaunchConfiguration launchConfiguration, Object... parameters)
      throws Exception
  {
    validate(project, launchConfiguration);
    IExecutionSession session = createSession(project, launchConfiguration,
        parameters);
    session.addListener(_changeRelayListener, null);

    _eventManager.notify(new SessionEvent(session, EventType.CREATED));

    startSession(session, false);

    return session;
  }

  protected void destroy(IExecutionSession session) throws DebugException
  {
    try
    {
      /*
       * make sure it isn't running
       */
      IExecutionControl control = session.getControl();
      if (control != null && control.isRunning() && control.canTerminate())
        control.terminate();
    }
    finally
    {
      synchronized (_activeSessions)
      {
        _activeSessions.remove(session);
      }
      _eventManager.notify(new SessionEvent(session, EventType.DESTROYED));
      destroySession(session);
    }
  }

  protected void signalStateChanged(IExecutionSession session)
  {
    _eventManager.notify(new SessionEvent(session, EventType.CHANGED));
  }

  private class SessionEvent
  {
    final IExecutionSession _session;

    final EventType         _type;

    public SessionEvent(IExecutionSession session, EventType type)
    {
      _type = type;
      _session = session;
    }
  }
}
