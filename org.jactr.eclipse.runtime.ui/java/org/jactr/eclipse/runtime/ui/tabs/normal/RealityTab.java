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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.bundles.descriptors.CommonExtensionDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.SensorDescriptor;
import org.jactr.eclipse.core.bundles.registry.SensorRegistry;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.eclipse.runtime.ui.tabs.CommonExtensionDescriptorTab;

public class RealityTab extends CommonExtensionDescriptorTab
{

  private Text   _timeoutText;

  private Button _disconnectButton;

  private Button _useEmbed;

  public RealityTab()
  {
    setTabDescription("CommonReality interfaces allow you to plug in different perceptual/motor control bridges to other devices. These are required for P/M modules.");
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
      public String getText(Object element)
      {
        return element.toString();
      }
    };
  }

  /**
   * to provide a checkbox for using embed connector
   */
  @Override
  protected void createHeader(Composite container)
  {
    Composite group = new Composite(container, SWT.BORDER);
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setLayout(new GridLayout(3, false));

    _useEmbed = new Button(group, SWT.CHECK);
    GridData gd = new GridData();
    gd.horizontalSpan = 1;
    gd.horizontalAlignment = SWT.CENTER;
    _useEmbed.setLayoutData(gd);

    Label label = new Label(group, SWT.LEFT);
    label.setText("Use embedded connector and ThinAgents for perception.");
    gd = new GridData();
    gd.horizontalSpan = 2;
    gd.horizontalAlignment = SWT.BEGINNING;
    gd.grabExcessHorizontalSpace = true;
    label.setLayoutData(gd);

    _useEmbed.addSelectionListener(new SelectionListener() {

      public void widgetDefaultSelected(SelectionEvent e)
      {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e)
      {
        dirty();
        _descriptorList.getTable().setEnabled(!_useEmbed.getSelection());
      }

    });

    createVerticalSpacer(container, 1);
  }

  @Override
  protected void createFooter(Composite container)
  {
    createVerticalSpacer(container, 1);

    Composite group = new Composite(container, SWT.BORDER);
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setLayout(new GridLayout(2, true));

    Label label = new Label(group, SWT.LEFT);
    label.setText("Acknowledgement time (ms)");
    GridData gd = new GridData();
    gd.horizontalSpan = 1;
    label.setLayoutData(gd);

    _timeoutText = new Text(group, SWT.RIGHT);
    _timeoutText.addModifyListener(new ModifyListener() {

      public void modifyText(ModifyEvent e)
      {
        dirty();
      }

    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    _timeoutText.setLayoutData(gd);

    _disconnectButton = new Button(group, SWT.CHECK);
    _disconnectButton.setText("Shutdown on timeout");
    gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    _disconnectButton.setLayoutData(gd);

    _disconnectButton.addSelectionListener(new SelectionListener() {

      public void widgetDefaultSelected(SelectionEvent e)
      {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e)
      {
        dirty();
      }

    });

  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy config)
  {
    super.setDefaults(config);
    config.setAttribute(ACTRLaunchConstants.ATTR_USE_EMBED_CONTROLLER, false);
    config.setAttribute(ACTRLaunchConstants.ATTR_COMMON_REALITY_ACK_TIME, 2000);
    config.setAttribute(ACTRLaunchConstants.ATTR_COMMON_REALITY_DISCONNECT,
        false);
  }

  @Override
  public void initializeFrom(ILaunchConfiguration config)
  {
    super.initializeFrom(config);
    try
    {
      _timeoutText.setText(""
          + config.getAttribute(
              ACTRLaunchConstants.ATTR_COMMON_REALITY_ACK_TIME, 2000));
      _disconnectButton.setSelection(config.getAttribute(
          ACTRLaunchConstants.ATTR_COMMON_REALITY_DISCONNECT, false));
      _useEmbed.setSelection(config.getAttribute(
          ACTRLaunchConstants.ATTR_USE_EMBED_CONTROLLER, false));

      _descriptorList.getTable().setEnabled(!_useEmbed.getSelection());
    }
    catch (CoreException ce)
    {
      CorePlugin.error("Could not initialize launch config", ce);
    }
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy config)
  {
    super.performApply(config);
    config.setAttribute(ACTRLaunchConstants.ATTR_COMMON_REALITY_ACK_TIME,
        Integer.parseInt(_timeoutText.getText()));
    config.setAttribute(ACTRLaunchConstants.ATTR_COMMON_REALITY_DISCONNECT,
        _disconnectButton.getSelection());
    config.setAttribute(ACTRLaunchConstants.ATTR_USE_EMBED_CONTROLLER,
        _useEmbed.getSelection());
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig)
  {
    boolean rtn = super.isValid(launchConfig);
    if (!rtn) return false;

    try
    {
      Integer.parseInt(_timeoutText.getText());
    }
    catch (Exception e)
    {
      setErrorMessage("Acknowledgement time must be milliseconds >= 50");
      return false;
    }

    /*
     * does the model meet common reality requirements?
     */
    try
    {
      ACTRLaunchConfigurationUtils.meetsCommonRealityRequirements(launchConfig);
    }
    catch (Exception e)
    {
      setErrorMessage(e.getMessage());
      RuntimePlugin.error("Failed to check CR reqs", e);
      return false;
    }

    return true;
  }

  @Override
  protected String getDescriptorAttributeName()
  {
    return ACTRLaunchConstants.ATTR_COMMON_REALITY_SENSORS;
  }

  @Override
  protected Map<String, String> getParameters(
      CommonExtensionDescriptor descriptor)
  {
    return ((SensorDescriptor) descriptor).getProperties();
  }

  @Override
  protected Collection<? extends CommonExtensionDescriptor> getRequiredDescriptors(
      ILaunchConfiguration config) throws CoreException
  {
    try
    {
      return ACTRLaunchConfigurationUtils.getRequiredSensors(config);
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
    {
      Collection<? extends CommonExtensionDescriptor> descs = SensorRegistry
          .getRegistry().getDescriptors(project, true);

      return descs;
    }
    return SensorRegistry.getRegistry().getAllDescriptors();
  }
}