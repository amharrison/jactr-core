package org.jactr.eclipse.production.filters;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jactr.tools.analysis.production.relationships.IRelationship;

/**
 * filters out ambiguous connections
 * @author harrison
 *
 */
public abstract class AbstractRelationshipFilter extends ViewerFilter
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractRelationshipFilter.class);

  protected boolean _enabled = false;
  
  public void setEnabled(boolean enabled)
  {
    _enabled = enabled;
  }
  
  public boolean isEnabled()
  {
    return _enabled;
  }
  
}
