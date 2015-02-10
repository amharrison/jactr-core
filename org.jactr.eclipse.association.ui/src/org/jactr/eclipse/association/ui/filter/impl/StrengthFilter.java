package org.jactr.eclipse.association.ui.filter.impl;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jactr.eclipse.association.ui.model.Association;

public class StrengthFilter extends ViewerFilter
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(StrengthFilter.class);

  private final double               _threshold;

  public StrengthFilter(double threshold)
  {
    _threshold = threshold;
  }

  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element)
  {
    if (element instanceof Association)
    {
      Association ass = (Association) element;
      return Math.abs(ass.getStrength()) >= _threshold;
    }
    return true;
  }

}
