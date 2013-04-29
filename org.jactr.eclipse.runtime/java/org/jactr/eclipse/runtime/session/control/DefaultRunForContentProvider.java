package org.jactr.eclipse.runtime.session.control;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class DefaultRunForContentProvider implements ITreeContentProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(DefaultRunForContentProvider.class);

  private final Double[]             _timeSteps = { 1.0, 10.0, 30.0, 60.0,
      300.0                                    };

  public void dispose()
  {

  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {

  }

  public Object[] getElements(Object inputElement)
  {
    return _timeSteps;
  }

  public Object[] getChildren(Object parentElement)
  {
    return null;
  }

  public Object getParent(Object element)
  {
    return null;
  }

  public boolean hasChildren(Object element)
  {
    return false;
  }

}
