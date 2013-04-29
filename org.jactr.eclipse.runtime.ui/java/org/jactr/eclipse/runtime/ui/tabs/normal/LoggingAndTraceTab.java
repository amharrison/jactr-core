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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.eclipse.ui.images.JACTRImages;
import org.jactr.eclipse.ui.messages.JACTRMessages;
import org.jactr.tools.tracer.listeners.BufferTracer;
import org.jactr.tools.tracer.listeners.LogTracer;
import org.jactr.tools.tracer.listeners.ProceduralModuleTracer;

/**
 * @author harrison TODO Flesh out documentation
 */
@Deprecated
public class LoggingAndTraceTab extends AbstractLaunchConfigurationTab
{

  /**
   * Default logger
   */
  static private transient final Log        LOGGER = LogFactory
                                                       .getLog(LoggingAndTraceTab.class);

  private boolean                           _showTracers;

  private Button                            _enableLogging;

  private Text                              _loggerClass;

  private Text                              _logConfigurationFile;

  private Text                              _tracerDescription;

  private CheckboxTableViewer               _tracerList;

  private Collection<IConfigurationElement> _tracers;


  /**
   * 
   */
  public LoggingAndTraceTab(boolean showTracers)
  {
    super();
    _showTracers = showTracers;
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

    if (_showTracers)
    {
      Group tracerWrapper = new Group(projComp, SWT.BORDER);
      projLayout = new GridLayout();
      projLayout.numColumns = 2;
      tracerWrapper.setLayout(projLayout);
      GridData gd = new GridData(GridData.FILL_HORIZONTAL);
      tracerWrapper.setLayoutData(gd);

      buildTracerControls(tracerWrapper);
    }

    Group loggingWrapper = new Group(projComp, SWT.BORDER);
    projLayout = new GridLayout();
    projLayout.numColumns = 2;
    loggingWrapper.setLayout(projLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    loggingWrapper.setLayoutData(gd);

    buildLoggingControls(loggingWrapper);
  }

  protected void buildTracerControls(Composite tracerWrapper)
  {
    _tracers = getInstalledTracers();

    Label label = new Label(tracerWrapper, SWT.NONE);
    label.setText("Runtime Tracers");

    _tracerList = CheckboxTableViewer.newCheckList(tracerWrapper, SWT.BORDER);
    _tracerList.addCheckStateListener(new ICheckStateListener() {

      public void checkStateChanged(CheckStateChangedEvent event)
      {
        setDirty(true);
        updateLaunchConfigurationDialog();
      }

    });
    
    _tracerList.setLabelProvider(new LabelProvider() {

      @Override
      public Image getImage(Object element)
      {
        return JACTRImages.getImage(JACTRImages.TOOL);
      }

      @Override
      public String getText(Object element)
      {
        return ((IConfigurationElement) element).getAttribute("name");
      }
    });

    _tracerList.setContentProvider(new ArrayContentProvider());
    _tracerList.setInput(_tracers.toArray());

    _tracerList.addSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(SelectionChangedEvent event)
      {
        IConfigurationElement selection = (IConfigurationElement) ((IStructuredSelection) event
            .getSelection()).getFirstElement();
        // set text
        if(selection==null) return;
        
        for (IConfigurationElement child : selection.getChildren("description"))
          _tracerDescription.setText(child.getValue());
      }
    });

    _tracerList.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

    _tracerDescription = new Text(tracerWrapper, SWT.WRAP | SWT.READ_ONLY
        | SWT.MULTI | SWT.VERTICAL | SWT.BORDER);
    GridData gd = new GridData(GridData.FILL_BOTH);
    gd.verticalSpan = 1;
    _tracerDescription.setLayoutData(gd);


  }

  protected void buildLoggingControls(Composite loggingWrapper)
  {
    /**
     * first row: X enable logging
     */
    _enableLogging = new Button(loggingWrapper, SWT.CHECK);
    _enableLogging.setText(JACTRMessages.getString(getClass().getName()
        + ".enable"));
    GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
    gd.horizontalSpan = 1;
    _enableLogging.setLayoutData(gd);

    _enableLogging.addSelectionListener(new SelectionListener() {

      public void widgetDefaultSelected(SelectionEvent e)
      {
        widgetSelected(e);
      }

      public void widgetSelected(SelectionEvent e)
      {
        _loggerClass.setEnabled(_enableLogging.getSelection());
        _logConfigurationFile.setEnabled(_enableLogging.getSelection());

        setDirty(true);
        updateLaunchConfigurationDialog();
      }
    });

    /**
     * second row logger name ___________________________
     */

    VerifyListener listener = new VerifyListener() {

      public void verifyText(VerifyEvent e)
      {
        setDirty(true);
        updateLaunchConfigurationDialog();
      }

    };

    Label loggerClass = new Label(loggingWrapper, SWT.NONE);
    loggerClass.setText(JACTRMessages.getString(getClass().getName()
        + ".logger")); //$NON-NLS-1$
    gd = new GridData();
    gd.horizontalSpan = 2;
    loggerClass.setLayoutData(gd);

    _loggerClass = new Text(loggingWrapper, SWT.SINGLE | SWT.BORDER);
    _loggerClass.setText(ACTRLaunchConstants.DEFAULT_CORE_LOGGER);
    _loggerClass.addVerifyListener(listener);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    _loggerClass.setLayoutData(gd);

    /**
     * second row logger name ___________________________
     */

    Label loggerConf = new Label(loggingWrapper, SWT.NONE);
    loggerConf.setText(JACTRMessages
        .getString(getClass().getName() + ".config")); //$NON-NLS-1$
    gd = new GridData();
    gd.horizontalSpan = 2;
    loggerConf.setLayoutData(gd);

    _logConfigurationFile = new Text(loggingWrapper, SWT.SINGLE | SWT.BORDER);
    _logConfigurationFile.setText(ACTRLaunchConstants.DEFAULT_CORE_LOG_CONF);
    _logConfigurationFile.addVerifyListener(listener);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    _logConfigurationFile.setLayoutData(gd);

    _enableLogging.setSelection(false);
    _logConfigurationFile.setEnabled(false);
    _loggerClass.setEnabled(false);
  }

  /**
   * @param configuration
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
  {
    configuration.setAttribute(ACTRLaunchConstants.ATTR_DEBUG_CORE_ENABLED,
        false);
    configuration.setAttribute(ACTRLaunchConstants.ATTR_TRACERS,
        LogTracer.class.getName() + "," + BufferTracer.class.getName() + ","
            + ProceduralModuleTracer.class.getName());
  }

  /**
   * @param configuration
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
   */
  public void initializeFrom(ILaunchConfiguration configuration)
  {
    try
    {
      _enableLogging.setSelection(configuration.getAttribute(
          ACTRLaunchConstants.ATTR_DEBUG_CORE_ENABLED, false));

      _loggerClass.setText(configuration.getAttribute(
          ACTRLaunchConstants.ATTR_DEBUG_CORE_LOGGER,
          ACTRLaunchConstants.DEFAULT_CORE_LOGGER));

      _logConfigurationFile.setText(configuration.getAttribute(
          ACTRLaunchConstants.ATTR_DEBUG_CORE_LOG_CONF,
          ACTRLaunchConstants.DEFAULT_CORE_LOG_CONF));

      _loggerClass.setEnabled(_enableLogging.getSelection());
      _logConfigurationFile.setEnabled(_enableLogging.getSelection());

      if (_showTracers)
      {
        Collection<String> tracers = Arrays.asList(configuration.getAttribute(
            ACTRLaunchConstants.ATTR_TRACERS, LogTracer.class.getName()).split(
            ","));

        for (IConfigurationElement element : _tracers)
          if (tracers.contains(element.getAttribute("class")))
            _tracerList.setChecked(element, true);
      }
    }
    catch (CoreException e)
    {
      _enableLogging.setSelection(false);

      _loggerClass.setText(ACTRLaunchConstants.DEFAULT_CORE_LOGGER);
      _logConfigurationFile.setText(ACTRLaunchConstants.DEFAULT_CORE_LOG_CONF);

      _loggerClass.setEnabled(false);
      _logConfigurationFile.setEnabled(false);
    }
  }

  /**
   * @param configuration
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  public void performApply(ILaunchConfigurationWorkingCopy configuration)
  {
    configuration.setAttribute(ACTRLaunchConstants.ATTR_DEBUG_CORE_ENABLED,
        _enableLogging.getSelection());

    configuration.setAttribute(ACTRLaunchConstants.ATTR_DEBUG_CORE_LOGGER,
        _loggerClass.getText());

    configuration.setAttribute(ACTRLaunchConstants.ATTR_DEBUG_CORE_LOG_CONF,
        _logConfigurationFile.getText());

    if (_showTracers)
    {
      StringBuilder sb = new StringBuilder();
      for (Object obj : _tracerList.getCheckedElements())
        sb.append(((IConfigurationElement) obj).getAttribute("class")).append(
            ",");

      if (sb.length() > 0) sb.delete(sb.length() - 1, sb.length());

      configuration.setAttribute(ACTRLaunchConstants.ATTR_TRACERS, sb
          .toString());
    }
  }

  /**
   * @return
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
   */
  public String getName()
  {
    // TODO Auto-generated method stub
    return JACTRMessages.getString(getClass().getName() + ".name");
  }

  private Collection<IConfigurationElement> getInstalledTracers()
  {
    Collection<IConfigurationElement> tracers = new ArrayList<IConfigurationElement>();
    IExtensionRegistry extReg = Platform.getExtensionRegistry();
    IExtensionPoint iep = extReg.getExtensionPoint("org.jactr.tools.tracers");
    for (IExtension ext : iep.getExtensions())
      for (IConfigurationElement element : ext.getConfigurationElements())
      {
        String className = element.getAttribute("class");

        // hide the procedural debugger
        if (ProceduralModuleTracer.class.getName().equals(className)) continue;

        tracers.add(element);
      }

    return tracers;
  }
}