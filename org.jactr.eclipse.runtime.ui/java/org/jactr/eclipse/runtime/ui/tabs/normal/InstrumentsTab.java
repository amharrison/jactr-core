/*
 * Created on Jul 12, 2004 Copyright (C) 2001-4, Anthony Harrison anh23@pitt.edu
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

package org.jactr.eclipse.runtime.ui.tabs.normal;

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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.jactr.eclipse.core.bundles.descriptors.CommonExtensionDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.InstrumentDescriptor;
import org.jactr.eclipse.core.bundles.registry.InstrumentRegistry;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.eclipse.runtime.ui.tabs.CommonExtensionDescriptorTab;
import org.jactr.eclipse.ui.images.JACTRImages;

public class InstrumentsTab extends CommonExtensionDescriptorTab
{

  
  public InstrumentsTab()
  {
    setTabDescription("Instruments can be configured and attached to specific models to record or manipulate behavior.");
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
  protected String getDescriptorAttributeName()
  {
    return ACTRLaunchConstants.ATTR_INSTRUMENTS;
  }

  @Override
  protected Map<String, String> getParameters(
      CommonExtensionDescriptor descriptor)
  {
    return ((InstrumentDescriptor) descriptor).getParameters();
  }

  @Override
  protected Collection<? extends CommonExtensionDescriptor> getRequiredDescriptors(
      ILaunchConfiguration config) throws CoreException
  {
    try
    {
      return ACTRLaunchConfigurationUtils.getRequiredInstruments(config);
    }
    catch (Exception e)
    {
      return Collections.EMPTY_LIST;
    }
  }

  @Override
  protected Collection<? extends CommonExtensionDescriptor> getAvailableDescriptors(
      IProject project)
  {
    if (project != null)
      return InstrumentRegistry.getRegistry().getDescriptors(project, true);

    return InstrumentRegistry.getRegistry().getAllDescriptors();
  }

  @Override
  public void createControl(Composite parent)
  {
    super.createControl(parent);
    ViewerFilter filter = new ViewerFilter() {

      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element)
      {
        /*
         * not currently operational as the element types are strings. which is
         * odd, since the input is a collection of intstrument descriptors.
         */
        // InstrumentDescriptor id = (InstrumentDescriptor) element;
        // if (id != null && id.isHidden()) return false;
        return true;
      }

    };
    _descriptorList.setFilters(new ViewerFilter[] { filter });
  }

}