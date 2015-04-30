package org.jactr.eclipse.ui.wizards.pages;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.jactr.eclipse.core.bundles.descriptors.CommonExtensionDescriptor;

public class CommonExtensionDescriptorLabelProvider extends LabelProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(CommonExtensionDescriptorLabelProvider.class);

  public CommonExtensionDescriptorLabelProvider()
  {

  }

  @Override
  public String getText(Object element)
  {
    return ((CommonExtensionDescriptor) element).getName();
  }


}
