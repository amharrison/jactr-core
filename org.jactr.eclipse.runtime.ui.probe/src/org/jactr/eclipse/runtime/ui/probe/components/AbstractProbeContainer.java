package org.jactr.eclipse.runtime.ui.probe.components;

/*
 * default logging
 */
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractProbeContainer<D, S> extends Canvas
{

  protected D              _probeData;

  protected Map<String, S> _probeSeries = new TreeMap<String, S>();

  protected Set<String>         _filteredOut = Collections
                                                 .synchronizedSet(new TreeSet<String>());

  public AbstractProbeContainer(Composite parent, int style)
  {
    super(parent, style);
  }

  public D getProbeData()
  {
    return _probeData;
  }

  public Set<String> getProbeNames()
  {
    return Collections.unmodifiableSet(_probeSeries.keySet());
  }

  public Set<String> getFilteredProbes(Set<String> container)
  {
    synchronized (_filteredOut)
    {
      container.addAll(_filteredOut);
    }
    return container;
  }

  abstract public void setFilteredProbes(Set<String> probeNames);

  /**
   * signal that we need to refresh the graph contents
   */
  public void refresh()
  {

  }

}