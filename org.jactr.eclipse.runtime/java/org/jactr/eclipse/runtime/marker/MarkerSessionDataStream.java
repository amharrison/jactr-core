package org.jactr.eclipse.runtime.marker;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.AbstractRollingSessionDataStream;
import org.jactr.tools.marker.impl.DefaultMarker;
import org.jactr.tools.marker.tracer.MarkerTransformedEvent;

public class MarkerSessionDataStream extends
    AbstractRollingSessionDataStream<MarkerTransformedEvent, Long>
{

  /**
   * Logger definition
   */
  static private final transient Log           LOGGER = LogFactory
                                                          .getLog(MarkerSessionDataStream.class);

  private SortedMap<Long, Map<String, String>> _markerProperties;

  private Set<Long>                            _knownModifiedMarkers;

  public MarkerSessionDataStream(ISessionData sessionData, int windowSize)
  {
    super("marker", sessionData, windowSize);
    _markerProperties = new TreeMap<Long, Map<String, String>>();
    _knownModifiedMarkers = new TreeSet<Long>();
  }

  @Override
  public void clear()
  {
    _knownModifiedMarkers.clear();
    _markerProperties.clear();
    super.clear();
  }


  @Override
  protected double getTime(Long id)
  {
    try
    {
      return Double.parseDouble(getMarkerPropertiesInternal(id).get(
          DefaultMarker.OPEN_TIME));
    }
    catch (Exception e)
    {
      LOGGER
          .error(String.format("Failed to get proper open time for marker %d ",
              id), e);
      return 0;
    }
  }

  @Override
  protected Collection<Long> toOutputData(MarkerTransformedEvent input)
  {
    long id = input.getMarkerId();
    Map<String, String> newProperties = input
        .getProperties(new TreeMap<String, String>());
    Map<String, String> oldProperties = _markerProperties
        .put(id, newProperties);

    if (oldProperties != null) _knownModifiedMarkers.add(id);

    return Collections.singleton(id);
  }

  @Override
  protected Collection<Long> getModifiedData()
  {
    Collection<Long> modified = Collections.EMPTY_LIST;
    if (_knownModifiedMarkers.size() > 0)
    {
      modified = new ArrayList<Long>(_knownModifiedMarkers);
      _knownModifiedMarkers.clear();
    }

    return modified;
  }

  public double getCloseTime(long markerId)
  {
    return getDouble(markerId, DefaultMarker.CLOSE_TIME, Double.NaN);
  }

  public double getOpenTime(long markerId)
  {
    return getDouble(markerId, DefaultMarker.OPEN_TIME, Double.NaN);
  }

  public String getName(long markerId)
  {
    return getString(markerId, DefaultMarker.NAME, null);
  }

  public String getType(long markerId)
  {
    return getString(markerId, DefaultMarker.TYPE, null);
  }

  public String getDescription(long markerId)
  {
    return getString(markerId, DefaultMarker.DESCRIPTION, null);
  }

  public String getModelName(long markerId)
  {
    return getString(markerId, DefaultMarker.MODEL_NAME, null);
  }

  protected double getDouble(long markerId, String propertyName,
      double defaultValue)
  {
    try
    {
      Map<String, String> properties = getMarkerPropertiesInternal(markerId);
      String value = properties.get(propertyName);
      if (value == null) return defaultValue;
      return Double.parseDouble(value);
    }
    catch (Exception e)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn(String.format(
            "Could not extract %s from marker:%d, setting to %.2f ",
            propertyName, markerId, defaultValue), e);
      return defaultValue;
    }
  }

  protected String getString(long markerId, String propertyName,
      String defaultValue)
  {
    try
    {
      Map<String, String> properties = getMarkerPropertiesInternal(markerId);
      String value = properties.get(propertyName);
      if (value == null) return defaultValue;
      return value;
    }
    catch (Exception e)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn(String.format(
            "Could not extract %s from marker:%d, setting to %s ",
            propertyName, markerId, defaultValue), e);
      return defaultValue;
    }
  }

  public Map<String, String> getMarkerProperties(long markerId,
      Map<String, String> container)
  {
    if (container == null) container = new TreeMap<String, String>();
    Map<String, String> properties = _markerProperties.get(markerId);
    if (properties != null) container.putAll(properties);
    return container;
  }

  protected Map<String, String> getMarkerPropertiesInternal(long markerId)
  {
    return _markerProperties.get(markerId);
  }

  /**
   * zip through the data, looking for any that are closed. We only remove
   * expired and closed markers
   */
  @Override
  protected void removeSubset(double timeOfData, double cullToTime,
      Collection<Long> data, Collection<Long> removed)
  {
    Iterator<Long> itr = data.iterator();
    while (itr.hasNext())
    {
      long markerId = itr.next();
      double closeTime = getCloseTime(markerId);

      if (!Double.isNaN(closeTime) && closeTime < cullToTime)
      {
        removed.add(markerId);
        itr.remove();
      }
    }

  }

  @Override
  protected void removed(Collection<Long> removed)
  {
    for (Long toBeRemoved : removed)
      _markerProperties.remove(toBeRemoved);
  }
}
