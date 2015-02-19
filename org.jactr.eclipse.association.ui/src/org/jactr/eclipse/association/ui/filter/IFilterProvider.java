package org.jactr.eclipse.association.ui.filter;

/*
 * default logging
 */
import org.eclipse.jface.viewers.ViewerFilter;

public interface IFilterProvider
{
  public String getLabel();

  public ViewerFilter[] getFilters();
}
