package org.jactr.eclipse.association.ui.mapper.registry;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.jactr.eclipse.core.bundles.registry.AbstractExtensionPointRegistry;

public class AssociationMapperRegistry extends
    AbstractExtensionPointRegistry<AssociationMapperDescriptor>
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AssociationMapperRegistry.class);

  static private final AssociationMapperRegistry DEFAULT = new AssociationMapperRegistry();

  static public AssociationMapperRegistry getRegistry()
  {
    return DEFAULT;
  }

  public AssociationMapperRegistry()
  {
    super("org.jactr.eclipse.association.ui.associationMapper");
  }

  @Override
  protected AssociationMapperDescriptor createDescriptor(
      IPluginElement extPointElement)
  {
    if (extPointElement.getName().equals("associationMapper"))
    {
      String instrName = extPointElement.getAttribute("name").getValue();
      String instrClass = extPointElement.getAttribute("class").getValue();

      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Adding extension of associationMapper from "
            + extPointElement.getPluginBase().getId() + " named:" + instrName
            + " class:" + instrClass);
      return new AssociationMapperDescriptor(extPointElement.getPluginBase()
          .getId(), instrName, instrClass);
    }
    else
      throw new IllegalArgumentException("Was expecting module tag, got "
          + extPointElement.getName());
  }

  @Override
  protected AssociationMapperDescriptor createDescriptor(
      IConfigurationElement extPointElement)
  {
    if (extPointElement.getName().equals("associationMapper"))
      return new AssociationMapperDescriptor(extPointElement);
    else
      throw new IllegalArgumentException(
          "Was expecting associationMapper tag, got "
              + extPointElement.getName());
  }

}
