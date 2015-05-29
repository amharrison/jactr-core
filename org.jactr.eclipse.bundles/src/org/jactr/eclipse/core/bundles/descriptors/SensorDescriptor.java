/*
 * Created on Mar 15, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.core.bundles.descriptors;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;

public class SensorDescriptor extends CommonExtensionDescriptor
{

  private Map<String, String> _properties;

  private boolean             _isClockOwner = false;

  public SensorDescriptor(String contributor, String name, String className,
      String description, boolean isClockOwner, Map<String, String> properties)
  {
    super("org.commonreality.core.sensors", contributor, name, className,
        description);
    _properties = new TreeMap<String, String>(properties);
    _isClockOwner = isClockOwner;
  }

  public SensorDescriptor(IConfigurationElement descriptor)
  {
    super(descriptor);
    _properties = new TreeMap<String, String>();
    try
    {
      _isClockOwner = Boolean.parseBoolean(descriptor
          .getAttribute("clock-owner"));
    }
    catch (Exception e)
    {
      _isClockOwner = false;
    }
  }
  
  public boolean isClockOwner()
  {
    return _isClockOwner;
  }

  public Map<String, String> getProperties()
  {
    if (getConfigurationElement() != null)
      return getMapOfValues("property", "name", "value");
    return _properties;
  }
}
