/*
 * Created on Apr 12, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.eclipse.ui.images.JACTRImages;

public class IterativeMainTab extends AbstractLaunchConfigurationTab
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(IterativeMainTab.class);

  private Text                       _iterations;

  private TreeViewer                 _actrConfigurations;

  private Text                       _deadlock;

  private boolean                    _inSetup = true;

  public void createControl(Composite parent)
  {
    Composite projComp = new Composite(parent, SWT.NONE);
    GridLayout projLayout = new GridLayout();
    projComp.setLayout(projLayout);
    setControl(projComp);

    createVerticalSpacer(projComp, 1);

    Group configWrapper = new Group(projComp, SWT.BORDER);
    projLayout = new GridLayout();
    projLayout.numColumns = 1;
    configWrapper.setLayout(projLayout);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    configWrapper.setLayoutData(gd);

    Label confLabel = new Label(configWrapper, SWT.NONE);
    confLabel.setText("jACT-R Run Configuration");
    gd = new GridData();
    gd.horizontalSpan = 2;
    confLabel.setLayoutData(gd);

    _actrConfigurations = new TreeViewer(configWrapper, SWT.VERTICAL);
    _actrConfigurations.setContentProvider(new ITreeContentProvider() {

      private ILaunchConfiguration[] configurations;

      public Object[] getChildren(Object parentElement)
      {
        return null;
      }

      public Object getParent(Object element)
      {
        return configurations;
      }

      public boolean hasChildren(Object element)
      {
        return false;
      }

      public Object[] getElements(Object inputElement)
      {
        return configurations;
      }

      public void dispose()
      {
        // TODO Auto-generated method stub

      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
      {
        configurations = (ILaunchConfiguration[]) newInput;

      }

    });
    _actrConfigurations.setLabelProvider(new LabelProvider() {

      @Override
      public Image getImage(Object element)
      {
        Image img = JACTRImages.getImage(JACTRImages.RUN);
        ;
        return img;
      }

      @Override
      public String getText(Object element)
      {
        String label = element == null ? "" : ((ILaunchConfiguration) element).getName();//$NON-NLS-1$
        return label;
      }
    });
    _actrConfigurations
        .addSelectionChangedListener(new ISelectionChangedListener() {

          public void selectionChanged(SelectionChangedEvent event)
          {
            if (_inSetup) return;
            setDirty(true);
            updateLaunchConfigurationDialog();
          }

        });
    gd = new GridData(GridData.FILL_BOTH);
    gd.widthHint = 125;
    gd.heightHint = 100;
    _actrConfigurations.getTree().setLayoutData(gd);

    createVerticalSpacer(projComp, 1);
    createSeparator(projComp, 1);
    createVerticalSpacer(projComp, 1);

    Group itrWrapper = new Group(projComp, SWT.BORDER);
    projLayout = new GridLayout();
    projLayout.numColumns = 2;
    itrWrapper.setLayout(projLayout);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    itrWrapper.setLayoutData(gd);

    Label itrLabel = new Label(itrWrapper, SWT.NONE);
    itrLabel.setText("Number of Iterations");
    gd = new GridData();
    gd.horizontalSpan = 2;
    itrLabel.setLayoutData(gd);

    _iterations = new Text(itrWrapper, SWT.SINGLE | SWT.BORDER);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    _iterations.setLayoutData(gd);
    _iterations.addModifyListener(new ModifyListener() {

      public void modifyText(ModifyEvent e)
      {
        if (_inSetup) return;
        setDirty(true);
        updateLaunchConfigurationDialog();
      }

    });

    Label deadlockLabel = new Label(itrWrapper, SWT.NONE);
    deadlockLabel.setText("Deadlock detection time (ms)");
    gd = new GridData();
    gd.horizontalSpan = 2;
    deadlockLabel.setLayoutData(gd);

    _deadlock = new Text(itrWrapper, SWT.SINGLE | SWT.BORDER);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    _deadlock.setLayoutData(gd);
    _deadlock.addModifyListener(new ModifyListener() {

      public void modifyText(ModifyEvent e)
      {
        if (_inSetup) return;
        setDirty(true);
        updateLaunchConfigurationDialog();
      }

    });

    setConfigurations();
  }

  public String getName()
  {
    return "Main";
  }

  protected void setConfigurations()
  {
    try
    {
      /*
       * first up, snag all the viable configurations
       */
      ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
      ILaunchConfigurationType type = manager
          .getLaunchConfigurationType(ACTRSession.LAUNCH_TYPE);

      ILaunchConfiguration[] configurations = manager
          .getLaunchConfigurations(type);

      _actrConfigurations.setInput(configurations);
    }
    catch (Exception e)
    {
      LOGGER.error("Could not get configurations ", e);
    }
  }

  protected ILaunchConfiguration getConfiguration(String configurationMemento)
  {
    if (configurationMemento == null || configurationMemento.length() == 0)
      return null;
    try
    {
      /*
       * first up, snag all the viable configurations
       */
      ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
      return manager.getLaunchConfiguration(configurationMemento);
    }
    catch (Exception e)
    {
      LOGGER.error("Could not get configuration " + configurationMemento, e);
      return null;
    }
  }

  public void initializeFrom(ILaunchConfiguration configuration)
  {
    try
    {
      String confMemento = configuration.getAttribute(
          ACTRLaunchConstants.ATTR_SOURCE_CONFIG, "");

      ILaunchConfiguration conf = getConfiguration(confMemento);

      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("Initializing :" + confMemento);
        LOGGER.debug("Configuration : " + conf);
        LOGGER.debug("Attributs : "
            + (conf != null ? conf.getAttributes() : ""));
      }

      if (conf != null)
        _actrConfigurations.setSelection(new StructuredSelection(conf));

      // if (confName.length() != 0)
      // _actrConfigurations.getList().setSelection(new String[] { confName });

      int itr = configuration.getAttribute(ACTRLaunchConstants.ATTR_ITERATIONS,
          1);
      _iterations.setText("" + itr);

      _deadlock.setText(""
          + configuration.getAttribute(
              ACTRLaunchConstants.ATTR_ITERATIVE_DEADLOCK_TIMEOUT, 10000));

      _inSetup = false;
    }
    catch (Exception e)
    {
      LOGGER.error("Could not restore configuration ", e);
    }
  }

  public ILaunchConfiguration getConfiguration()
  {
    IStructuredSelection selection = (IStructuredSelection) _actrConfigurations
        .getSelection();
    if (selection != null && selection.getFirstElement() != null) return (ILaunchConfiguration) selection.getFirstElement();
    return null;
  }

  public int getIterations()
  {
    try
    {
      int itr = Integer.parseInt(_iterations.getText());
      if (itr > 0) return itr;
      return -1;
    }
    catch (NumberFormatException nfe)
    {
      return -1;
    }
  }
  
  public int getDeadlock()
  {
    try
    {
      return Integer.parseInt(_deadlock.getText());
    }
    catch (NumberFormatException nfe)
    {
      return -1;
    }
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig)
  {
    setErrorMessage(null);
    setMessage(null);

    if (getConfiguration() == null)
    {
      setErrorMessage("A valid jACT-R Run configuration must be selected");
      return false;
    }

    if (getIterations() == -1)
    {
      setErrorMessage("Iterations must be >=1");
      return false;
    }
    
    if (getDeadlock() <= 100)
    {
      setErrorMessage("Deadlock must be milliseconds > 100");
      return false;
    }

    return true;
  }

  public void performApply(ILaunchConfigurationWorkingCopy configuration)
  {
    /*
     * snag
     */
    try
    {
      ILaunchConfiguration original = getConfiguration();
      if (original != null)
      {
        Map attrs = original.getAttributes();
        
        /*
         * this theft of the previous attributes can over write the stored
         * listeners.. we also remove the debug parameters so that the
         * iterative ones are used instead
         */
        attrs.remove(ACTRLaunchConstants.ATTR_DEBUG_CORE_ENABLED);
        attrs.remove(ACTRLaunchConstants.ATTR_DEBUG_CORE_LOG_CONF);
        attrs.remove(ACTRLaunchConstants.ATTR_DEBUG_CORE_LOGGER);
        attrs.remove(ACTRLaunchConstants.ATTR_IDE_TRACE);
        
        Map current = configuration.getAttributes();
        current.putAll(attrs);

        configuration.setAttributes(current);
 
        configuration.setAttribute(ACTRLaunchConstants.ATTR_SOURCE_CONFIG,
            original.getMemento());
        configuration.setAttribute(ACTRLaunchConstants.ATTR_ITERATIONS,
            getIterations());
        configuration.setAttribute(
            ACTRLaunchConstants.ATTR_ITERATIVE_DEADLOCK_TIMEOUT, getDeadlock());

        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug("Copying attributes from " + original);
          LOGGER.debug("Attrs : " + current);
        }
      }
      else
        throw new RuntimeException("Must have a valid jACT-R Run configuration");
    }
    catch (Exception e)
    {
      LOGGER.error("Could not copy attributes ", e);
    }
  }

  public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
  {
    // configuration.setAttribute(
    // ACTRLaunchConstants.ATTR_ITERATIVE_DEADLOCK_TIMEOUT, 10000);
  }
}
