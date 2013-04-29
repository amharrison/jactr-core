package org.jactr.eclipse.runtime.session.impl;

/*
 * default logging
 */
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.execution.internal.GeneralEventManager;
import org.jactr.eclipse.execution.internal.GeneralEventManager.INotifier;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.ISessionListener;
import org.jactr.eclipse.runtime.session.control.ISessionController;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.manager.internal.SessionManager;
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;

public abstract class AbstractSession implements ISession
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractSession.class);

  static private enum EventType {
    CLOSED, DESTROYED, NEW_DATA, NEW_TIME, NEW_STREAM
  };

  private boolean                                               _closed    = false;

  private boolean                                               _destroyed = false;

  private final UUID                                            _sessionId;

  private final Map<String, ISessionData>                       _sessionData;

  private final GeneralEventManager<ISessionListener, Object[]> _eventManager;

  private final Map<String, Object>                             _metaData;

  public AbstractSession()
  {
    this(UUID.randomUUID());
  }

  public AbstractSession(UUID uuid)
  {
    _sessionData = new ConcurrentHashMap<String, ISessionData>();
    _sessionId = uuid;
    _eventManager = new GeneralEventManager<ISessionListener, Object[]>(
        new INotifier<ISessionListener, Object[]>() {

          public void notify(ISessionListener listener, Object[] event)
          {
            try
            {
            notifyListeners(listener, event);
            }
            catch (Exception e)
            {
              LOGGER.error("Failed to propogate event to listeners ", e);
            }
          }
        });

    _metaData = new TreeMap<String, Object>();
  }

  protected void notifyListeners(ISessionListener listener, Object[] event)
  {
    EventType type = (EventType) event[0];
    switch (type)
    {
      case CLOSED:
        listener.sessionClosed(AbstractSession.this);
        break;
      case DESTROYED:
        listener.sessionDestroyed(AbstractSession.this);
        break;
      case NEW_DATA:
        listener.newSessionData((ISessionData) event[1]);
        break;
      case NEW_STREAM:
        listener.newSessionDataStream(
            ((ISessionDataStream) event[1]).getSessionData(),
            (ISessionDataStream) event[1]);
        break;
    }
  }

  public UUID getSessionId()
  {
    return _sessionId;
  }

  /**
   * we dont support control through here, just yet. ideally, we'll return a
   * controller that directs the ArchivalIndex directly.
   */
  public ISessionController getController()
  {
    return null;
  }

  public Set<String> getMetaDataKeys(Set<String> container)
  {
    if (container == null) container = new HashSet<String>();
    container.addAll(_metaData.keySet());
    return container;
  }

  public Object getMetaData(String key)
  {
    return _metaData.get(key);
  }

  public Object setMetaData(String key, Object value)
  {
    return _metaData.put(key, value);
  }

  public Set<String> getKeys(Set<String> container)
  {
    if (container == null) container = new HashSet<String>();
    container.addAll(_sessionData.keySet());
    return container;
  }

  public ISessionData getData(String key)
  {
    return _sessionData.get(key);
  }

  public void addData(String key, ISessionData sessionData)
  {
    if (sessionData != _sessionData.put(key, sessionData))
      fireNewData(sessionData);
  }

  public void close()
  {
    if (!isOpen()) return;
    try
    {
      for (ISessionData data : _sessionData.values())
        try
        {
          data.close();
        }
        catch (Exception e)
        {
          LOGGER.error("Could not close data ", e);
        }

      _closed = true;
      closeSession();
    }
    finally
    {
      fireClosed();
    }
  }

  public boolean isOpen()
  {
    return !_closed;
  }

  abstract protected void closeSession();

  public void destroy()
  {
    if (hasBeenDestroyed()) return;

    try
    {
      if (isOpen()) close();

      for (ISessionData data : _sessionData.values())
        try
        {
          data.delete();
        }
        catch (Exception e)
        {
          LOGGER.error("Could not delete data ", e);
        }

      /*
       * we keep the containers as they are likely keys in the viewers
       */
      // _sessionData.clear();

      destroySession();
    }
    finally
    {
      _destroyed = true;
      fireDestroyed();

      ((SessionManager) RuntimePlugin.getDefault().getSessionManager())
          .removeSession(this);
    }
  }

  public boolean hasBeenDestroyed()
  {
    return _destroyed;
  }

  /**
   * destroy the session
   */
  abstract protected void destroySession();

  public void addListener(ISessionListener listener, Executor executor)
  {
    _eventManager.addListener(listener, executor);
  }

  public void removeListener(ISessionListener listener)
  {
    _eventManager.removeListener(listener);
  }

  protected void fireClosed()
  {
    _eventManager.notify(new Object[] { EventType.CLOSED });
  }

  protected void fireDestroyed()
  {
    _eventManager.notify(new Object[] { EventType.DESTROYED });
  }

  protected void fireNewData(ISessionData sessionData)
  {
    _eventManager.notify(new Object[] { EventType.NEW_DATA, sessionData });
  }

  protected void fireNewDataStream(ISessionDataStream stream)
  {
    _eventManager.notify(new Object[] { EventType.NEW_STREAM, stream });
  }

  public void newDataStreamAdded(ISessionDataStream stream)
  {
    fireNewDataStream(stream);
  }

}
