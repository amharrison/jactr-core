/*
 * Created on Mar 15, 2007
 * Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu (jactr.org) This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.eclipse.core.bundles.descriptors;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;

public class InstrumentDescriptor extends CommonExtensionDescriptor
{

  private Map<String,String> _parameters;

  private boolean             _isHidden = false;
  
  public InstrumentDescriptor(String contributor, String name,
      String className, String desc, Map<String, String> parameters,
      boolean isHidden)
  {
    super("org.jactr.instruments",contributor,name,className,desc);
    _parameters = new TreeMap<String,String>(parameters);
    _isHidden = isHidden;
  }
  
  public InstrumentDescriptor(IConfigurationElement descriptor)
  {
    super(descriptor);
    _parameters = new TreeMap<String,String>();
    _isHidden = Boolean.parseBoolean(descriptor.getAttribute("hidden"));
  }

  
  public boolean isHidden()
  {
    return _isHidden;
  }
  
  public Map<String, String> getParameters()
  {
   if(getConfigurationElement()!=null)
     return getMapOfValues("parameter", "name", "value");
   return _parameters;
  }
}


