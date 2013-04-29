package org.jactr.eclipse.demo.export.ops;

/*
 * default logging
 */
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.jactr.eclipse.runtime.launching.env.EnvironmentConfigurator;

public class WriteEnvironmentOperation extends WorkspaceModifyOperation
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(WriteEnvironmentOperation.class);

  private ILaunchConfiguration       _configuration;

  private IProject                   _project;

  private IFile                      _environmentFile;

  public WriteEnvironmentOperation(ILaunchConfiguration configuration,
      IProject project)
  {
    _configuration = configuration;
    _project = project;
  }
  
  public IFile getEnvironmentFile()
  {
    return _environmentFile;
  }

  /**
   * make sure the project has the demo diretory
   * configuration/demo/launchName/environment.xml
   */
  protected IFile ensureFile(String folderName, String fileName)
      throws CoreException
  {
    IFolder folder = _project.getFolder(folderName);
    create(folder);
    return folder.getFile(fileName);
  }

  private void create(IFolder folder) throws CoreException
  {
    if (folder.exists()) return;

    if (!folder.getParent().exists()) create((IFolder) folder.getParent());

    folder.create(true, true, null);
  }

  protected IFile writeEnvironment(ILaunchConfiguration configuration)
      throws CoreException
  {
    /*
     * 
     */
    IFile environmentFile = ensureFile("configuration/demo/"
        + configuration.getName(), "environment.xml");

    if (environmentFile.exists()) environmentFile.delete(true, null);

    EnvironmentConfigurator.writeEnvironmentConfiguration(environmentFile,
        configuration, ILaunchManager.RUN_MODE, null);

    return environmentFile;
  }

  @Override
  protected void execute(IProgressMonitor monitor) throws CoreException,
      InvocationTargetException, InterruptedException
  {
    if (monitor != null)
      monitor = new SubProgressMonitor(monitor, 1);
    else
      monitor = new NullProgressMonitor();
    
    try
    {
      monitor.beginTask("Writing environment", 1);
      _environmentFile = writeEnvironment(_configuration);
    }
    finally
    {
      monitor.done();
    }
  }

}
