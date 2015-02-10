package org.jactr.eclipse.association.ui.filter.registry;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.jactr.eclipse.core.bundles.descriptors.CommonExtensionDescriptor;

public class AssociationFilterDescriptor extends CommonExtensionDescriptor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AssociationFilterDescriptor.class);

  public AssociationFilterDescriptor(String contributor, String className)
  {
    super("org.jactr.eclipse.association.ui.associationFilter", contributor,
        "", className, "");
  }

  public AssociationFilterDescriptor(IConfigurationElement descriptor)
  {
    super(descriptor);
  }

}
