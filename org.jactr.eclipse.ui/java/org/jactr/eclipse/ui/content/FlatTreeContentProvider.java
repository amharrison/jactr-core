package org.jactr.eclipse.ui.content;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class FlatTreeContentProvider extends ArrayContentProvider implements
    ITreeContentProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(FlatTreeContentProvider.class);

  public FlatTreeContentProvider()
  {

  }

  @Override
  public Object[] getChildren(Object parentElement)
  {
    return null;
  }

  @Override
  public Object getParent(Object element)
  {
    return null;
  }

  @Override
  public boolean hasChildren(Object element)
  {
    return false;
  }

}
