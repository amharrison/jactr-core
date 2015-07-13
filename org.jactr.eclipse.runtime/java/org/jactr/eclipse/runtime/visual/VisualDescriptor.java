package org.jactr.eclipse.runtime.visual;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.tools.tracer.transformer.visual.TransformedVisualEvent;

public class VisualDescriptor
{
  /**
   * Logger definition
   */
  static private final transient Log            LOGGER = LogFactory
                                                           .getLog(VisualDescriptor.class);

  private Map<IIdentifier, Map<String, Object>> _objects;

  // what object was found
  private IIdentifier                           _searchResult;

  // what object was encoded
  private IIdentifier                           _encoded;

  private String                                _commonName;
  private String                                _modelName;

  private Collection<IVisualDescriptorListener> _listeners;

  private double[]                              _resolution;

  private double[]                              _fov;

  final private ISession                        _session;

  public VisualDescriptor(String commonName, String modelName,
 ISession session)
  {
    _commonName = commonName;
    _modelName = modelName;
    _objects = new HashMap<IIdentifier, Map<String, Object>>();
    _listeners = new ArrayList<IVisualDescriptorListener>();
    _session = session;
  }

  public ISession getSession()
  {
    return _session;
  }
  
  public void setModelName(String modelName)
  {
    _modelName = modelName;
  }

  public void add(IVisualDescriptorListener listener)
  {
    _listeners.add(listener);
  }

  public void remove(IVisualDescriptorListener listener)
  {
    _listeners.remove(listener);
  }

  public double[] getResolution()
  {
    return _resolution;
  }

  public double[] getFOV()
  {
    return _fov;
  }

  public Collection<IIdentifier> getIdentifiers()
  {
    synchronized (_objects)
    {
      return new ArrayList<IIdentifier>(_objects.keySet());
    }
  }

  /**
   * should synchronize when iterating
   * 
   * @param identifier
   * @return
   */
  public Map<String, Object> getData(IIdentifier identifier)
  {
    synchronized (_objects)
    {
      Map<String, Object> data = _objects.get(identifier);
      if (data == null) return Collections.emptyMap();
      return Collections.unmodifiableMap(data);
    }
  }

  public String getModelName()
  {
    return _modelName;
  }

  public void process(TransformedVisualEvent event)
  {
    _fov = event.getFOV();
    _resolution = event.getResolution();

    switch (event.getType())
    {
      case ADDED:
        added(event);
        break;
      case ENCODED:
        encoded(event);
        break;
      case FOUND:
        found(event);
        break;
      case REMOVED:
        removed(event);
        break;
      case UPDATED:
        updated(event);
        break;
    }
  }

  private void added(TransformedVisualEvent event)
  {
    synchronized (_objects)
    {
      _objects.put(event.getIdentifier(), event.getData());
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Added " + event.getIdentifier() + " with "
          + event.getData());

    for (IVisualDescriptorListener listener : _listeners)
      listener.added(this, event.getIdentifier());
  }

  private void encoded(TransformedVisualEvent event)
  {
    _encoded = event.getIdentifier();

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Encoded " + event.getIdentifier());

    for (IVisualDescriptorListener listener : _listeners)
      listener.encoded(this, _encoded);
  }

  private void found(TransformedVisualEvent event)
  {
    _searchResult = event.getIdentifier();

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Found " + _searchResult);

    for (IVisualDescriptorListener listener : _listeners)
      listener.found(this, _searchResult);
  }

  private void removed(TransformedVisualEvent event)
  {
    synchronized (_objects)
    {
      _objects.remove(event.getIdentifier());
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Removed " + event.getIdentifier());

    for (IVisualDescriptorListener listener : _listeners)
      listener.removed(this, event.getIdentifier());
  }

  private void updated(TransformedVisualEvent event)
  {
    Map<String, Object> data = null;
    synchronized (_objects)
    {
      data = _objects.get(event.getIdentifier());
    }
    
    if (data == null)
    {
      added(event);
      return;
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Updated " + event.getIdentifier() + " with "
          + event.getData());

    synchronized (data)
    {
      data.putAll(event.getData());
    }

    for (IVisualDescriptorListener listener : _listeners)
      listener.updated(this, event.getIdentifier());
  }
}
