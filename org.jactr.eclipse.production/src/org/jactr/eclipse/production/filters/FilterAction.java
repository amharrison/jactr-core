package org.jactr.eclipse.production.filters;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.zest.core.viewers.GraphViewer;

public class FilterAction extends Action
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(FilterAction.class);

  private AbstractRelationshipFilter _filter;
  private GraphViewer _viewer;
  
  public FilterAction(String name, AbstractRelationshipFilter filter, GraphViewer viewer)
  {
    this(name, AS_CHECK_BOX, filter, viewer);
  }
  
  public FilterAction(String name, int type, AbstractRelationshipFilter filter, GraphViewer viewer)
  {
    super(name, type);
    _filter = filter;
    _viewer = viewer;
  }
  
  public ViewerFilter getFilter()
  {
    return _filter;
  }
  
  
  public void setChecked(boolean checked)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Setting filter "+getText()+" to "+checked);
    _filter.setEnabled(!checked);
    super.setChecked(checked);
  }
  
  public void run()
  {
//    setChecked(!isChecked());
    _viewer.refresh();
  }
}
