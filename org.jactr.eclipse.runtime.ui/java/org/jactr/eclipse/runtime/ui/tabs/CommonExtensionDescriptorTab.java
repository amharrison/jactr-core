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

package org.jactr.eclipse.runtime.ui.tabs;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledPageBook;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.bundles.descriptors.CommonExtensionDescriptor;
import org.jactr.eclipse.core.project.ACTRProjectUtils;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.eclipse.runtime.ui.UIPlugin;
import org.jactr.eclipse.runtime.ui.tabs.normal.GeneralParameterPage;
import org.jactr.eclipse.ui.messages.JACTRMessages;

public abstract class CommonExtensionDescriptorTab extends
    AbstractLaunchConfigurationTab
{

  /**
   * Logger definition
   */

  static private final transient Log              LOGGER           = LogFactory
                                                                       .getLog(CommonExtensionDescriptorTab.class);

  protected CheckboxTableViewer                   _descriptorList;

  private Hashtable<String, GeneralParameterPage> _propertySources = new Hashtable<String, GeneralParameterPage>();

  private FormToolkit                             _toolkit;

  private ScrolledPageBook                        _pageBook;

  private Label                                   _label;

  private Text                                    _description;

  private Map<String, CommonExtensionDescriptor>  _descriptorMap;
  
  private String                                  _tabDescription;

  // private Button _sharedClockButton;
  //
  // private Button _realtimeClockButton;

  public CommonExtensionDescriptorTab()
  {
  }

  public void createControl(Composite parent)
  {
    Composite container = new Composite(parent, SWT.NULL);
    container.setLayout(new GridLayout());
    setControl(container);

    createHeader(container);

    Group interfaceGroup = new Group(container, SWT.BORDER);
    interfaceGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    interfaceGroup.setLayout(new GridLayout());
    createSashSection(interfaceGroup);

    Group descriptionGroup = new Group(container, SWT.BORDER);
    descriptionGroup.setLayout(new GridLayout());
    descriptionGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

    _description = new Text(descriptionGroup, SWT.WRAP | SWT.READ_ONLY
        | SWT.MULTI | SWT.VERTICAL);
    _description.setLayoutData(new GridData(GridData.FILL_BOTH));

    createFooter(container);
  }

  protected void createFooter(Composite container)
  {

  }

  protected void createHeader(Composite container)
  {

  }

  private void createSashSection(Composite container)
  {
    SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
    sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
    createPluginViewer(sashForm);
    createPropertySheetClient(sashForm);
  }

  abstract protected IContentProvider createContentProvider();

  abstract protected ILabelProvider createLabelProvider();

  private void createPluginViewer(Composite sashForm)
  {
    Composite composite = new Composite(sashForm, SWT.NULL);
    GridLayout layout = new GridLayout();
    layout.marginWidth = layout.marginHeight = 0;
    composite.setLayout(layout);
    Label label = new Label(composite, SWT.NULL);

    label.setText(JACTRMessages.getString(getClass().getName() + ".label")); //$NON-NLS-1$

    _descriptorList = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
    _descriptorList.setContentProvider(createContentProvider());
    _descriptorList.setLabelProvider(createLabelProvider());

    _descriptorList
        .addSelectionChangedListener(new ISelectionChangedListener() {

          public void selectionChanged(SelectionChangedEvent e)
          {
            descriptorSelected(getSelectedDescriptor());
          }
        });

    _descriptorList.addCheckStateListener(new ICheckStateListener() {

      public void checkStateChanged(CheckStateChangedEvent event)
      {
        dirty();
      }
    });

    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.widthHint = 125;
    gd.heightHint = 100;
    _descriptorList.getTable().setLayoutData(gd);

  }

  private void createPropertySheetClient(Composite sashForm)
  {
    Composite tableChild = new Composite(sashForm, SWT.NULL);
    GridLayout layout = new GridLayout();
    tableChild.setLayout(layout);
    _label = new Label(tableChild, SWT.NULL);
    _label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    _label.setText(JACTRMessages.getString(getClass().getName() + ".options"));
    int margin = createPropertySheet(tableChild);
    layout.marginWidth = layout.marginHeight = margin;
  }

  protected int createPropertySheet(Composite parent)
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

  @Override
  public void dispose()
  {
    if (_pageBook != null) _pageBook.dispose();
    _pageBook = null;

    if (_propertySources != null)
      try
      {
        disposePropertySources();
        _propertySources = null;
      }
      catch (Exception e)
      {
        LOGGER.error("Failed to dispose of instruments tab", e);
        UIPlugin.getDefault().getLog().log(
            new Status(IStatus.ERROR, UIPlugin.class.getName(),
                "Failed to dispose of instruments tab ", e));
      }

    if (_toolkit != null) _toolkit.dispose();
    _toolkit = null;
    super.dispose();
  }

  public FormToolkit getToolkit()
  {
    return _toolkit;
  }

  public void dirty()
  {
    setDirty(true);
    updateLaunchConfigurationDialog();
  }

  protected void setDescriptorData(IProject project)
  {
    Map<String, CommonExtensionDescriptor> map = new TreeMap<String, CommonExtensionDescriptor>();

    for (CommonExtensionDescriptor instrument : getAvailableDescriptors(project))
      map.put(instrument.getName(), instrument);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(project + " has descriptor data " + map);

    _descriptorMap = map;

    _descriptorList.setInput(map.keySet().toArray(new String[map.size()]));
  }

  /**
   * return all the extension descriptors availble to the project (in
   * classpath), or all available to the system if null
   * 
   * @param project
   * @return
   */
  abstract protected Collection<? extends CommonExtensionDescriptor> getAvailableDescriptors(
      IProject project);

  abstract protected Map<String, String> getParameters(
      CommonExtensionDescriptor descriptor);

  private GeneralParameterPage getPropertySource(
      CommonExtensionDescriptor descriptor)
  {
    if (descriptor == null) return null;
    GeneralParameterPage source = _propertySources.get(descriptor.getName());
    if (source == null)
    {
      source = new GeneralParameterPage(descriptor.getName(),
          getParameters(descriptor), getToolkit(), this);
      _propertySources.put(descriptor.getName(), source);
    }

    return source;
  }

  private void disposePropertySources()
  {
    if (_pageBook == null || _propertySources == null) return;

    for (Enumeration<GeneralParameterPage> en = _propertySources.elements(); en
        .hasMoreElements();)
    {
      GeneralParameterPage source = en.nextElement();
      _pageBook.removePage(source.getName());
    }
    _propertySources.clear();
  }

  protected void setProject(IProject project)
  {
    if (project == null || !ACTRProjectUtils.isACTRProject(project))
    {
      setErrorMessage("Project must be an ACT-R Project");
      project = null;
    }

    setDescriptorData(project);
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig)
  {
    setErrorMessage(null);
    setMessage(_tabDescription);
    return true;
  }
  
  public void setTabDescription(String description)
  {
    _tabDescription = description;
  }

  public void initializeFrom(ILaunchConfiguration config)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Initializing :" + config);
    disposePropertySources();

    /*
     * we need to get the project
     */
    IProject project = null;
    try
    {
      project = ACTRLaunchConfigurationUtils.getProject(config);
    }
    catch (Exception e)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Could not get project, using null");
    }

    setProject(project);
    _descriptorList.setAllChecked(false);

    try
    {
      Collection<? extends CommonExtensionDescriptor> required = getRequiredDescriptors(config);

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(project + " requires " + required);

      for (CommonExtensionDescriptor instrument : required)
      {
        _descriptorList.setChecked(instrument.getName(), true);
        GeneralParameterPage pp = getPropertySource(instrument);
        Map<String, String> parameters = config.getAttribute(
            ACTRLaunchConstants.ATTR_PARAMETERS + instrument.getClassName(),
            Collections.EMPTY_MAP);

        for (Map.Entry<String, String> param : parameters.entrySet())
          pp.setParameter(param.getKey(), param.getValue());
        //
        // for (String parameterName : config
        // .getAttribute(
        // ACTRLaunchConstants.ATTR_PARAMETERS + instrument.getClassName(),
        // "").split(","))
        // {
        // parameterName = parameterName.trim();
        // if (parameterName.length() == 0) continue;
        //
        // String parameterValue = config.getAttribute(
        // ACTRLaunchConstants.ATTR_PARAMETER_VALUE +
        // instrument.getClassName() + "." + parameterName, "");
        // parameterValue = parameterValue.trim();
        //
        // pp.setParameter(parameterName, parameterValue);
        // }
      }
    }
    catch (CoreException ce)
    {
      CorePlugin.error("Failed to initialize configuration", ce);
    }
  }

  /**
   * return the descriptors required by this config, or none if no project could
   * be found (default)
   * 
   * @param config
   * @return
   * @throws CoreException
   */
  abstract protected Collection<? extends CommonExtensionDescriptor> getRequiredDescriptors(
      ILaunchConfiguration config) throws CoreException;

  public void performApply(ILaunchConfigurationWorkingCopy config)
  {
    Object[] checked = _descriptorList.getCheckedElements();

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(checked.length + " descriptors selected");

    StringBuilder descriptors = new StringBuilder();
    for (Object obj : checked)
    {
      CommonExtensionDescriptor descriptor = _descriptorMap.get(obj);

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(descriptor.getName() + " was selected");

      String className = descriptor.getClassName();
      descriptors.append(className).append(",");
      GeneralParameterPage pp = getPropertySource(descriptor);
      Map<String, String> paramMap = new TreeMap<String, String>();

      if (pp != null) paramMap.putAll(pp.getParameterMap());

      // StringBuilder parameterNames = new StringBuilder();
      // for (Map.Entry<String, String> entry : paramMap.entrySet())
      // {
      // String pName = entry.getKey().trim();
      // String pValue = entry.getValue().trim();
      //
      // if (LOGGER.isDebugEnabled())
      // LOGGER.debug("Storing " + pName + "=" + pValue + " for " +
      // className);
      //
      // if (pName.length() == 0 || pValue.length() == 0)
      // {
      // config.setAttribute(ACTRLaunchConstants.ATTR_PARAMETER_VALUE +
      // className + "." + pName, (String) null);
      // continue;
      // }
      //
      // parameterNames.append(pName).append(",");
      // config.setAttribute(ACTRLaunchConstants.ATTR_PARAMETER_VALUE +
      // className + "." + pName, pValue);
      // }

      // if (parameterNames.length() > 0)
      // parameterNames.delete(parameterNames.length() - 1, parameterNames
      // .length());
      //
      // config.setAttribute(ACTRLaunchConstants.ATTR_PARAMETERS + className,
      // parameterNames.toString());

      config.setAttribute(ACTRLaunchConstants.ATTR_PARAMETERS + className,
          paramMap);
    }

    if (descriptors.length() > 0)
      descriptors.delete(descriptors.length() - 1, descriptors.length());

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Selected descriptors : " + descriptors);

    config.setAttribute(getDescriptorAttributeName(), descriptors.toString());
  }

  /**
   * the name of the attribute that the list of descriptors will be stored under
   * 
   * @return
   */
  abstract protected String getDescriptorAttributeName();

  public void setDefaults(ILaunchConfigurationWorkingCopy config)
  {

  }

  private void descriptorSelected(CommonExtensionDescriptor instrument)
  {
    if (instrument == null) return;
    GeneralParameterPage source = getPropertySource(instrument);
    if (source == null)
      _pageBook.showEmptyPage();
    else
    {
      if (!_pageBook.hasPage(instrument.getName()))
      {
        Composite parent = _pageBook.createPage(instrument.getName());
        source.createContents(parent);
      }
      _pageBook.showPage(instrument.getName());
    }

    String description = instrument.getDescription();
    if (description == null) description = "";
    _description.setText(description);
  }

  public String getName()
  {
    return JACTRMessages.getString(getClass().getName() + ".name"); //$NON-NLS-1$
  }

  private CommonExtensionDescriptor getSelectedDescriptor()
  {
    Object item = ((IStructuredSelection) _descriptorList.getSelection())
        .getFirstElement();
    if (item instanceof String) return _descriptorMap.get(item);
    return null;
  }

}