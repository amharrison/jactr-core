package org.jactr.eclipse.runtime.marker;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OpenMarkers
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(OpenMarkers.class);

  private final double               _time;

  private final Set<Long>            _openMarkers;

  public OpenMarkers(double time, Collection<Long> currentlyOpenMarkers)
  {
    _time = time;
    _openMarkers = new TreeSet<Long>(currentlyOpenMarkers);
  }


  public double getTime()
  {
    return _time;
  }

  public Collection<Long> getOpenMarkers(Collection<Long> container)
  {
    if (container == null) container = new ArrayList<Long>();
    container.addAll(_openMarkers);
    return container;
  }

  protected void add(Long openMarker)
  {
    _openMarkers.add(openMarker);
  }

  protected void remove(Long closedMarker)
  {
    _openMarkers.remove(closedMarker);
  }
}
