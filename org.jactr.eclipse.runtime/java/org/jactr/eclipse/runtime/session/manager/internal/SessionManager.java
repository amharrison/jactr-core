package org.jactr.eclipse.runtime.session.manager.internal;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.concurrent.ExecutorServices;
import org.jactr.eclipse.runtime.launching.session.AbstractSession;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.ISessionListener;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.impl.Session2SessionAdapter;
import org.jactr.eclipse.runtime.session.manager.ISessionManager;
import org.jactr.eclipse.runtime.session.manager.ISessionManagerListener;
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;
import org.jactr.eclipse.runtime.trace.impl.GeneralEventManager;
import org.jactr.eclipse.runtime.trace.impl.GeneralEventManager.INotifier;

public class SessionManager implements ISessionManager
{
  /**
   * Logger definition
   */
  static private final transient Log                                   LOGGER = LogFactory
                                                                                  .getLog(SessionManager.class);

  private final Set<ISession>                                          _activeSessions;

  private final Map<UUID, ISession>                                    _sessionById;

  private final GeneralEventManager<ISessionManagerListener, Object[]> _eventManager;

  private final ISessionListener                                       _sessionListener;

  public SessionManager()
  {
    _activeSessions = new HashSet<ISession>();
    _sessionById = new HashMap<UUID, ISession>();
    _eventManager = new GeneralEventManager<ISessionManagerListener, Object[]>(
        new INotifier<ISessionManagerListener, Object[]>() {

          public void notify(ISessionManagerListener listener, Object[] event)
          {
            Boolean add = (Boolean) event[0];
            ISession session = (ISession) event[1];
            if (add)
              listener.sessionAdded(session);
            else
              listener.sessionRemoved(session);
          }
        });
    _sessionListener = new ISessionListener() {

      public void sessionDestroyed(ISession session)
      {

      }

      public void sessionClosed(ISession session)
      {
        removeSession(session);
      }

      public void newSessionData(ISessionData sessionData)
      {

      }

      public void newSessionDataStream(ISessionData sessionData,
          ISessionDataStream sessionDataStream)
      {

      }
    };
  }

  public Set<ISession> getSessions(Set<ISession> container)
  {
    if (container == null) container = new HashSet<ISession>();
    synchronized (_activeSessions)
    {
      container.addAll(_activeSessions);
    }

    return container;
  }

  public ISession getSession(UUID sessionId)
  {
    synchronized (_sessionById)
    {
      return _sessionById.get(sessionId);
    }
  }

  public void addListener(ISessionManagerListener listener, Executor executor)
  {
    _eventManager.addListener(listener, executor);
  }

  public Collection<ISessionManagerListener> getListeners()
  {
    Collection<ISessionManagerListener> listeners = new ArrayList<ISessionManagerListener>();
    _eventManager.getListeners(listeners);
    return listeners;
  }

  public void removeListener(ISessionManagerListener listener)
  {
    _eventManager.removeListener(listener);
  }

  public void addSession(ISession session)
  {

    synchronized (_sessionById)
    {
      _sessionById.put(session.getSessionId(), session);
    }

    synchronized (_activeSessions)
    {
      _activeSessions.add(session);
    }
    /*
     * add our own listener
     */
    session.addListener(_sessionListener, ExecutorServices.INLINE_EXECUTOR);

    _eventManager.notify(new Object[] { Boolean.TRUE, session });
  }

  public void addSession(AbstractSession session)
  {
    ISession newSession = new Session2SessionAdapter(session);
    session.setSession(newSession);
    addSession(newSession);
  }

  public void removeSession(ISession session)
  {

    session.removeListener(_sessionListener);

    synchronized (_sessionById)
    {
      _sessionById.remove(session.getSessionId());
    }

    synchronized (_activeSessions)
    {
      _activeSessions.remove(session);
    }

    _eventManager.notify(new Object[] { Boolean.FALSE, session });
  }
}
