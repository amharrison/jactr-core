package org.jactr.eclipse.production.filters;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.Viewer;
import org.jactr.tools.analysis.production.relationships.IRelationship;

public class NegativeFilter extends AbstractRelationshipFilter
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(NegativeFilter.class);

  public NegativeFilter()
  {
    setEnabled(true);
  }
  
  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element)
  {
    if(!_enabled) return true;
    
    if(!(element instanceof IRelationship))
       return true;
    
    IRelationship rel = (IRelationship) element;

    return rel.getScore()>=0;
  }
}
