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

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.jactr.eclipse.core.bundles.descriptors.SensorDescriptor;

public class SensorRegistry extends
    AbstractExtensionPointRegistry<SensorDescriptor>
{

  /**
   * Logger definition
   */

  static private final transient Log  LOGGER  = LogFactory
                                                  .getLog(SensorRegistry.class);

  static private final SensorRegistry DEFAULT = new SensorRegistry();

  static public SensorRegistry getRegistry()
  {
    return DEFAULT;
  }

  private SensorRegistry()
  {
    super("org.commonreality.core.sensors");
  }

  @Override
  protected SensorDescriptor createDescriptor(IPluginElement extPointElement)
  {
    if (extPointElement.getName().equals("sensor"))
    {
      String sensorName = extPointElement.getAttribute("name").getValue();
      String sensorClass = extPointElement.getAttribute("class").getValue();
      boolean isClockOwner = false;

      try
      {
        isClockOwner = Boolean.parseBoolean(extPointElement.getAttribute(
            "clock-owner").getValue());
      }
      catch (Exception e)
      {
        isClockOwner = false;
      }

      StringBuilder sensorDesc = new StringBuilder();
      Map<String, String> properties = new TreeMap<String, String>();
      for (IPluginObject child : extPointElement.getChildren())
      {
        IPluginElement childElement = (IPluginElement) child;
        if (childElement.getName().equals("description"))
          sensorDesc.append(childElement.getText()).append(" ");
        else if (childElement.getName().equals("property"))
          properties.put(childElement.getAttribute("name").getValue(),
              childElement.getAttribute("value").getValue());
      }

      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Adding extension of sensor from "
            + extPointElement.getPluginBase().getId() + " named:" + sensorName
            + " class:" + sensorClass + " desc:" + sensorDesc + " props:"
            + properties);
      return new SensorDescriptor(extPointElement.getPluginBase().getId(),
          sensorName, sensorClass, sensorDesc.toString(), isClockOwner,
          properties);
    }
    else
      throw new IllegalArgumentException("Was expecting sensor tag, got "
          + extPointElement.getName());
  }

  @Override
  protected SensorDescriptor createDescriptor(
      IConfigurationElement extPointElement)
  {
    if (extPointElement.getName().equals("sensor"))
      return new SensorDescriptor(extPointElement);
    else
      throw new IllegalArgumentException("Was expecting sensor tag, got "
          + extPointElement.getName());
  }

}
