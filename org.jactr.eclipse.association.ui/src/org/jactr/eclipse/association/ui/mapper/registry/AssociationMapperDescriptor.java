package org.jactr.eclipse.association.ui.mapper.registry;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.jactr.eclipse.core.bundles.descriptors.CommonExtensionDescriptor;

public class AssociationMapperDescriptor extends CommonExtensionDescriptor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AssociationMapperDescriptor.class);

  public AssociationMapperDescriptor(String contributor,
      String name, String className)
  {
    super("org.jactr.eclipse.association.ui.associationMapper", contributor,
        name, className, "");
  }

  public AssociationMapperDescriptor(IConfigurationElement descriptor)
  {
    super(descriptor);
  }

}
