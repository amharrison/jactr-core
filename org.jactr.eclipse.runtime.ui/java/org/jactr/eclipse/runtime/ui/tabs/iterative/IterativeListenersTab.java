/*
 * Created on Jun 9, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.runtime.ui.tabs.iterative;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jactr.eclipse.core.bundles.descriptors.CommonExtensionDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.IterativeListenerDescriptor;
import org.jactr.eclipse.core.bundles.registry.IterativeListenerRegistry;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.eclipse.runtime.ui.tabs.CommonExtensionDescriptorTab;
import org.jactr.eclipse.ui.images.JACTRImages;

public class IterativeListenersTab extends CommonExtensionDescriptorTab
{

  public IterativeListenersTab()
  {
    setTabDescription("Iterative listeners are just like model instruments. You can track and manipulate the behavior of bulk runs with these.");
  }

  @Override
  protected IContentProvider createContentProvider()
  {
    return new ArrayContentProvider();
  }

  @Override
  protected ILabelProvider createLabelProvider()
  {
    return new LabelProvider() {

      @Override
      public Image getImage(Object element)
      {
        return JACTRImages.getImage(JACTRImages.TOOL);
      }

      @Override
      public String getText(Object element)
      {
        return element.toString();
      }
    };
  }

  @Override
  protected Collection<? extends CommonExtensionDescriptor> getAvailableDescriptors(
      IProject project)
  {
    if (project != null)
      return IterativeListenerRegistry.getRegistry().getDescriptors(project);
    return IterativeListenerRegistry.getRegistry().getAllDescriptors();
  }

  @Override
  protected String getDescriptorAttributeName()
  {
    return ACTRLaunchConstants.ATTR_ITERATIVE_LISTENERS;
  }

  @Override
  protected Map<String, String> getParameters(
      CommonExtensionDescriptor descriptor)
  {
    return ((IterativeListenerDescriptor) descriptor).getParameters();
  }

  @Override
  protected Collection<? extends CommonExtensionDescriptor> getRequiredDescriptors(
      ILaunchConfiguration config) throws CoreException
  {
    try
    {
      return ACTRLaunchConfigurationUtils.getRequiredListeners(config);
    }
    catch (Exception e)
    {
      return Collections.EMPTY_LIST;
    }
  }

}
