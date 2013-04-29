package org.jactr.eclipse.demo.export.ops;

/*
 * default logging
 */
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.iproduct.IArgumentsInfo;
import org.eclipse.pde.internal.core.iproduct.ILauncherInfo;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductModelFactory;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.jactr.eclipse.core.bundles.BundleUtilities;
import org.jactr.eclipse.core.bundles.meta.ManifestTools;

public class CustomizeProductOperation extends WorkspaceModifyOperation
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(CustomizeProductOperation.class);

  private ILaunchConfiguration       _launchConfiguration;

  private IFile                      _productConfiguration;

  public CustomizeProductOperation(ILaunchConfiguration configuration,
      IFile productConfiguration)
  {
    _launchConfiguration = configuration;
    _productConfiguration = productConfiguration;
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
      monitor.beginTask("Customizing deployable product", 1);
      IPluginModelBase modelBase = PluginRegistry
          .findModel(_productConfiguration.getProject());
      modelBase.load();

      customize(modelBase, _launchConfiguration, _productConfiguration);

      ManifestTools.save(modelBase);
    }
    finally
    {
      monitor.done();
    }
  }

  protected IFile customize(IPluginModelBase modelBase,
      ILaunchConfiguration configuration, IFile productFile)
      throws CoreException
  {
    /*
     * we now have the file, but we want to edit it further to set the default
     * values..
     */
    WorkspaceProductModel model = new WorkspaceProductModel(productFile, true);

    model.load();

    /*
     * we need to tweak productId, application, add all the required bundles,
     * change the laucnher name, and specify program and VM args
     */
    IProduct product = model.getProduct();
    product.setName("jACT-R : "+configuration.getName());
    
    IProductModelFactory factory = model.getFactory();
    IArgumentsInfo args = product.getLauncherArguments();
      

    /*
     * command line
     */
    String cmdLine = args.getProgramArguments(IArgumentsInfo.L_ARGS_ALL);

    cmdLine += " -r demo/" + configuration.getName() + "/environment.xml";
    args.setProgramArguments(cmdLine, IArgumentsInfo.L_ARGS_ALL);

    /*
     * VM args from the launch config
     */
    cmdLine = args.getVMArguments(IArgumentsInfo.L_ARGS_ALL);

    String defined = configuration.getAttribute(
        IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");

    cmdLine += defined;
    args.setVMArguments(cmdLine, IArgumentsInfo.L_ARGS_ALL);

    product.setApplication("org.jactr.launching.application");
    product.setId(modelBase.getPluginBase().getId() + ".application");


    ILauncherInfo launcherInfo = product.getLauncherInfo();
    if (launcherInfo == null) launcherInfo = factory.createLauncherInfo();
    launcherInfo.setLauncherName("jACT-R");
    
    product.setLauncherInfo(launcherInfo);

    /*
     * finally we need to set up the dependencies..
     */
    Collection<IProductPlugin> plugins = new ArrayList<IProductPlugin>();
    for (String pluginId : BundleUtilities.getDependencies(productFile
        .getProject()))
    {
      IProductPlugin plugin = factory.createPlugin();
      plugin.setId(pluginId);
      plugins.add(plugin);
    }

    product.addPlugins(plugins.toArray(new IProductPlugin[0]));

    model.save();
    model.dispose();

    return productFile;
  }

}
