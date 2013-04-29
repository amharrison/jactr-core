package org.jactr.eclipse.demo.export.ops;

/*
 * default logging
 */
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;

public class UpdateLaunchOperation extends WorkspaceModifyOperation
{
  /**
   * Logger definition
   */
  static private final transient Log      LOGGER = LogFactory
                                                     .getLog(UpdateLaunchOperation.class);

  private ILaunchConfigurationWorkingCopy _configuration;

  public UpdateLaunchOperation(ILaunchConfigurationWorkingCopy configuration)
  {
    _configuration = configuration;
  }

  @Override
  protected void execute(IProgressMonitor monitor) throws CoreException,
      InvocationTargetException, InterruptedException
  {
    if (monitor != null)
      monitor = new SubProgressMonitor(monitor, 3);
    else
      monitor = new NullProgressMonitor();
    try
    {

      monitor.beginTask("Adjusting run configuration", 3);

      ACTRLaunchConfigurationUtils.setupPermanentAttributes(_configuration);
      monitor.worked(1);

      ACTRLaunchConfigurationUtils.setupPersistentAttributes(_configuration);
      monitor.worked(1);
      /*
       * we overwrite the ports and address to prevent connection attempts
       */
      _configuration.setAttribute(ACTRLaunchConstants.ATTR_DEBUG_PORT, -1);
      _configuration.setAttribute(ACTRLaunchConstants.ATTR_DEBUG_ADDRESS,
          (String) null);
      _configuration.setAttribute(ACTRLaunchConstants.ATTR_CREDENTIALS,
          (String) null);
      monitor.worked(1);
    }
    finally
    {
      monitor.done();
    }
  }

}
