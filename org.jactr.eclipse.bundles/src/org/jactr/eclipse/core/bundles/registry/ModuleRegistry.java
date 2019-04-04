/*
 * Created on Mar 28, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.eclipse.core.bundles.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.jactr.eclipse.core.bundles.descriptors.ModuleDescriptor;

public class ModuleRegistry extends
    AbstractExtensionPointRegistry<ModuleDescriptor>
{

  /**
   * Logger definition
   */

  static private final transient Log  LOGGER  = LogFactory
                                                  .getLog(ModuleRegistry.class);

  static private final ModuleRegistry DEFAULT = new ModuleRegistry();

  static public ModuleRegistry getRegistry()
  {
    return DEFAULT;
  }

  private ModuleRegistry()
  {
    super("org.jactr.osgi.modules");
  }

  @Override
  protected ModuleDescriptor createDescriptor(IPluginElement extPointElement)
  {
    if (extPointElement.getName().equals("module"))
    {
      String instrName = extPointElement.getAttribute("name").getValue();
      String instrClass = extPointElement.getAttribute("class").getValue();
      boolean enabled = Boolean.parseBoolean(extPointElement.getAttribute(
          "defaultEnabled").getValue());
      boolean requiresReality = Boolean.parseBoolean(extPointElement
          .getAttribute("requiresCommonReality").getValue());
      StringBuilder instrDesc = new StringBuilder();

      for (IPluginObject child : extPointElement.getChildren())
      {
        IPluginElement childElement = (IPluginElement) child;
        if (childElement.getName().equals("description"))
          instrDesc.append(childElement.getText()).append(" ");
      }

      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Adding extension of module from "
            + extPointElement.getPluginBase().getId() + " named:" + instrName
            + " class:" + instrClass + " desc:" + instrDesc);
      return new ModuleDescriptor(extPointElement.getPluginBase().getId(),
          instrName, instrClass, instrDesc.toString(), enabled, requiresReality);
    }
    else
      throw new IllegalArgumentException("Was expecting module tag, got "
          + extPointElement.getName());
  }

  @Override
  protected ModuleDescriptor createDescriptor(
      IConfigurationElement extPointElement)
  {
    if (extPointElement.getName().equals("module"))
      return new ModuleDescriptor(extPointElement);
    else
      throw new IllegalArgumentException("Was expecting module tag, got "
          + extPointElement.getName());
  }

}
