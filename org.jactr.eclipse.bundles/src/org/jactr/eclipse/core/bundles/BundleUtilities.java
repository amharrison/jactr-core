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

import java.util.Collections;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.DependencyManager;

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
   * return the bundle descriptions for this and *all* its dependencies
   *
   * @param pluginId
   * @return
   */
  static public Set<BundleDescription> getSelfAndDependencies(String pluginId)
  {
    IPluginModelBase modelBase = PluginRegistry.findModel(pluginId);
    return DependencyManager
        .getSelfAndDependencies(Collections.singleton(modelBase));
  }

  static public Set<BundleDescription> getSelfAndDependencies(IProject project)
  {
    IPluginModelBase modelBase = PluginRegistry.findModel(project);
    return DependencyManager
        .getSelfAndDependencies(Collections.singleton(modelBase));
  }


}