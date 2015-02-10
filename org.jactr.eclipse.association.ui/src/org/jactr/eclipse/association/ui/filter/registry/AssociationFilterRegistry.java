package org.jactr.eclipse.association.ui.filter.registry;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.jactr.eclipse.core.bundles.registry.AbstractExtensionPointRegistry;

public class AssociationFilterRegistry extends
    AbstractExtensionPointRegistry<AssociationFilterDescriptor>
{
  /**
   * Logger definition
   */
  static private final transient Log             LOGGER  = LogFactory
                                                             .getLog(AssociationFilterRegistry.class);

  static private final AssociationFilterRegistry DEFAULT = new AssociationFilterRegistry();

  static public AssociationFilterRegistry getRegistry()
  {
    return DEFAULT;
  }

  public AssociationFilterRegistry()
  {
    super("org.jactr.eclipse.association.ui.associationFilter");
  }

  @Override
  protected AssociationFilterDescriptor createDescriptor(
      IPluginElement extPointElement)
  {
    if (extPointElement.getName().equals("associationFilter"))
    {
      // String instrName = extPointElement.getAttribute("name").getValue();
      String instrClass = extPointElement.getAttribute("class").getValue();

      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Adding extension of associationFilter from "
            + extPointElement.getPluginBase().getId() + " class:" + instrClass);
      return new AssociationFilterDescriptor(extPointElement.getPluginBase()
          .getId(), instrClass);
    }
    else
      throw new IllegalArgumentException("Was expecting module tag, got "
          + extPointElement.getName());
  }

  @Override
  protected AssociationFilterDescriptor createDescriptor(
      IConfigurationElement extPointElement)
  {
    if (extPointElement.getName().equals("associationFilter"))
      return new AssociationFilterDescriptor(extPointElement);
    else
      throw new IllegalArgumentException(
          "Was expecting associationFilter tag, got "
              + extPointElement.getName());
  }

}
