package org.jactr.eclipse.runtime.trace.impl;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.ISessionListener;
import org.jactr.tools.tracer.transformer.ITransformedEvent;

public abstract class RuntimeTraceDataManager<T> implements
 ISessionListener
{
  /**
   * Logger definition
   */
  static private final transient Log            LOGGER = LogFactory
                                                           .getLog(RuntimeTraceDataManager.class);

  private Map<ISession, Map<String, T>>      _data;

  private Map<ISession, Map<String, String>> _commonModelNameMap;

  private int                                   _executedSessions;

  protected RuntimeTraceDataManager()
  {
    _data = new HashMap<ISession, Map<String, T>>();
    _commonModelNameMap = new HashMap<ISession, Map<String, String>>();

    // ACTRSession.getACTRSessionTracker().addListener(this);
    // RuntimePlugin.getDefault().getSessionManager().addListener(this, null);
  }

  protected T getRuntimeTraceData(ISession session, String modelName,
      boolean create)
  {
    boolean added = false;
    T data = null;
    String commonName = null;
    synchronized (_data)
    {

      Map<String, T> container = _data.get(session);
      Map<String, String> commonNames = _commonModelNameMap.get(session);
      if (container == null)
      {
        if (!create) return null;
        container = new TreeMap<String, T>();
        commonNames = new TreeMap<String, String>();
        _commonModelNameMap.put(session, commonNames);
        _data.put(session, container);
        _executedSessions++;
      }

      /*
       * translation from the session specific modelname, to the system specific
       */
      commonName = commonNames.get(modelName);
      if (commonName == null)
      {
        commonName = modelName + (_data.size() == 1 ? "" : "." + _data.size());
        commonNames.put(modelName, commonName);
      }

      data = container.get(commonName);
      if (data == null)
      {
        if (!create) return null;
        data = createRuntimeTraceData(session, commonName, modelName);
        container.put(commonName, data);
        added = true;
      }
    }

    if (added) modelAdded(session, data, commonName);

    return data;
  }

  /**
   * hook to fire an event when a model is added
   * 
   * @param session
   * @param data
   * @param modelName
   */
  protected void modelAdded(ISession session, T data, String commonName)
  {

  }

  /**
   * hook to fire an event when a model is removed
   * 
   * @param session
   * @param data
   * @param modelName
   */
  protected void modelRemoved(ISession session, T data, String modelName)
  {

  }

  public T getRuntimeTraceData(ISession session, String modelName)
  {
    return getRuntimeTraceData(session, modelName, false);
  }

  public Collection<T> getAllRuntimeTraceData(Collection<T> container)
  {
    if (container == null) container = new ArrayList<T>();
    synchronized (_data)
    {
      for (Map<String, T> sessionData : _data.values())
        container.addAll(sessionData.values());
    }
    return container;
  }

  public Collection<T> getAllRuntimeTraceData(ISession session,
      Collection<T> container)
  {
    if (container == null) container = new ArrayList<T>();
    Map<String, T> data = null;
    synchronized (_data)
    {
      data = _data.get(session);
    }

    if (data != null) container.addAll(data.values());

    return container;
  }

  abstract protected T createRuntimeTraceData(ISession session,
      String commonModelName, String trueName);

  abstract protected void disposeRuntimeTraceData(ISession session,
      String commonModelName, T data);

  public void process(ITransformedEvent event, ISession session)
  {

    String modelName = event.getModelName();
    T data = getRuntimeTraceData(session, modelName, true);
    process(session, modelName, data, event);
  }

  abstract protected void process(ISession session, String modelName,
      T data, ITransformedEvent event);

  public void sessionOpened(ISession session)
  {
    // noop
  }

  public void sessionClosed(ISession session, boolean normal)
  {
    // noop
  }

  public void sessionDestroyed(ISession session)
  {
    Map<String, T> container = null;
    synchronized (_data)
    {
      container = _data.remove(session);
      _commonModelNameMap.remove(session);
    }

    if (container != null)
      for (Map.Entry<String, T> data : container.entrySet())
      {
        modelRemoved(session, data.getValue(), data.getKey());
        disposeRuntimeTraceData(session, data.getKey(), data.getValue());
      }
  }
}
