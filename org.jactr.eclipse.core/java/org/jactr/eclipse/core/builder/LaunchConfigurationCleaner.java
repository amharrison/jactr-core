package org.jactr.eclipse.core.builder;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.jactr.eclipse.core.bundles.registry.ASTParticipantRegistry;
import org.jactr.eclipse.core.bundles.registry.InstrumentRegistry;
import org.jactr.eclipse.core.bundles.registry.IterativeListenerRegistry;
import org.jactr.eclipse.core.bundles.registry.ModuleRegistry;
import org.jactr.eclipse.core.bundles.registry.RuntimeTracerRegistry;
import org.jactr.eclipse.core.bundles.registry.SensorRegistry;
import org.jactr.eclipse.core.bundles.registry.UnitCompilerRegistry;

public class LaunchConfigurationCleaner
{
  
  static public final String NORMAL_CONFIGURATION_LOCATION = "${system_property:user.home}/.jactr/configuration/";
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(LaunchConfigurationCleaner.class);

  static public void clean()
  {
    clean(null);
  }

  static public void clean(IProject project)
  {
    /*
     * flush the registries, just to be complete.
     */
    ASTParticipantRegistry.getRegistry().flush();
    InstrumentRegistry.getRegistry().flush();
    IterativeListenerRegistry.getRegistry().flush();
    ModuleRegistry.getRegistry().flush();
    RuntimeTracerRegistry.getRegistry().flush();
    SensorRegistry.getRegistry().flush();
    UnitCompilerRegistry.getRegistry().flush();

    IPath location = getRunConfigurationRootLocation();
    
    if(project!=null)
      location.append(project.getFullPath());

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Attempting to delete " + location);

    IFileStore store = EFS.getLocalFileSystem().getStore(
        getRunConfigurationRootLocation());

    if (store != null)
      delete(store, null);
    else if (LOGGER.isWarnEnabled())
      LOGGER.warn("Could not find files to delete at " + location);
  }

  static public IPath getRunConfigurationRootLocation()
  {
    String configurationLocation = NORMAL_CONFIGURATION_LOCATION;
    try
    {
      IStringVariableManager mgr = VariablesPlugin.getDefault()
          .getStringVariableManager();
      configurationLocation = mgr.performStringSubstitution(
          configurationLocation, false);
    }
    catch (CoreException ce)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn(
            "Could not substitue variables in " + configurationLocation, ce);
    }
    return new Path(configurationLocation);
  }

  static protected void delete(IFileStore fileOrFolder, IProgressMonitor monitor)
  {
    if (fileOrFolder.fetchInfo().exists())
    {
      if (fileOrFolder.fetchInfo().isDirectory())
        try
        {
          for (IFileStore child : fileOrFolder.childStores(EFS.NONE, monitor))
            delete(child, monitor);
        }
        catch (CoreException ce)
        {
          if (LOGGER.isWarnEnabled())
            LOGGER.warn(
                "Could not fetch children of " + fileOrFolder.getName(), ce);
        }

      /*
       * and now we delete the file/folder itself
       */
      try
      {
        fileOrFolder.delete(EFS.NONE, monitor);
      }
      catch (CoreException ce)
      {
        if (LOGGER.isWarnEnabled())
          LOGGER.warn("Could not delete " + fileOrFolder.getName(), ce);
      }
    }
  }
}
