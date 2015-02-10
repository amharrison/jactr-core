package org.jactr.eclipse.association.ui.content;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.jactr.eclipse.association.ui.model.Association;
import org.jactr.eclipse.association.ui.model.ModelAssociations;

public class AssociativeContentProvider implements IGraphContentProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AssociativeContentProvider.class);

  private ModelAssociations          _associations;

  public Object[] getElements(Object input)
  {
    // possible when there is no input available yet

    if (input == _associations) return _associations.getAssociations();
    return null;
  }

  public Object getSource(Object rel)
  {
    return ((Association) rel).getJChunk();
  }

  public Object getDestination(Object rel)
  {
    return ((Association) rel).getIChunk();
  }

  public void dispose()
  {

  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    _associations = (ModelAssociations) newInput;

  }

}
