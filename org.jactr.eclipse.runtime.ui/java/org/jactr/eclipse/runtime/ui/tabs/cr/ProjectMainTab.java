/**
 * Copyright (C) 2001-3, Anthony Harrison anh23@pitt.edu This library is free
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

/*
 * Created on May 5, 2004 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jactr.eclipse.runtime.ui.tabs.cr;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.eclipse.ui.renders.ACTRProjectLabelProvider;

/**
 * @author harrison To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ProjectMainTab extends AbstractLaunchConfigurationTab
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER             = LogFactory
                                                            .getLog(ProjectMainTab.class);

  // Project UI widgets
  private Label                      _projectLabel;

  private Text                       _projectText;

  private Button                     _projectButton;

  private IProject                   _currentProject;

  private Button                     _enableMockAgent;

  private static final String        EMPTY_STRING       = "";                             //$NON-NLS-1$

  private final ModifyListener       fModifyListener    = new ModifyListener() {

                                                          public void modifyText(
                                                              ModifyEvent e)
                                                          {
                                                            updateLaunchConfigurationDialog();
                                                            setProject(_projectText
                                                                .getText());
                                                          }
                                                        };

  private final SelectionAdapter     fSelectionListener = new SelectionAdapter() {

                                                          @Override
                                                          public void widgetSelected(
                                                              SelectionEvent e)
                                                          {
                                                            Object source = e
                                                                .getSource();
                                                            if (source == _projectButton)
                                                              handleProjectButtonSelected();
                                                            else if (source == _enableMockAgent)
                                                              setDirty(true);
                                                          }
                                                        };

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
   */
  @Override
  public void dispose()
  {

    if (_projectButton != null) _projectButton.dispose();
    _projectButton = null;
    if (_projectLabel != null) _projectLabel.dispose();
    _projectLabel = null;

    if (_projectText != null) _projectText.dispose();
    _projectText = null;

    if (_enableMockAgent != null) _enableMockAgent.dispose();
    _enableMockAgent = null;

    super.dispose();
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
   */
  public void createControl(Composite parent)
  {

    Font font = parent.getFont();

    Composite projComp = new Composite(parent, SWT.NONE);
    setControl(projComp);

    GridLayout projLayout = new GridLayout();
    projLayout.numColumns = 2;
    projComp.setLayout(projLayout);
    projComp.setFont(font);

    createVerticalSpacer(projComp, 2);

    GridData gd;
    _projectLabel = new Label(projComp, SWT.NONE);
    _projectLabel.setText("Project"); //$NON-NLS-1$
    gd = new GridData();
    gd.horizontalSpan = 2;
    _projectLabel.setLayoutData(gd);
    _projectLabel.setFont(font);

    _projectText = new Text(projComp, SWT.SINGLE | SWT.BORDER);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    _projectText.setLayoutData(gd);
    _projectText.setFont(font);
    _projectText.addModifyListener(fModifyListener);

    _projectButton = createPushButton(projComp, "Browse", null); //$NON-NLS-1$
    _projectButton.addSelectionListener(fSelectionListener);

    _enableMockAgent = new Button(projComp, SWT.CHECK);
    _enableMockAgent.setText("Include Mock Agent");
    _enableMockAgent.addSelectionListener(fSelectionListener);
    gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
    gd.horizontalSpan = 1;
    _enableMockAgent.setLayoutData(gd);

  }

  @Override
  public void setDirty(boolean dirty)
  {
    super.setDirty(dirty);
    updateLaunchConfigurationDialog();
  }

  @Override
  public void updateLaunchConfigurationDialog()
  {
    super.updateLaunchConfigurationDialog();
  }



  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration config)
  {
    updateProjectFromConfig(config);
    updateMockFromConfig(config);
  }


  private void updateMockFromConfig(ILaunchConfiguration config)
  {
    boolean includeMock = false;
    try
    {
      includeMock = config.getAttribute(ACTRLaunchConstants.INCLUDE_MOCK_AGENT,
          false);
    }
    catch (CoreException ce)
    {
      CorePlugin.error("Failed to update project from config", ce);
    }

    _enableMockAgent.setSelection(includeMock);
  }

  private void updateProjectFromConfig(ILaunchConfiguration config)
  {
    String projectName = EMPTY_STRING;
    try
    {
      projectName = config.getAttribute(
          IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
    }
    catch (CoreException ce)
    {
      CorePlugin.error("Failed to update project from config", ce);
    }

    setProject(projectName);
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy config)
  {
    if (_currentProject == null) return;

    config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
        _currentProject.getName());

    config.setAttribute(ACTRLaunchConstants.INCLUDE_MOCK_AGENT,
        _enableMockAgent.getSelection());
  }



  /**
   * Show a dialog that lets the user select a project. This in turn provides
   * context for the main type, allowing the user to key a main type name, or
   * constraining the search for main types to the specified project.
   */
  private void handleProjectButtonSelected()
  {
    setProject(chooseProject());
  }

  /**
   * Realize a Java Project selection dialog and return the first selected
   * project, or null if there was none.
   */
  private IProject chooseProject()
  {
    IProject[] projects = getProjects();

// projects = ACTRProjectUtils.getACTRProjects();

    ILabelProvider labelProvider = ACTRProjectLabelProvider.getInstance();
    ElementListSelectionDialog dialog = new ElementListSelectionDialog(
        getShell(), labelProvider);

    dialog.setTitle("Choose a project"); //$NON-NLS-1$
    dialog.setMessage("Select the Project"); //$NON-NLS-1$
    dialog.setElements(projects);

    IProject actrProject = getProject();
    if (actrProject != null)
      dialog.setInitialSelections(new Object[] { actrProject });
    if (dialog.open() == Window.OK) return (IProject) dialog.getFirstResult();
    return null;
  }

  protected void setProject(String projectName)
  {
    for (IProject project : getProjects())
      if (projectName.equals(project.getName()))
      {
        setProject(project);
        return;
      }

    setProject((IProject) null);
  }

  private IProject[] getProjects()
  {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    Collection<IProject> projects = new ArrayList<IProject>();
    for (IProject project : workspaceRoot.getProjects())
      if (project.isAccessible()) projects.add(project);
    return projects.toArray(new IProject[projects.size()]);
  }

  protected void setProject(IProject project)
  {
    if (_currentProject == project) return;


    String projectName = "";
    if (project != null) projectName = project.getName();

    _currentProject = project;

    _projectText.setText(projectName);
  }

  private IProject getProject()
  {
    return _currentProject;
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(ILaunchConfiguration)
   */
  @Override
  public boolean isValid(ILaunchConfiguration launchConfig)
  {
    setErrorMessage(null);
    setMessage("Select the project you wish to execute from.");

    if (getProject() == null)
    {
      setErrorMessage("Project must be specified");
      return false;
    }

    return true;
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName()
  {
    return "Project";
  }

  public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
  {

  }

}