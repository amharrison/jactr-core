package org.jactr.eclipse.runtime.marker;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.AbstractRollingSessionDataStream;
import org.jactr.tools.marker.tracer.MarkerTransformedEvent;

public class OpenMarkerSessionDataStream extends
    AbstractRollingSessionDataStream<MarkerTransformedEvent, OpenMarkers>
{
  /**
   * Logger definition
   */
  static private final transient Log    LOGGER = LogFactory
                                                   .getLog(OpenMarkerSessionDataStream.class);

  private final MarkerSessionDataStream _msds;

  private final Set<Long>               _currentlyOpen;

  private final Set<OpenMarkers>        _knownModified;

  public OpenMarkerSessionDataStream(ISessionData sessionData,
 int windowSize,
      MarkerSessionDataStream msds)
  {
    super("openMarkers", sessionData, windowSize);
    _msds = msds;
    _currentlyOpen = new TreeSet<Long>();
    _knownModified = new HashSet<OpenMarkers>();
  }

  public MarkerSessionDataStream getRawMarkerDataStream()
  {
    return _msds;
  }

  @Override
  public void clear()
  {
    _knownModified.clear();
    _currentlyOpen.clear();
    super.clear();
  }

  @Override
  protected Collection<OpenMarkers> getModifiedData()
  {
    Collection<OpenMarkers> modified = Collections.EMPTY_LIST;
    if (_knownModified.size() > 0)
    {
      modified = new ArrayList<OpenMarkers>(_knownModified);
      _knownModified.clear();
    }

    return modified;
  }

  @Override
  protected Collection<OpenMarkers> toOutputData(MarkerTransformedEvent marker)
  {
    long markerId = marker.getMarkerId();
    double eventTime = marker.getSimulationTime();

    double lastDataTime = Double.MIN_VALUE;
    OpenMarkers lastData = getLastData();
    if (lastData != null) lastDataTime = lastData.getTime();

    Collection<OpenMarkers> rtn = Collections.EMPTY_LIST;
    /*
     * new time frame? create a new OpenMarkers
     */
    if (eventTime > lastDataTime)
    {
      lastData = new OpenMarkers(eventTime, _currentlyOpen);
      rtn = Collections.singleton(lastData);
    }
    else
      /*
       * reusing, mark it as modified
       */
      _knownModified.add(lastData);

    /*
     * now update the currentlyOpen set, that is, deal with this marker directly
     */
    if (marker.isClosed())
    {
      _currentlyOpen.remove(markerId);
      lastData.remove(markerId);
    }
    else
    {
      _currentlyOpen.add(markerId);
      if (lastData != null) lastData.add(markerId);
    }

    return rtn;
  }

  @Override
  protected double getTime(OpenMarkers id)
  {
    return id.getTime();
  }

}
