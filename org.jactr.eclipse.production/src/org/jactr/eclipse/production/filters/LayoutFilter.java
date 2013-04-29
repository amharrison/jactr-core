package org.jactr.eclipse.production.filters;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class LayoutFilter // implements Filter
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(LayoutFilter.class);

  
  private ViewerFilter[] _filters;
  private Viewer _viewer;
  
  public LayoutFilter(Viewer viewer, ViewerFilter ... filters)
  {
    _filters = filters;
    _viewer = viewer;
  }
  
  // public boolean isObjectFiltered(LayoutItem object)
  // {
  // for(ViewerFilter filter : _filters)
  // if (!filter.select(_viewer, null, object.getGraphData()))
  // return true;
  //
  // return false;
  // }

}
