/*
 * Created on Jul 30, 2004 Copyright (C) 2001-4, Anthony Harrison anh23@pitt.edu
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

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.eclipse.ui.messages.JACTRMessages;

/**
 * @author harrison TODO Flesh out documentation
 */
public class StartStopTab extends AbstractLaunchConfigurationTab
{

  /**
   * Default logger
   */
  static private transient final Log LOGGER = LogFactory
                                                .getLog(StartStopTab.class);

  private Text                       _onStartText;

  private Text                       _onStopText;

  /**
   * 
   */
  public StartStopTab()
  {
    super();
  }

  /**
   * @param parent
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl(Composite parent)
  {
    Composite projComp = new Composite(parent, SWT.NONE);
    GridLayout projLayout = new GridLayout();
    projComp.setLayout(projLayout);
    setControl(projComp);

    Group onWrapper = new Group(projComp, SWT.BORDER);
    projLayout = new GridLayout();
    projLayout.numColumns = 2;

    onWrapper.setLayout(projLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    onWrapper.setLayoutData(gd);

    Label onStartLabel = new Label(onWrapper, SWT.NONE);
    onStartLabel.setText(JACTRMessages.getString(getClass().getName()
        + ".onStart")); //$NON-NLS-1$
    gd = new GridData();
    gd.horizontalSpan = 2;
    onStartLabel.setLayoutData(gd);

    ModifyListener ml = new ModifyListener() {

      public void modifyText(ModifyEvent e)
      {
        setDirty(true);
        updateLaunchConfigurationDialog();
      }

    };

    _onStartText = new Text(onWrapper, SWT.SINGLE | SWT.BORDER);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    _onStartText.setLayoutData(gd);
    _onStartText.addModifyListener(ml);

    Label onStopLabel = new Label(onWrapper, SWT.NONE);
    onStopLabel.setText(JACTRMessages.getString(getClass().getName()
        + ".onStop")); //$NON-NLS-1$
    gd = new GridData();
    gd.horizontalSpan = 2;
    onStopLabel.setLayoutData(gd);

    _onStopText = new Text(onWrapper, SWT.SINGLE | SWT.BORDER);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    _onStopText.setLayoutData(gd);
    _onStopText.addModifyListener(ml);
  }

  /**
   * @param configuration
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
  {

  }

  /**
   * @param configuration
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration configuration)
  {
    try
    {
      _onStartText.setText(configuration.getAttribute(
          ACTRLaunchConstants.ATTR_ON_START, ""));
      _onStopText.setText(configuration.getAttribute(
          ACTRLaunchConstants.ATTR_ON_STOP, ""));
    }
    catch (CoreException e)
    {
      _onStartText.setText("");
      _onStopText.setText("");
    }
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig)
  {
    setErrorMessage(null);
    setMessage("These classes extend java.lang.Runnable and are called exactly once at the start or end of the runtime.");

    try
    {
      for (String className : new String[] {
          launchConfig.getAttribute(ACTRLaunchConstants.ATTR_ON_START, ""),
          launchConfig.getAttribute(ACTRLaunchConstants.ATTR_ON_STOP, "") })
        try
        {
          if (className.length() > 0) testClass(className, launchConfig);
        }
        catch (ClassNotFoundException e)
        {
          setErrorMessage(className + " was not found in classpath");
          return false;
        }
        catch (IllegalArgumentException e)
        {
          setErrorMessage(className + " does not implement java.lang.Runnable");
          return false;
        }
    }
    catch (Exception e)
    {
      return false;
    }
    return true;
  }

  private void testClass(String className, ILaunchConfiguration launchConfig)
      throws ClassNotFoundException, IllegalArgumentException
  {
    try
    {
      if (testSystem(className)) return;
    }
    catch (Exception e)
    {
      testProject(className, launchConfig);
    }
  }

  /**
   * is this accessible in the core pacakge?
   * 
   * @param className
   * @return
   */
  private boolean testSystem(String className) throws ClassNotFoundException,
      IllegalArgumentException
  {
    Class clazz = getClass().getClassLoader().loadClass(className);
    if (!Runnable.class.isAssignableFrom(clazz))
      throw new IllegalArgumentException();
    return true;
  }

  private void testProject(String className, ILaunchConfiguration configuration)
      throws ClassNotFoundException, IllegalArgumentException
  {
    try
    {
      IProject project = ACTRLaunchConfigurationUtils.getProject(configuration);
      IJavaProject jProject = JavaCore.create(project);
      IType type = jProject.findType(className);
      if (type == null)
      {
        RuntimePlugin.info("Could not find " + className + " on classpath of "
            + project.getName() + ". Checked : "
            + Arrays.toString(jProject.getResolvedClasspath(false)));
        throw new ClassNotFoundException();
      }
    }
    catch (CoreException ce)
    {
      throw new IllegalArgumentException();
    }
  }

  /**
   * @param configuration
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy configuration)
  {
    configuration.setAttribute(ACTRLaunchConstants.ATTR_ON_START, _onStartText
        .getText());
    configuration.setAttribute(ACTRLaunchConstants.ATTR_ON_STOP, _onStopText
        .getText());
  }

  /**
   * @return @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName()
  {
    return JACTRMessages.getString(getClass().getName() + ".name");
  }
}