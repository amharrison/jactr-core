/*
 * Created on Jul 25, 2004 Copyright (C) 2001-4, Anthony Harrison anh23@pitt.edu
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this library; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.jactr.eclipse.core.bundles;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * @author harrison TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Generation - Code and
 *         Comments
 */
public class BundleUtilities
{

  /**
   * Default logger
   */
  static private transient final Log LOGGER = LogFactory
                                                .getLog(BundleUtilities.class);

  private BundleUtilities()
  {
  }

  /**
   * Returns the bundle for a given bundle name, regardless whether the bundle
   * is resolved or not.
   * 
   * @param bundleName
   *            the bundle name
   * @return the bundle
   */
  static public Bundle getBundle(String bundleName)
  {
    Bundle bundle = Platform.getBundle(bundleName);
    if (bundle != null) return bundle;

    // Accessing unresolved bundle

    Bundle[] bundles = getPackageAdmin().getBundles(bundleName, null);
    if (bundles != null && bundles.length > 0) return bundles[0];
    return null;
  }

  static public PackageAdmin getPackageAdmin()
  {
    ServiceReference serviceRef = BundleTools.getDefault().getBundleContext()
        .getServiceReference(PackageAdmin.class.getName());
    PackageAdmin admin = (PackageAdmin) BundleTools.getDefault()
        .getBundleContext().getService(serviceRef);

    return admin;
  }

  static public PlatformAdmin getPlatformAdmin()
  {
    return Platform.getPlatformAdmin();
  }

  /**
   * return the root of the bundle
   * 
   * @param bundleName
   * @return
   */
  static public IPath getBundleLocation(String bundleName)
  {
    Bundle bundle = getBundle(bundleName);
    if (bundle == null) return null;

    URL local = null;
    try
    {
      local = FileLocator.toFileURL(bundle.getEntry("/")); //$NON-NLS-1$
    }
    catch (IOException e)
    {
      return null;
    }
    String fullPath = new File(local.getPath()).getAbsolutePath();
    return Path.fromOSString(fullPath);
  }

  /**
   * @param bundleName
   * @return
   */
  static protected void getRequiredBundles(String bundleName,
      State platformState, Collection<BundleDescription> requiredBundles)
  {
    BundleDescription descriptor = platformState.getBundle(bundleName, null);

    requiredBundles.add(descriptor);
    
    for(BundleDescription fragment : descriptor.getFragments())
      getRequiredBundles(fragment.getName(), platformState, requiredBundles);

    for (BundleSpecification requirement : descriptor.getRequiredBundles())
    {
      // if (!requirement.isOptional())
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(requirement.getName() + " isOptional " +
            requirement.isOptional());
      if (!requirement.isOptional())
        getRequiredBundles(requirement.getName(), platformState,
            requiredBundles);
    }
    

  }

  static public Collection<BundleDescription> getRequiredBundles(String bundleId)
  {
    PlatformAdmin pAdmin = getPlatformAdmin();
    State state = pAdmin.getState();
    Set<BundleDescription> required = new TreeSet<BundleDescription>(
        new Comparator<BundleDescription>() {

          public int compare(BundleDescription o1, BundleDescription o2)
          {
            return o1.getName().compareTo(o2.getName());
          }

        });

    getRequiredBundles(bundleId, state, required);

    return required;
  }

  /**
   * find dependencies of non workspace plugins
   */
  static public Collection<String> getDependencies(String pluginName)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Getting dependencies of " + pluginName);
    TreeSet<String> dependencies = new TreeSet<String>();
    getDependencies(pluginName, dependencies);
    return dependencies;
  }

  /**
   * find dependencies of workspace plugins
   */
  static public Collection<String> getDependencies(IProject project)
  {
    TreeSet<String> dependencies = new TreeSet<String>();
    getDependencies(project, dependencies);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Dependencies of " + project.getName() + " : " +
          dependencies);

    return dependencies;
  }

  static protected void getDependencies(IProject project,
      Set<String> dependencies)
  {
    IPluginModelBase modelBase = PluginRegistry.findModel(project);
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Search for " +
          project.getName() +
          " found " +
          (modelBase == null ? "nothing, skipping." : modelBase.getPluginBase()
              .getId()));
    if (modelBase != null) getDependencies(modelBase, dependencies);
  }

  /**
   * search for a plugin model base based on the plugin id
   * 
   * @param pluginId
   * @param dependencies
   */
  static protected void getDependencies(String pluginId,
      Set<String> dependencies)
  {
    IPluginModelBase modelBase = PluginRegistry.findModel(pluginId);
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Search for " +
          pluginId +
          " found " +
          (modelBase == null ? "nothing, skipping." : modelBase.getPluginBase()
              .getId()));
    if (modelBase != null) getDependencies(modelBase, dependencies);
  }

  /**
   * this is the code that actually does the work
   * 
   * @param modelBase
   * @param dependenices
   */
  static protected void getDependencies(IPluginModelBase modelBase,
      Set<String> dependencies)
  {
    if (modelBase == null) return;

    String id = modelBase.getPluginBase().getId();
    if (!dependencies.add(id)) return;
    
    for(BundleDescription desc : modelBase.getBundleDescription().getFragments())
      getDependencies(desc.getName(), dependencies);

    for (BundleSpecification requirement : modelBase.getBundleDescription()
        .getRequiredBundles())
      if (!dependencies.contains(requirement.getName()))
      {
        if (!requirement.isOptional())
          getDependencies(requirement.getName(), dependencies);
        else if (LOGGER.isDebugEnabled())
          LOGGER.debug(requirement.getName() + " is optional, ignoring");
      }
  }

}