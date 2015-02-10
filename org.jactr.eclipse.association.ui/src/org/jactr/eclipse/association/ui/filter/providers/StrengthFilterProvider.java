package org.jactr.eclipse.association.ui.filter.providers;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.jactr.eclipse.association.ui.filter.IFilterProvider;
import org.jactr.eclipse.association.ui.filter.impl.StrengthFilter;
import org.jactr.eclipse.ui.generic.dialog.NumericInputDialog;

public class StrengthFilterProvider implements IFilterProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(StrengthFilterProvider.class);

  public StrengthFilterProvider()
  {
  }

  @Override
  public String getLabel()
  {
    return "Weak Links";
  }

  @Override
  public ViewerFilter[] getFilters()
  {
    // we want to prompt for a value, abs(strength) < threshold will be filtered
    // out
    NumericInputDialog nid = new NumericInputDialog(Display.getCurrent()
        .getActiveShell(), "Strength Threshold",
        "Will filter out |strength| < threshold", 0.1);
    nid.create();

    if (nid.open() == Window.OK)
      return new ViewerFilter[] { new StrengthFilter(nid.getValue()) };

    return new ViewerFilter[0];
  }

}
