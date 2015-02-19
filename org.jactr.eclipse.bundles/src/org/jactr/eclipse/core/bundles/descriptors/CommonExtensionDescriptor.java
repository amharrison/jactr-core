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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.jactr.eclipse.core.bundles.BundleTools;

/**
 * @author developer
 */
public class CommonExtensionDescriptor extends ExtensionDescriptor
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(CommonExtensionDescriptor.class);

  private IConfigurationElement      _configurationElement;

  private String                     _className;

  private String                     _description;

  public CommonExtensionDescriptor(String extensionPoint, String contributor,
      String name, String className, String description)
  {
    super(extensionPoint, contributor, name, true);
    _className = className;
    _description = description;
  }

  public CommonExtensionDescriptor(IConfigurationElement descriptor)
  {
    super(descriptor.getDeclaringExtension()
        .getExtensionPointUniqueIdentifier(), descriptor
        .getDeclaringExtension().getContributor().getName(), descriptor
        .getAttribute("name"), false);
    _configurationElement = descriptor;
    _className = _configurationElement.getAttribute("class");
  }

  public String getClassName()
  {
    return _className;
  }

  public IConfigurationElement getConfigurationElement()
  {
    return _configurationElement;
  }

  

  @SuppressWarnings("unchecked")
  public Object instantiate() throws CoreException
  {
    String className = getClassName();
    try
    {
      Class clazz = getDefinedClass(className);
      return clazz.newInstance();
    }
    catch (Exception e)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER
        .debug(
            "Could not instantiate extension, will try IExecutableExtension ",
            e);
    }
    try
    {
      return _configurationElement.createExecutableExtension("class");
    }
    catch (CoreException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      LOGGER.error("Could not load " + className + " for extension "
          + getExtensionPointID() + " ", e);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private Class getDefinedClass(String className)
  {
    try
    {
      return getClass().getClassLoader().loadClass(className);
    }
    catch (Exception e)
    {
      LOGGER.debug("Could not load class " + className, e);
      BundleTools.debug("Could not load class " + className, e);
      return null;
    }
  }

  @Override
  public String toString()
  {
    String name = getName();
    if (name != null) return name;
    return super.toString();
  }

  public String getDescription()
  {
    if (getConfigurationElement() != null)
      return getCompoundString("description");
    return _description;
  }

  protected String getCompoundString(String elementName)
  {
    IConfigurationElement[] elements = getConfigurationElement().getChildren(
        elementName);
    StringBuilder sb = new StringBuilder();
    for (IConfigurationElement element : elements)
      sb.append(element.getValue()).append("\n");
    return sb.toString();
  }

  protected Map<String, String> getMapOfValues(String elementName,
      String keyAttr, String valueAttr)
  {
    IConfigurationElement[] parameters = getConfigurationElement().getChildren(
        elementName);
    Map<String, String> params = new TreeMap<String, String>();
    for (IConfigurationElement parameter : parameters)
      params.put(parameter.getAttribute(keyAttr), parameter
          .getAttribute(valueAttr));
    return params;
  }
}
