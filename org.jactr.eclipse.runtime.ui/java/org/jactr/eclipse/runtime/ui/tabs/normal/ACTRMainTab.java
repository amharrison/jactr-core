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
package org.jactr.eclipse.runtime.ui.tabs.normal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.project.ACTRProjectUtils;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.eclipse.ui.images.JACTRImages;
import org.jactr.eclipse.ui.messages.JACTRMessages;
import org.jactr.eclipse.ui.renders.ACTRProjectLabelProvider;

/**
 * @author harrison To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ACTRMainTab extends AbstractLaunchConfigurationTab
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER             = LogFactory
                                                            .getLog(ACTRMainTab.class);

  // Project UI widgets
  private Label                      _projectLabel;

  private Text                       _projectText;

  private Button                     _projectButton;

  private CheckboxTableViewer        _modelViewer;

  private FormToolkit                _toolkit;

  private ScrolledPageBook           _pageBook;

  private Map<IResource, AliasPage>  _aliasSources;

  private Object[]                   _modelFiles;

  private IProject                   _currentProject;

  private Button                     _addButton;

  private Button                     _removeButton;

  private String                     _activeAlias;

  private AliasPage                  _activePage;

  private static final String        EMPTY_STRING       = "";                          //$NON-NLS-1$

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
                                                          }
                                                        };

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
   */
  @Override
  public void dispose()
  {
    if (_toolkit == null) return;

    _toolkit.dispose();
    _toolkit = null;
    _aliasSources.clear();
    _modelViewer.getTable().dispose();
    _removeButton.dispose();
    _addButton.dispose();
    _projectButton.dispose();
    _projectLabel.dispose();
    _projectText.dispose();
    _pageBook.dispose();
    super.dispose();
  }

  private void disposePropertySources()
  {
    for (IResource modelFile : _aliasSources.keySet())
      _pageBook.removePage(modelFile.getName());

    _aliasSources.clear();
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
   */
  public void createControl(Composite parent)
  {
    _aliasSources = new HashMap<IResource, AliasPage>();

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
    _projectLabel.setText("ACT-R Project"); //$NON-NLS-1$
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

    createVerticalSpacer(projComp, 2);
    createSashSection(projComp);
    createVerticalSpacer(projComp, 2);
  }

  private void createSashSection(Composite container)
  {
    SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
    sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
    createModelViewer(sashForm);
    createAliasControl(sashForm);
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

  protected FormToolkit getToolkit()
  {
    return _toolkit;
  }

  private void createAliasControl(Composite sashForm)
  {
    Composite tableChild = new Composite(sashForm, SWT.NULL);
    GridLayout layout = new GridLayout();
    tableChild.setLayout(layout);
    Label label = new Label(tableChild, SWT.NULL);
    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    label.setText(JACTRMessages.getString(getClass().getName() + ".alias"));
    int margin = createAliasTable(tableChild);
    layout.marginWidth = layout.marginHeight = margin;

    Composite buttonGroup = new Composite(sashForm, SWT.NULL);
    buttonGroup.setLayout(new GridLayout());
    buttonGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    _addButton = new Button(buttonGroup, SWT.PUSH);
    _addButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    _addButton.setText(JACTRMessages.getString(getClass().getName()
        + ".alias.add"));
    _addButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e)
      {
        if (_activePage != null)
        {
          String name = getSelectedModel().getName();
          if (name.indexOf(".") != -1)
            name = name.substring(0, name.lastIndexOf("."));
          _activePage.addAlias(name + "-"
              + (_activePage.getNumberOfAliases() + 1));
        }
        updateLaunchConfigurationDialog();
      }
    });
    _addButton.setEnabled(false);

    _removeButton = new Button(buttonGroup, SWT.PUSH);
    _removeButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    _removeButton.setText(JACTRMessages.getString(getClass().getName()
        + ".alias.remove"));
    _removeButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e)
      {
        if (_activePage != null && _activeAlias != null
            && !_activeAlias.equals("self"))
        {
          _activePage.removeAlias(_activeAlias);
          setActiveAlias(_activePage, null);
        }
        updateLaunchConfigurationDialog();
      }
    });
    _removeButton.setEnabled(false);
  }

  protected int createAliasTable(Composite parent)
  {
    _toolkit = new FormToolkit(parent.getDisplay());
    int toolkitBorderStyle = _toolkit.getBorderStyle();
    int style = toolkitBorderStyle == SWT.BORDER ? SWT.NULL : SWT.BORDER;
    _pageBook = new ScrolledPageBook(parent, style | SWT.V_SCROLL
        | SWT.H_SCROLL);
    _toolkit.adapt(_pageBook, false, false);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.heightHint = 100;
    gd.widthHint = 125;
    _pageBook.setLayoutData(gd);
    if (style == SWT.NULL)
    {
      _pageBook.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);
      _toolkit.paintBordersFor(parent);
    }
    return style == SWT.NULL ? 2 : 0;
  }

  private void createModelViewer(Composite sashForm)
  {
    Composite composite = new Composite(sashForm, SWT.NULL);
    GridLayout layout = new GridLayout();
    layout.marginWidth = layout.marginHeight = 0;
    composite.setLayout(layout);
    Label label = new Label(composite, SWT.NULL);

    label.setText(JACTRMessages.getString(getClass().getName() + ".models")); //$NON-NLS-1$

    _modelViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
    _modelViewer.setContentProvider(new ArrayContentProvider());
    _modelViewer.setLabelProvider(new LabelProvider() {

      @Override
      public Image getImage(Object element)
      {
        return JACTRImages.getImage(JACTRImages.MODEL);
      }

      @Override
      public String getText(Object element)
      {
        if (element instanceof IFile) return ((IFile) element).getName();
        return element.toString();
      }
    });

    _modelViewer.addCheckStateListener(new ICheckStateListener() {

      public void checkStateChanged(CheckStateChangedEvent event)
      {
        modelSelectionChanged();
        updateLaunchConfigurationDialog();
      }
    });

    _modelViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(SelectionChangedEvent event)
      {
        modelSelectionChanged();
      }

    });
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.widthHint = 125;
    gd.heightHint = 100;
    _modelViewer.getTable().setLayoutData(gd);
    setModelData(getProject());
  }

  protected void setModelData(IProject project)
  {
    if (project != null)
      _modelFiles = ACTRProjectUtils.getModels(project).toArray();
    else
      _modelFiles = new IResource[0];

    _modelViewer.setInput(_modelFiles);
    disposePropertySources();
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration config)
  {
    updateProjectFromConfig(config);
    updateModelFilesFromConfig(config);
  }

  // @Override
  // public void activated(ILaunchConfigurationWorkingCopy workingCopy)
  // {
  // pageBook.getParent().getParent().layout(true);
  // super.activated(workingCopy);
  // }

  protected void setActiveAlias(AliasPage aliasPage, String alias)
  {
    _activePage = aliasPage;
    _activeAlias = alias;
    if (_activePage.getNumberOfAliases() > 1 && alias != null)
      _removeButton.setEnabled(true);
    else
      _removeButton.setEnabled(false);
  }

  private IResource getSelectedModel()
  {
    return (IResource) ((IStructuredSelection) _modelViewer.getSelection())
        .getFirstElement();
  }

  private void modelSelectionChanged()
  {
    IResource modelFile = getSelectedModel();
    if (modelFile == null)
    {
      _addButton.setEnabled(false);
      _removeButton.setEnabled(false);
    }
    else
      _addButton.setEnabled(true);

    AliasPage source = getAliasSource(modelFile);
    if (source == null /* || !_modelViewer.getChecked(modelFile) */)
      _pageBook.showEmptyPage();
    else
    {
      if (!_pageBook.hasPage(modelFile))
      {
        Composite parent = _pageBook.createPage(modelFile);
        source.createContents(parent);
      }
      _pageBook.showPage(modelFile);
      setActiveAlias(source, null);
    }
  }

  private void updateModelFilesFromConfig(ILaunchConfiguration config)
  {
    if (_currentProject == null) return;

    try
    {
      _modelViewer.setAllChecked(false);
      _modelViewer.setSelection(null);

      for (IResource model : ACTRLaunchConfigurationUtils.getModelFiles(config))
        if (model.exists())
        {
          _modelViewer.setChecked(model, true);
          AliasPage source = getAliasSource(model);
          Collection<String> aliases = ACTRLaunchConfigurationUtils
              .getModelAliases(model, config);
          // clear the deafults
          if (aliases.size() != 0) source.removeAllAliases();
          for (String alias : aliases)
            source.addAlias(alias);
        }
    }
    catch (CoreException ce)
    {
      CorePlugin.error("Failed to update model files from config", ce);
    }
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

    Object[] checked = _modelViewer.getCheckedElements();
    StringBuilder models = new StringBuilder();

    for (Object element : checked)
    {
      IResource modelFile = (IResource) element;
      models.append(modelFile.getProjectRelativePath().toString()).append(",");

      AliasPage source = getAliasSource(modelFile);
      if (source != null)
      {
        StringBuilder aliases = new StringBuilder();

        for (String alias : source.getAliases())
          aliases.append(alias).append(",");

        if (aliases.length() > 0)
          aliases.delete(aliases.length() - 1, aliases.length());

        config.setAttribute(ACTRLaunchConstants.ATTR_MODEL_ALIASES
            + modelFile.getFullPath().toOSString(), aliases.toString());
      }
    }

    if (models.length() > 0)
      models.delete(models.length() - 1, models.length());

    config
        .setAttribute(ACTRLaunchConstants.ATTR_MODEL_FILES, models.toString());
  }

  private AliasPage getAliasSource(IResource modelFile)
  {
    if (modelFile == null) return null;
    AliasPage source = _aliasSources.get(modelFile);
    if (source == null)
    {
      String name = modelFile.getName();
      if (name.indexOf(".") != -1) name = name.substring(0, name.indexOf("."));
      source = new AliasPage(this, name);
      _aliasSources.put(modelFile, source);
    }
    return source;
  }

  /**
   * Show a dialog that lets the user select a project. This in turn provides
   * context for the main type, allowing the user to key a main type name, or
   * constraining the search for main types to the specified project.
   */
  private void handleProjectButtonSelected()
  {
    setProject(chooseACTRProject());
  }

  /**
   * Realize a Java Project selection dialog and return the first selected
   * project, or null if there was none.
   */
  private IProject chooseACTRProject()
  {
    IProject[] projects;

    projects = ACTRProjectUtils.getACTRProjects();

    ILabelProvider labelProvider = ACTRProjectLabelProvider.getInstance();
    ElementListSelectionDialog dialog = new ElementListSelectionDialog(
        getShell(), labelProvider);

    dialog.setTitle("Choose a project"); //$NON-NLS-1$
    dialog.setMessage("Select the ACTR Project"); //$NON-NLS-1$
    dialog.setElements(projects);

    IProject actrProject = getProject();
    if (actrProject != null)
      dialog.setInitialSelections(new Object[] { actrProject });
    if (dialog.open() == Window.OK) return (IProject) dialog.getFirstResult();
    return null;
  }

  protected void setProject(String projectName)
  {
    for (IProject project : ACTRProjectUtils.getACTRProjects())
      if (projectName.equals(project.getName()))
      {
        setProject(project);
        return;
      }
    setProject((IProject) null);
  }

  protected void setProject(IProject project)
  {
    if (!ACTRProjectUtils.isACTRProject(project))
    {
      setErrorMessage("Project must be an ACT-R project");
      project = null;
    }

    if (_currentProject == project) return;

    setModelData(project);

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
    setMessage("Select the project and model you wish to execute. Multiple identical models may be instantiated by adding aliases.");


    if (!ACTRProjectUtils.isACTRProject(getProject()))
    {
      setErrorMessage("Project must be an ACT-R Project");
      return false;
    }

    /*
     * now we make sure some model has been selected
     */
    if (_modelViewer.getCheckedElements().length == 0)
    {
      setErrorMessage("At least one model must be selected");
      return false;
    }

    return true;
  }

  /**
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName()
  {
    return JACTRMessages.getString(getClass().getName() + ".name");
  }

  public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
  {

  }

}