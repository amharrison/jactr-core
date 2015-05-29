/*
 * Created on Mar 22, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.runtime.launching;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.core.plugin.TargetPlatform;
import org.eclipse.pde.internal.launching.IPDEConstants;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.bundles.BundleUtilities;
import org.jactr.eclipse.core.bundles.descriptors.InstrumentDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.IterativeListenerDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.ModuleDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.RuntimeTracerDescriptor;
import org.jactr.eclipse.core.bundles.descriptors.SensorDescriptor;
import org.jactr.eclipse.core.bundles.registry.InstrumentRegistry;
import org.jactr.eclipse.core.bundles.registry.IterativeListenerRegistry;
import org.jactr.eclipse.core.bundles.registry.ModuleRegistry;
import org.jactr.eclipse.core.bundles.registry.RuntimeTracerRegistry;
import org.jactr.eclipse.core.bundles.registry.SensorRegistry;
import org.jactr.eclipse.core.comp.CompilationUnitManager;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;

/**
 * builds a valid ILaunchConfiguration for the ACTR launch environment, which
 * currently relies upon the PDE launch set up.
 * 
 * @author developer
 */
public class ACTRLaunchConfigurationUtils
{

  static private final Log LOGGER = LogFactory
                                      .getLog(ACTRLaunchConfigurationUtils.class);

  /**
   * return the project defined in the configuration
   * 
   * @param configuration
   * @return project
   * @throws CoreException
   *           if project doesnt exist
   */
  static public IProject getProject(ILaunchConfiguration configuration)
      throws CoreException
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    String projectName = configuration.getAttribute(
        IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");

    try
    {
      IProject sourceProject = root.getProject(projectName);
      if (sourceProject.exists() && sourceProject.isOpen())
        return sourceProject;
      else
        throw new RuntimeException(
            "Could not get valid project from launchConfig:" + configuration);
    }
    catch (Exception e)
    {
      throw new RuntimeException(
          "Could not get valid project from launchConfig:" + configuration, e);
    }
  }

  /**
   * return all the model files defined in the launch configuration
   * 
   * @param configuration
   * @return
   * @throws CoreException
   *           if none are defined
   */
  static public Collection<IResource> getModelFiles(
      ILaunchConfiguration configuration) throws CoreException
  {
    IProject project = getProject(configuration);
    ArrayList<IResource> resources = new ArrayList<IResource>();

    if (project == null) return resources;

    for (String modelFile : configuration.getAttribute(
        ACTRLaunchConstants.ATTR_MODEL_FILES, "").split(","))
      if (modelFile.length() > 0)
      {
        IResource resource = project.findMember(modelFile, false);
        if (resource != null) resources.add(resource);
      }
    return resources;
  }

  /**
   * checks all the model files in the configuration for modules. if any of the
   * modules requires common reality then a sensor must have been defined. this
   * does not validate that the sensors actually meet the requirements of the
   * modules
   * 
   * @param configuration
   * @return true if it does. throws exception otherwise
   * @throws CoreException
   */
  static public boolean meetsCommonRealityRequirements(
      ILaunchConfiguration configuration) throws CoreException
  {
    boolean hasSensors = getRequiredSensors(configuration).size() != 0
        || configuration.getAttribute(
            ACTRLaunchConstants.ATTR_USE_EMBED_CONTROLLER, false);

    for (IResource modelFile : getModelFiles(configuration))
      for (ModuleDescriptor module : getModulesInModel(modelFile))
        if (module.requiresCommonReality() && !hasSensors)
          throw new RuntimeException(modelFile.getName()
              + " requires CommonReality because of " + module.getName()
              + ", but no sensors are configured.");
    return true;
  }

  /**
   * returns all the modules required in a specific model
   * 
   * @param modelFile
   * @return
   */
  static public Collection<ModuleDescriptor> getModulesInModel(
      IResource modelFile)
  {
    ArrayList<ModuleDescriptor> modules = new ArrayList<ModuleDescriptor>();
    Collection<ModuleDescriptor> allModules = ModuleRegistry.getRegistry()
        .getDescriptors(modelFile.getProject(), true);
    ICompilationUnit compilationUnit = CompilationUnitManager
        .acquire(modelFile);
    try
    {
      CommonTree modelTree = compilationUnit.getModelDescriptor();

      if (modelTree == null)
        throw new IllegalArgumentException(modelFile.getName() + " has errors");

      Map<String, CommonTree> extMap = ASTSupport.getMapOfTrees(modelTree,
          JACTRBuilder.MODULE);

      for (CommonTree extTree : extMap.values())
      {
        String className = ((CommonTree) extTree
            .getFirstChildWithType(JACTRBuilder.CLASS_SPEC)).getText();

        for (ModuleDescriptor module : allModules)
          if (module.getClassName().equals(className))
          {
            modules.add(module);
            break;
          }

        /*
         * we might be tempted to throw an exception if there is an unresolved
         * class, but we hold that for the runtime itself
         */
      }

      return modules;
    }
    finally
    {
      if (compilationUnit != null)
        CompilationUnitManager.release(compilationUnit);
    }
  }

  /**
   * return all the aliases for a specific model in the configuration
   * 
   * @param modelFile
   * @param configuration
   * @return
   * @throws CoreException
   */
  static public Collection<String> getModelAliases(IResource modelFile,
      ILaunchConfiguration configuration) throws CoreException
  {
    String path = modelFile.getFullPath().toOSString();
    Set<String> aliases = new HashSet<String>();
    for (String alias : configuration.getAttribute(
        ACTRLaunchConstants.ATTR_MODEL_ALIASES + path, "").split(","))
    {
      alias = alias.trim();
      if (alias.length() > 0) aliases.add(alias);
    }
    return aliases;
  }

  /**
   * sets up the basic parameters needed for the eclipse launch. these are the
   * attributes that are set once and never need modifying
   * 
   * @param workingCopy
   */
  static public void setupPermanentAttributes(
      ILaunchConfigurationWorkingCopy workingCopy) throws CoreException
  {

    workingCopy.setAttribute(
        org.eclipse.pde.launching.IPDELauncherConstants.AUTOMATIC_VALIDATE,
        RuntimePlugin.getDefault().getPluginPreferences()
            .getBoolean(RuntimePreferences.VERIFY_RUN_PREF));

    /*
     * First, we tell the PDE launcher that we will be running an application
     * and what that application is
     */
    workingCopy.setAttribute(
        org.eclipse.pde.launching.IPDELauncherConstants.USE_PRODUCT, false);

    if (workingCopy.getAttribute(ACTRLaunchConstants.ATTR_ITERATIONS, 0) == 0)
      workingCopy.setAttribute(
          org.eclipse.pde.launching.IPDELauncherConstants.APPLICATION,
          ACTRLaunchConstants.DEFAULT_APPLICATION);
    else
      workingCopy.setAttribute(
          org.eclipse.pde.launching.IPDELauncherConstants.APPLICATION,
          ACTRLaunchConstants.ITERATIVE_APPLICATION);

    /*
     * then we tell it where the workspace should be..
     * ${system_property:user.home}/.jactr/workspaces/${project_name} this will
     * be resolved by the launcher
     */
    workingCopy.setAttribute(
        org.eclipse.pde.launching.IPDELauncherConstants.LOCATION,
        ACTRLaunchConstants.NORMAL_WORKSPACE_LOCATION);

    /*
     * if true, we'd load ALL the bundles that are present in the current
     * environment we only want to load the required ones that we selected above
     */
    workingCopy.setAttribute(
        org.eclipse.pde.launching.IPDELauncherConstants.USE_DEFAULT, false);

    /*
     * exclude optional bundles
     */
    workingCopy
        .setAttribute(
            org.eclipse.pde.launching.IPDELauncherConstants.INCLUDE_OPTIONAL,
            false);

    /*
     * don't add everything in the workspace
     */
    workingCopy.setAttribute(
        org.eclipse.pde.launching.IPDELauncherConstants.AUTOMATIC_ADD, false);

    /*
     * 
     */
    workingCopy
        .setAttribute(
            org.eclipse.pde.launching.IPDELauncherConstants.DESELECTED_WORKSPACE_PLUGINS,
            (String) null);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Applied permanent attributes " + workingCopy);
  }

  /**
   * set up the attributes that we just apply after Apply is clicked in the
   * config dialog
   * 
   * @param workingCopy
   */
  static public void setupPersistentAttributes(
      ILaunchConfigurationWorkingCopy workingCopy) throws CoreException
  {
    /*
     * we will use the default configuration location, which should be the
     * current environment's.. the name is RUNTYPE:runName..
     */
    IProject project = getProject(workingCopy);
    String configName = workingCopy.getName();

    workingCopy
        .setAttribute(
            org.eclipse.pde.launching.IPDELauncherConstants.CONFIG_USE_DEFAULT_AREA,
            false);
    workingCopy.setAttribute(
        org.eclipse.pde.launching.IPDELauncherConstants.CONFIG_LOCATION,
        ACTRLaunchConstants.NORMAL_CONFIGURATION_LOCATION + project.getName()
            + "/" + configName);

    /*
     * what about core logging? that needs to be set up by the tab but for now..
     */
    boolean logging = workingCopy.getAttribute(
        ACTRLaunchConstants.ATTR_DEBUG_CORE_ENABLED, false);

    if (logging)
    {
      IResource logFile = project.findMember(workingCopy.getAttribute(
          ACTRLaunchConstants.ATTR_DEBUG_CORE_LOG_CONF,
          ACTRLaunchConstants.DEFAULT_CORE_LOG_CONF));
      if (logFile != null && logFile.exists())
      {
        StringBuilder vmArg = new StringBuilder(
            " -Dorg.apache.commons.logging.log=");
        vmArg.append(workingCopy.getAttribute(
            ACTRLaunchConstants.ATTR_DEBUG_CORE_LOGGER,
            ACTRLaunchConstants.DEFAULT_CORE_LOGGER));
        vmArg.append(" -Dlog4j.configuration=");
        try
        {
          URI uri = logFile.getRawLocationURI();
          vmArg
              .append(uri.toURL())
              .append(" ")
              .append(
                  workingCopy.getAttribute(
                      IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""));

          workingCopy.setAttribute(
              IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
              vmArg.toString());
        }
        catch (Exception e)
        {
          CorePlugin.debug("failed to transform url " + logFile, e);
          logging = false;
        }
      }
      else
        logging = false;
    }

    if (!logging)
      workingCopy.setAttribute(
          IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
          "-Dorg.apache.commons.logging.log=org.apache.commons.logging.impl.SimpleLog "
              + workingCopy.getAttribute(
                  IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, ""));

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Applied persistent attributes " + workingCopy);
  }

  /**
   * set up the information that is set strictly for the immediate launch. this
   * includes the working directory, environment file, program args
   * 
   * @param workingCopy
   * @param mode
   * @param environmentFile
   */
  static public void setupTemporaryAttributes(
      ILaunchConfigurationWorkingCopy workingCopy, String mode,
      IResource environmentFile) throws CoreException
  {
    StringBuilder arguments = new StringBuilder();

    /*
     * make sure no splash screen is shown
     */
    arguments.append("-name jACTR -nosplash ");

    /**
     * here's a mac specific bit of code
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=133072. The deal is that
     * eclipse (SWT) needs -ws carbon to run correctly. However if this is
     * provided, Swing/AWT calls will result in deadlock. The PDE tools
     * automatically add the -ws option, unless
     * IPDEUIConstants.APPEND_ARGS_EXPLICITLY is true (i.e. the program added it
     * already). So, if this is the mac, we explicitly add the program args,
     * excluding -ws. <br>
     * <br>
     * This works fine since we are launching within eclipse. If we were to
     * build a standalone app, on the mac, we'd need to add
     * --launcher.secondThread to the command line so to achieve a similar
     * effect.
     */
    if (TargetPlatform.getWS().equals("carbon")
        || TargetPlatform.getWS().equals("cocoa"))
    {
      workingCopy.setAttribute(IPDEConstants.APPEND_ARGS_EXPLICITLY, true);
      // arguments.append("--launcher.secondThread ");
      arguments.append("-os ").append(TargetPlatform.getOS()).append(" ");
      arguments.append("-arch ").append(TargetPlatform.getOSArch()).append(" ");
    }

    if (workingCopy.getType().getIdentifier()
        .equals("org.jactr.eclipse.runtime.launching.cr"))
      arguments.append(ACTRLaunchConstants.DEFAULT_CR_RUN_ARG);
    else if (workingCopy.getAttribute(ACTRLaunchConstants.ATTR_ITERATIONS, 0) != 0)
      arguments.append(ACTRLaunchConstants.ITERATIVE_APPLICATION_ARG);
    else if (ILaunchManager.DEBUG_MODE.equals(mode))
      arguments.append(ACTRLaunchConstants.DEFAULT_APPLICATION_DEBUG_ARG);
    else
      arguments.append(ACTRLaunchConstants.DEFAULT_APPLICATION_RUN_ARG);

    arguments.append(" ");

    try
    {
      URI uri = environmentFile.getRawLocationURI();
      arguments
          .append(uri.toURL())
          .append(" ")
          .append(
              workingCopy.getAttribute(
                  IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""));

      workingCopy.setAttribute(
          IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
          arguments.toString());
    }
    catch (Exception e)
    {
      throw new CoreException(new Status(IStatus.ERROR,
          RuntimePlugin.PLUGIN_ID, "Could not get a valid url from "
              + environmentFile, e));
    }

    /*
     * working directory - where the JVM is run from
     */
    workingCopy.setAttribute(
        IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
        environmentFile.getParent().getLocation().toOSString());

    /*
     * now for the fun.. we need to get the plugin dependencies. first we get
     * the dependencies for the application.. we do this temporarily because..
     * well, things change.
     */

    Set<String> workspace = new TreeSet<String>();
    Set<String> target = new TreeSet<String>();
    computeBundleDependencies(workingCopy, workspace, target);

    StringBuilder workspaceBundles = new StringBuilder();
    StringBuilder targetBundles = new StringBuilder();
    for (String bundle : workspace)
      workspaceBundles.append(bundle).append(",");
    for (String bundle : target)
      targetBundles.append(bundle).append(",");

    if (workspaceBundles.length() > 0)
      workspaceBundles.delete(workspaceBundles.length() - 1,
          workspaceBundles.length());

    if (targetBundles.length() > 0)
      targetBundles.delete(targetBundles.length() - 1, targetBundles.length());

    workingCopy
        .setAttribute(
            org.eclipse.pde.launching.IPDELauncherConstants.SELECTED_WORKSPACE_PLUGINS,
            workspaceBundles.toString());
    workingCopy
        .setAttribute(
            org.eclipse.pde.launching.IPDELauncherConstants.SELECTED_TARGET_PLUGINS,
            targetBundles.toString());

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Applied temporary attributes " + workingCopy);
  }

  /**
   * snag all the sensors that are defined in this configuration
   * 
   * @param configuration
   * @return
   * @throws CoreException
   */
  static public Collection<SensorDescriptor> getRequiredSensors(
      ILaunchConfiguration configuration) throws CoreException
  {
    IProject project = getProject(configuration);
    ArrayList<SensorDescriptor> descriptors = new ArrayList<SensorDescriptor>();
    Collection<SensorDescriptor> installed = SensorRegistry.getRegistry()
        .getDescriptors(project, true);

    String sensors = configuration.getAttribute(
        ACTRLaunchConstants.ATTR_COMMON_REALITY_SENSORS, "");
    for (String sensor : sensors.split(","))
      for (SensorDescriptor desc : installed)
        if (desc.getClassName().equals(sensor)) descriptors.add(desc);
    return descriptors;
  }

  static public Collection<IterativeListenerDescriptor> getRequiredListeners(
      ILaunchConfiguration configuration) throws CoreException
  {
    IProject project = getProject(configuration);

    ArrayList<IterativeListenerDescriptor> descriptors = new ArrayList<IterativeListenerDescriptor>();
    Collection<IterativeListenerDescriptor> installed = IterativeListenerRegistry
        .getRegistry().getDescriptors(project, true);

    String sensors = configuration.getAttribute(
        ACTRLaunchConstants.ATTR_ITERATIVE_LISTENERS, "");

    for (String listener : sensors.split(","))
      for (IterativeListenerDescriptor desc : installed)
        if (desc.getClassName().equals(listener)) descriptors.add(desc);

    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Launch configuration for " + project.getName() + " : "
          + configuration.getAttributes());
      LOGGER.debug("Required sensors : " + sensors);
      LOGGER.debug("Returning : " + descriptors);
    }

    return descriptors;
  }

  /**
   * return all the instruments that are required in this configuration
   * 
   * @param configuration
   * @return
   * @throws CoreException
   */
  static public Collection<InstrumentDescriptor> getRequiredInstruments(
      ILaunchConfiguration configuration) throws CoreException
  {
    IProject project = getProject(configuration);
    ArrayList<InstrumentDescriptor> descriptors = new ArrayList<InstrumentDescriptor>();

    Collection<InstrumentDescriptor> installed = InstrumentRegistry
        .getRegistry().getDescriptors(project, true);

    String instruments = configuration.getAttribute(
        ACTRLaunchConstants.ATTR_INSTRUMENTS, "");
    for (String instrument : instruments.split(","))
      for (InstrumentDescriptor desc : installed)
        if (desc.getClassName().equals(instrument)) descriptors.add(desc);
    return descriptors;
  }

  static public Collection<RuntimeTracerDescriptor> getRequiredTracers(
      ILaunchConfiguration configuration) throws CoreException
  {
    IProject project = getProject(configuration);
    ArrayList<RuntimeTracerDescriptor> descriptors = new ArrayList<RuntimeTracerDescriptor>();

    Collection<RuntimeTracerDescriptor> installed = RuntimeTracerRegistry
        .getRegistry().getDescriptors(project, true);

    String instruments = configuration.getAttribute(
        ACTRLaunchConstants.ATTR_TRACERS, "");

    for (String instrument : instruments.split(","))
      for (RuntimeTracerDescriptor desc : installed)
        if (desc.getClassName().equals(instrument)) descriptors.add(desc);

    return descriptors;
  }

  @SuppressWarnings("unchecked")
  static public void computeBundleDependencies(
      ILaunchConfigurationWorkingCopy configuration,
      Set<String> workspaceBundles, Set<String> targetBundles)
      throws CoreException
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    String projectName = configuration.getAttribute(
        IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
    IProject sourceProject = null;

    if (projectName.length() != 0)
      sourceProject = root.getProject(projectName);

    // IProject project = root.getProject(configuration.getAttribute(
    // LaunchConfigurationConstants.ACTR_PROJECT, ""));

    Collection<String> appDependencies = null;

    if (configuration.getAttribute(ACTRLaunchConstants.ATTR_ITERATIONS, 0) == 0)
      appDependencies = BundleUtilities
          .getDependencies(ACTRLaunchConstants.DEFAULT_APPLICATION_BUNDLE);
    else
      appDependencies = BundleUtilities
          .getDependencies(ACTRLaunchConstants.ITERATIVE_APPLICATION_BUNDLE);

    Collection<String> currentDependencies = Collections.EMPTY_SET;

    if (sourceProject != null && sourceProject.exists())
      currentDependencies = BundleUtilities.getDependencies(sourceProject);

    Collection<String> uniqueDependencies = new TreeSet<String>();
    for (String bundleId : appDependencies)
      uniqueDependencies.add(bundleId);

    for (String bundleId : currentDependencies)
      uniqueDependencies.add(bundleId);

    /*
     * now for the sensors
     */
    for (SensorDescriptor sensor : getRequiredSensors(configuration))
      for (String bundleId : BundleUtilities.getDependencies(sensor
          .getContributor()))
        uniqueDependencies.add(bundleId);

    /*
     * and instruments
     */
    for (InstrumentDescriptor instrument : getRequiredInstruments(configuration))
      for (String bundleId : BundleUtilities.getDependencies(instrument
          .getContributor()))
        uniqueDependencies.add(bundleId);

    /*
     * now we determine where they are coming from, we preference workspace
     * plugins over installed ones so that you can self-host
     */
    for (IPluginModelBase modelBase : PluginRegistry.getWorkspaceModels())
    {
      String pluginId = modelBase.getPluginBase(true).getId();

      // not entirely clear how to get the project from the model..
      // this matters because if the project is closed, we shouldn't use it
      // IProject requiredProject = root.getProject();
      // if (requiredProject.isAccessible())
      if (pluginId != null && uniqueDependencies.remove(pluginId))
        workspaceBundles.add(pluginId);
    }

    /*
     * and the rest we assume are targets
     */
    targetBundles.addAll(uniqueDependencies);

    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("workspace : " + workspaceBundles.toString());
      LOGGER.debug("target : " + targetBundles.toString());
    }
  }
}
