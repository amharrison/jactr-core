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

import org.eclipse.core.runtime.IConfigurationElement;
import org.jactr.io.compiler.IReportableUnitCompiler;

public class UnitCompilerDescriptor extends CommonExtensionDescriptor
{
  
  private boolean _defaultEnabled;
  private IReportableUnitCompiler.Level _defaultLevel = IReportableUnitCompiler.Level.IGNORE;
  
  public UnitCompilerDescriptor(String contributor, String name, String className,
      String description, boolean defaultEnabled, IReportableUnitCompiler.Level defaultLevel)
  {
    super("org.jactr.io.unitcompiler", contributor, name, className, description);
    _defaultEnabled = defaultEnabled;
    _defaultLevel = defaultLevel;
  }

  public UnitCompilerDescriptor(IConfigurationElement descriptor)
  {
    super(descriptor);
    try
    {
      _defaultEnabled = Boolean.parseBoolean(descriptor.getAttribute("defaultEnabled"));
    }
    catch(Exception e)
    {
      //noop
    }
    try
    {
      _defaultLevel = IReportableUnitCompiler.Level.valueOf(descriptor.getAttribute("report").toUpperCase());
    }
    catch(Exception e)
    {
      
    }
  }

  
  public boolean isDefaultEnabled()
  {
    return _defaultEnabled;
  }
  
  public IReportableUnitCompiler.Level getReportLevel()
  {
    return _defaultLevel;
  }
}


