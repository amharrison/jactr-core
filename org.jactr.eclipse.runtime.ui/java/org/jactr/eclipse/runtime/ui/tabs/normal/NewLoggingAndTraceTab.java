package org.jactr.eclipse.runtime.ui.tabs.normal;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.jactr.eclipse.core.bundles.descriptors.CommonExtensionDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.RuntimeTracerDescriptor;
import org.jactr.eclipse.core.bundles.registry.RuntimeTracerRegistry;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.eclipse.runtime.ui.tabs.CommonExtensionDescriptorTab;
import org.jactr.eclipse.ui.images.JACTRImages;
import org.jactr.eclipse.ui.messages.JACTRMessages;
import org.jactr.tools.tracer.listeners.LogTracer;

public class NewLoggingAndTraceTab extends CommonExtensionDescriptorTab
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(NewLoggingAndTraceTab.class);

  private Button                     _enableLogging;

  private Text                       _loggerClass;

  private Text                       _logConfigurationFile;

  private Button                     _useIDESink;

  private Button                     _useFileSink;

  
  public NewLoggingAndTraceTab()
  {
    setTabDescription("Tracers route information from the model back to the IDE. Additionally, you can enable runtime debugging options.");
  }

  @Override
  protected void createFooter(Composite container)
  {
    createSinkSection(container);

    Group loggingWrapper = new Group(container, SWT.BORDER);
    GridLayout projLayout = new GridLayout();
    projLayout.numColumns = 2;
    loggingWrapper.setLayout(projLayout);

    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    loggingWrapper.setLayoutData(gd);

    buildLoggingControls(container);
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
  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
  {
    configuration.setAttribute(ACTRLaunchConstants.ATTR_DEBUG_CORE_ENABLED,
        false);
    configuration.setAttribute(ACTRLaunchConstants.ATTR_TRACERS,
        LogTracer.class.getName());
    configuration.setAttribute(ACTRLaunchConstants.ATTR_RECORD_TRACE, false);
    configuration.setAttribute(ACTRLaunchConstants.ATTR_IDE_TRACE, true);

    super.setDefaults(configuration);
  }

  /**
   * @param configuration
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
   */
  @Override
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

      _useIDESink.setSelection(configuration.getAttribute(
          ACTRLaunchConstants.ATTR_IDE_TRACE, true));
      _useFileSink.setSelection(configuration.getAttribute(
          ACTRLaunchConstants.ATTR_RECORD_TRACE, false));

    }
    catch (CoreException e)
    {
      _enableLogging.setSelection(false);

      _loggerClass.setText(ACTRLaunchConstants.DEFAULT_CORE_LOGGER);
      _logConfigurationFile.setText(ACTRLaunchConstants.DEFAULT_CORE_LOG_CONF);

      _loggerClass.setEnabled(false);
      _logConfigurationFile.setEnabled(false);

      _useFileSink.setSelection(false);
      _useIDESink.setSelection(true);
    }
    super.initializeFrom(configuration);
  }

  protected void createSinkSection(Composite parent)
  {
    /*
     * sink buttons
     */

    Group sinkGroup = new Group(parent, SWT.BORDER);
    sinkGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    GridLayout projLayout = new GridLayout();
    projLayout.numColumns = 3;
    sinkGroup.setLayout(projLayout);

    Label label = new Label(sinkGroup, SWT.None);
    label.setText("Send data to");
    GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
    gd.horizontalSpan = 1;
    label.setLayoutData(gd);

    _useIDESink = new Button(sinkGroup, SWT.CHECK);
    _useIDESink.setText("IDE");
    gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
    _useIDESink.setLayoutData(gd);

    _useFileSink = new Button(sinkGroup, SWT.CHECK);
    _useFileSink.setText("file");
    gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
    gd.horizontalSpan = 1;
    _useFileSink.setLayoutData(gd);
    
    SelectionListener listener = new SelectionListener() {
      
      public void widgetSelected(SelectionEvent e)
      {
        widgetDefaultSelected(e);
      }
      
      public void widgetDefaultSelected(SelectionEvent e)
      {
       setDirty(true);
       updateLaunchConfigurationDialog();
      }
    };

    _useFileSink.addSelectionListener(listener);
    _useIDESink.addSelectionListener(listener);
  }

  /**
   * @param configuration
   * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
   */
  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration)
  {
    configuration.setAttribute(ACTRLaunchConstants.ATTR_DEBUG_CORE_ENABLED,
        _enableLogging.getSelection());

    configuration.setAttribute(ACTRLaunchConstants.ATTR_DEBUG_CORE_LOGGER,
        _loggerClass.getText());

    configuration.setAttribute(ACTRLaunchConstants.ATTR_DEBUG_CORE_LOG_CONF,
        _logConfigurationFile.getText());

    configuration.setAttribute(ACTRLaunchConstants.ATTR_IDE_TRACE,
        _useIDESink.getSelection());
    configuration.setAttribute(ACTRLaunchConstants.ATTR_RECORD_TRACE,
        _useFileSink.getSelection());

    super.performApply(configuration);
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig)
  {
    boolean rtn = super.isValid(launchConfig);
    if (!rtn) return false;

    try
    {
      if(getRequiredDescriptors(launchConfig).size()==0)
        setErrorMessage("No trace options have been set. This will run, and much faster, but it'll be hard to understand what it's doing.");

    }
    catch (Exception e)
    {
      return false;
    }
    
    return true;
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
      return RuntimeTracerRegistry.getRegistry().getDescriptors(project);
    else
      return RuntimeTracerRegistry.getRegistry().getAllDescriptors();
  }

  @Override
  protected String getDescriptorAttributeName()
  {
    return ACTRLaunchConstants.ATTR_TRACERS;
  }

  @Override
  protected Map<String, String> getParameters(
      CommonExtensionDescriptor descriptor)
  {
    return ((RuntimeTracerDescriptor) descriptor).getParameters();
  }

  @Override
  protected Collection<? extends CommonExtensionDescriptor> getRequiredDescriptors(
      ILaunchConfiguration config) throws CoreException
  {
    try
    {
      return ACTRLaunchConfigurationUtils.getRequiredTracers(config);
    }
    catch (Exception e)
    {
      // return Collections.EMPTY_LIST;

      /*
       * if default, no project will be available causing the above to fail, so
       * we manually enforce the default we set in setDefaults
       */

      ArrayList<RuntimeTracerDescriptor> descriptors = new ArrayList<RuntimeTracerDescriptor>();
      Collection<RuntimeTracerDescriptor> installed = RuntimeTracerRegistry
          .getRegistry().getAllDescriptors();

      String instruments = config.getAttribute(
          ACTRLaunchConstants.ATTR_TRACERS, "");

      for (String instrument : instruments.split(","))
        for (RuntimeTracerDescriptor desc : installed)
          if (desc.getClassName().equals(instrument)) descriptors.add(desc);

      return descriptors;
    }
  }

}
