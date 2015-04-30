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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginObject;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.jactr.eclipse.core.bundles.BundleTools;
import org.jactr.eclipse.core.bundles.BundleUtilities;
import org.jactr.eclipse.core.bundles.descriptors.ExtensionDescriptor;

public abstract class AbstractExtensionPointRegistry<T extends ExtensionDescriptor>
{
  /**
   * Logger definition
   */

  static private final transient Log           LOGGER = LogFactory
                                                          .getLog(AbstractExtensionPointRegistry.class);

  private final String                         _extensionPointId;

  private final Map<String, CachedInformation> _map;

  private final String                         ALL    = "*ALL*";

  public AbstractExtensionPointRegistry(String extensionPoint)
  {
    _extensionPointId = extensionPoint;
    _map = new TreeMap<String, CachedInformation>();
  }

  /**
   * flush the cache entirely.
   */
  public void flush()
  {
    _map.clear();
  }

  /**
   * returns all the known descriptors that are not tied to specific projects..
   * i.e. global visibility.
   * 
   * @return
   */
  synchronized public Collection<T> getAllDescriptors()
  {
    Collection<T> descriptors = Collections.emptyList();

    CachedInformation information = _map.get(ALL);
    if (information == null || information.isStale())
    {
      descriptors = createFromExtension(getInstalledExtensions(null));
      _map.put(ALL, new CachedInformation(descriptors));
    }
    else
      descriptors = information.getDescriptors();

    return descriptors;
  }

  public Collection<T> getDescriptors(IProject project)
  {
    return getDescriptors(PluginRegistry.findModel(project), false);
  }

  public Collection<T> getDescriptors(IProject project,
      boolean includeRequirements)
  {
    return getDescriptors(PluginRegistry.findModel(project),
        includeRequirements);
  }

  /**
   * @param pluginProject
   * @return
   */
  public Collection<T> getDescriptors(String pluginProject)
  {
    return getDescriptors(PluginRegistry.findModel(pluginProject), false);
  }

  public Collection<T> getDescriptors(IPluginModelBase modelBase,
      boolean includeRequirements)
  {
    ArrayList<T> rtn = new ArrayList<T>();

    for (String reqId : BundleUtilities.getDependencies(modelBase
        .getPluginBase().getId()))
      rtn.addAll(getExtensionDescriptors(PluginRegistry.findModel(reqId)));

    return rtn;
  }

  protected boolean isWorkspaceModel(IPluginModelBase modelBase)
  {
    String id = modelBase.getPluginBase().getId();
    for (IPluginModelBase mb : PluginRegistry.getWorkspaceModels())
      if (id.equals(mb.getPluginBase().getId())) return true;
    return false;
  }

  synchronized protected Collection<T> getExtensionDescriptors(
      IPluginModelBase modelBase)
  {
    Collection<T> descriptors = Collections.emptyList();
    if (modelBase == null) return descriptors;

    CachedInformation information = _map.get(modelBase.getPluginBase().getId());
    if (information == null || information.isStale())
    {
      try
      {
        if (isWorkspaceModel(modelBase))
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(modelBase.getPluginBase().getName()
                + " is in workspace");
          descriptors = createFromPluginExtension(getPluginExtensions(modelBase));
        }
        else
          descriptors = createFromExtension(getInstalledExtensions(modelBase));
      }
      catch (Exception e)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(
              "Failed to get extesnion descriptors for, creating empty", e);
      }

      /*
       * add to the cache
       */
      _map.put(modelBase.getPluginBase().getId(), new CachedInformation(
          descriptors));
    }
    else
      descriptors = information.getDescriptors();

    return descriptors;
  }

  /**
   * create a descriptor from IPluginExtension's IPluginElement
   */
  abstract protected T createDescriptor(IPluginElement extPointElement);

  /**
   * @param extPointElement
   * @return
   */
  abstract protected T createDescriptor(IConfigurationElement extPointElement);

  /**
   * create all the descriptors from these extensions. This are installed in the
   * workbench
   * 
   * @param extensions
   * @return
   */
  protected Collection<T> createFromExtension(Collection<IExtension> extensions)
  {
    if (extensions.size() == 0) return Collections.emptyList();

    // it's at least the same size as extensions..
    ArrayList<T> rtn = new ArrayList<T>(extensions.size());

    for (IExtension extensionPoint : extensions)
      for (IConfigurationElement child : extensionPoint
          .getConfigurationElements())
        if (child instanceof IConfigurationElement)
        {
          IConfigurationElement element = child;
          try
          {
            T descriptor = createDescriptor(element);
            rtn.add(descriptor);
          }
          catch (RuntimeException e)
          {
            String message = "Could not extract create descriptor from "
                + element;
            LOGGER.error(message, e);
            BundleTools.error(message, e);
          }
        }
        else if (LOGGER.isDebugEnabled())
          LOGGER
              .debug("No clue what to do with non IPluginElement children. Got : "
                  + child + " [" + child.getClass().getName() + "]");

    return rtn;
  }

  /**
   * create all the descriptors from these extensions, defined in the workspace
   * 
   * @param extensions
   * @return
   */
  protected Collection<T> createFromPluginExtension(
      Collection<IPluginExtension> extensions)
  {
    if (extensions.size() == 0) return Collections.emptyList();

    // it's at least the same size as extensions..
    ArrayList<T> rtn = new ArrayList<T>(extensions.size());

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Processing plugin extensions " + extensions);

    for (IPluginExtension extensionPoint : extensions)
      if (extensionPoint.getChildCount() == 0)
      {
        if (LOGGER.isWarnEnabled())
          LOGGER.warn("Extension " + extensionPoint
              + " is defined, but has no children?");
      }
      else
        for (IPluginObject child : extensionPoint.getChildren())
          if (child instanceof IPluginElement)
          {
            IPluginElement element = (IPluginElement) child;
            try
            {
              rtn.add(createDescriptor(element));
            }
            catch (RuntimeException e)
            {
              String message = "Could not extract create descriptor from "
                  + element;
              LOGGER.error(message, e);
              BundleTools.error(message, e);
            }
          }
          else if (LOGGER.isDebugEnabled())
            LOGGER
                .debug("No clue what to do with non IPluginElement children. Got : "
                    + child + " [" + child.getClass().getName() + "]");

    if (LOGGER.isDebugEnabled() && rtn.size() == 0 && extensions.size() != 0)
      LOGGER
          .debug("Could not make an descriptors from extensions. Something may have gone quietly wrong above. ext:"
              + extensions);

    return rtn;
  }

  /**
   * load the extensions that match extensionPoint defined in the workspace
   * plugin bundleId. This is a workspace bundle that has bundle manifest
   * defined.
   * 
   * @param bundleId
   * @return
   */
  protected Collection<IPluginExtension> getPluginExtensions(
      IPluginModelBase modelBase)
  {
    if (modelBase == null) return Collections.emptyList();

    ArrayList<IPluginExtension> rtn = new ArrayList<IPluginExtension>();

    for (IPluginExtension extension : modelBase.getExtensions(true)
        .getExtensions())
      if (_extensionPointId.equals(extension.getPoint())) rtn.add(extension);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Returning extensions of "
          + modelBase.getPluginBase().getName() + ": " + rtn);

    return rtn;
  }

  /**
   * returns all the extensions of extensionpoint in pluginid
   * 
   * @param pluginId
   * @return
   */
  protected Collection<IExtension> getInstalledExtensions(
      IPluginModelBase modelBase)
  {
    String id = null;
    if (modelBase != null) id = modelBase.getPluginBase().getId();

    Collection<IExtension> rtn = Collections.emptyList();
    try
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Searching " + id + " for " + _extensionPointId);
      IExtensionRegistry extReg = Platform.getExtensionRegistry();
      IExtensionPoint iep = extReg.getExtensionPoint(_extensionPointId);

      if (iep == null) return rtn;

      IExtension[] extensions = iep.getExtensions();

      rtn = new ArrayList<IExtension>();
      for (IExtension extension : extensions)
      {
        String contrib = extension.getContributor().getName();
        extension.getNamespaceIdentifier();
        if (id == null || contrib.equals(id)) rtn.add(extension);
      }

      if (LOGGER.isDebugEnabled() && rtn.size() != 0)
        LOGGER.debug("Found " + rtn.size() + " extension of "
            + _extensionPointId + " provided by " + id);
    }
    catch (Exception e)
    {
      LOGGER.error("Could not retrieve extensions for point:"
          + _extensionPointId, e);
    }
    return rtn;
  }

  /**
   * stores the cached information
   * 
   * @author developer
   */
  class CachedInformation
  {

    private final long          _timeStamp;

    private final Collection<T> _descriptors;

    public CachedInformation(Collection<T> descriptors)
    {
      _timeStamp = System.currentTimeMillis();
      _descriptors = Collections.unmodifiableCollection(new ArrayList<T>(
          descriptors));
    }

    public boolean isStale()
    {
      // 1 minute cache
      return System.currentTimeMillis() - _timeStamp > 60 * 1000;
    }

    public Collection<T> getDescriptors()
    {
      return _descriptors;
    }

  }
}
