/*
 * Created on Jun 14, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.runtime.launching.norm;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.pde.ui.launcher.EclipseApplicationLaunchConfiguration;
import org.eclipse.swt.widgets.Display;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.launching.env.EnvironmentConfigurator;
import org.jactr.eclipse.runtime.launching.session.AbstractSession;
import org.jactr.eclipse.runtime.launching.session.SessionTracker;
import org.jactr.eclipse.runtime.launching.support.ACTRProjectVariableResolver;

public abstract class AbstractACTRLaunchConfiguration extends
    EclipseApplicationLaunchConfiguration
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractACTRLaunchConfiguration.class);

  
  private AbstractSession _session;
  
  
  public AbstractSession getSession()
  {
    return _session;
  }
  
  /**
   * if defered execution is supported, return the tracker to use, otherwise,
   * null
   * 
   * @return
   */
  abstract protected SessionTracker getSessionTracker();

  abstract protected String getLaunchPrefix();

  /**
   * actually create and start the session to track the running model
   * 
   * @param launch
   * @param configuration
   * @param mode
   * @return
   * @throws CoreException
   */
  abstract protected AbstractSession startSession(ILaunch launch,
      ILaunchConfigurationWorkingCopy configuration, String mode)
      throws CoreException;

  protected void verify(ILaunchConfiguration configuration)
      throws CoreException
  {
    /*
     * make sure the models are clean
     */
    for (IResource modelFile : ACTRLaunchConfigurationUtils
        .getModelFiles(configuration))
    {
      IMarker[] markers = modelFile.findMarkers(IMarker.PROBLEM, true,
          IResource.DEPTH_INFINITE);
      for (IMarker marker : markers)
        if (IMarker.SEVERITY_ERROR == marker.getAttribute(IMarker.SEVERITY,
            IMarker.SEVERITY_INFO))
          throw new CoreException(new Status(IStatus.ERROR,
              RuntimePlugin.PLUGIN_ID, 0, "Could not launch runtime because " +
                  modelFile.getName() + " has compilation errors", null));
    }

    ACTRLaunchConfigurationUtils.meetsCommonRealityRequirements(configuration);
  }

  protected ILaunchConfigurationWorkingCopy createActualConfiguration(
      ILaunchConfiguration configuration, String mode, IFile envFile,
      IProgressMonitor monitor) throws CoreException
  {

    String name = configuration.getName()+"-actual";

    ILaunchConfigurationWorkingCopy workingCopy = configuration.copy(name);
    workingCopy.setContainer(null);

    ACTRLaunchConfigurationUtils.setupPermanentAttributes(workingCopy);

    ACTRLaunchConfigurationUtils.setupPersistentAttributes(workingCopy);
    // workingCopy.doSave();

    /*
     * make sure everything is AOK to run. we do all the bundle selection and
     * launch configuration set up here based on the settings in the actual
     * working copy
     */
    ACTRLaunchConfigurationUtils.setupTemporaryAttributes(workingCopy, mode,
        envFile);

//    configuration = workingCopy.doSave();

    return workingCopy;
  }

  protected AbstractSession createSession(
      ILaunchConfigurationWorkingCopy workingCopy, ILaunch launch, String mode,
      IFile environmentFile, IProgressMonitor monitor) throws CoreException
  {
    /*
     * start up session. once the session is started, we can actually write the
     * environment file
     */
    AbstractSession session = startSession(launch, workingCopy, mode);
    
    if (session != null)
    {
    session.setRelativeWorkingDirectory(environmentFile.getProjectRelativePath().removeLastSegments(1));
    session.setAbsoluteWorkingDirectory(environmentFile.getLocation()
        .removeLastSegments(1));
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Launch Config : " + workingCopy.getAttributes());

    /*
     * and write the environment file
     */
    EnvironmentConfigurator.writeEnvironmentConfiguration(environmentFile,
        workingCopy, mode, monitor);

    return session;
  }

  @Override
  public void launch(ILaunchConfiguration configuration, String mode,
      ILaunch launch, IProgressMonitor monitor) throws CoreException
  {
    _session = null;
    boolean shouldCleanUp = false;

    verify(configuration);

    SessionTracker tracker = getSessionTracker();
    if (tracker != null && tracker.hasActiveSessions())
    {
      /*
       * prompt the user. should we defer?
       */
      FutureTask<Integer> response = new FutureTask<Integer>(
          new Callable<Integer>() {
            public Integer call()
            {
              String[] buttons = new String[] { "Cancel", "Run Now",
                  "Run Later" };
              MessageDialog prompt = new MessageDialog(Display.getDefault()
                  .getActiveShell(), "Run when?", null,
                  "A session is already running. Run now or queue for later?",
                  MessageDialog.QUESTION, buttons, 2);

              return prompt.open();
            }
          });

      Display.getDefault().syncExec(response);

      try
      {
        switch (response.get())
        {
          case 0:
            return;
          case 2:
            tracker.addDeferredLaunch(configuration, mode);
            return;
        }
      }
      catch (Exception e)
      {
        throw new CoreException(new Status(IStatus.ERROR,
            RuntimePlugin.PLUGIN_ID, 0, "Could not launch runtime", e));
      }
    }

    ILaunchConfigurationWorkingCopy workingCopy = null;
    try
    {

      /*
       * this is needed to correctly resolve the data and config directories..
       */
      ACTRProjectVariableResolver
          .setCurrentProject(ACTRLaunchConfigurationUtils
              .getProject(configuration));

      /*
       * we create the reference to the configuration file, which is needed in
       * order to set working directories and commandline parameters for the run
       */
      IFile envFile = EnvironmentConfigurator.createRuntimeEnvironmentFile(
          configuration, mode, monitor);

      workingCopy = createActualConfiguration(configuration, mode, envFile,
          monitor);

      _session = createSession(workingCopy, launch, mode, envFile, monitor);
    }
    catch (CoreException ce)
    {
      throw ce;
    }

    try
    {
      configuration = workingCopy.doSave();
      
      super.launch(configuration, mode, launch, monitor);

      shouldCleanUp = monitor.isCanceled();
      
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Launched shouldCleanUp:" + shouldCleanUp);
    }
    catch (CoreException ce)
    {
      shouldCleanUp = true;
      throw ce;
    }
    finally
    {
      if (_session != null && shouldCleanUp) stopSession(_session);
      
      try
      {
        configuration.delete();
      }
      catch (CoreException ce)
      {
        CorePlugin.error("Could not delete temporary launch config", ce);
      }
    }
  }

  protected void stopSession(AbstractSession session) throws CoreException
  {
    session.stop();
  }

}
