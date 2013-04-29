package org.jactr.eclipse.ui.content;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class ACTRLazyContentProvider implements ILazyTreeContentProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER           = LogFactory
                                                          .getLog(ACTRLazyContentProvider.class);

  private ACTRContentProvider        _contentProvider;

  private TreeViewer                 _treeViewer;
  
  public ACTRLazyContentProvider()
  {
    _contentProvider = new ACTRContentProvider(true);
    _contentProvider.setShowOnlyLocal(true);
  }
  
  public boolean isImportedContentFiltered()
  {
    return _contentProvider.isShowOnlyLocal();
  }
  
  public void setImportedContentFiltered(boolean filter)
  {
    _contentProvider.setShowOnlyLocal(filter);
  }

  public Object getParent(Object element)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Returning parent for " + element);
    return _contentProvider.getParent(element);
  }

  public void updateChildCount(Object element, int currentChildCount)
  {
    Object[] children = _contentProvider.getChildren(element);
    if (children.length != currentChildCount)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Setting child count of " + element + " to "
            + children.length + " from " + currentChildCount);

      _treeViewer.setChildCount(element, children.length);
    }
  }

  public void updateElement(Object parent, int index)
  {
    if(parent==null) return;
    Object[] children = _contentProvider.getChildren(parent);
    if (children.length == 0) return;

    Object child = children[index];
    
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Updating element " + index + " of " + parent + " to "
          + child);

    _treeViewer.replace(parent, index, children[index]);
    
    children = _contentProvider.getChildren(child);
    _treeViewer.setChildCount(child, children.length);
  }

  public void dispose()
  {
    _contentProvider.dispose();
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    _treeViewer = (TreeViewer) viewer;
    _contentProvider.inputChanged(viewer, oldInput, newInput);
  }

}
