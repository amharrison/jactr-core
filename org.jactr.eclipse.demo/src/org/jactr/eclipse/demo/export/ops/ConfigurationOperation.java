package org.jactr.eclipse.demo.export.ops;

/*
 * default logging
 */
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.pde.core.plugin.IExtensions;
import org.eclipse.pde.core.plugin.IExtensionsModelFactory;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.pde.core.plugin.IPluginExtension;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.jactr.eclipse.core.bundles.meta.ManifestTools;

public class ConfigurationOperation extends WorkspaceModifyOperation
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ConfigurationOperation.class);

  IProject                           _project;

  private String                     _launchName;

  public ConfigurationOperation(IProject project, String launchName)
  {
    _project = project;
    _launchName = launchName;
  }

  protected void ensureDependencies(IPluginModelBase modelBase, IProject project)
      throws CoreException
  {
    Collection<String> launching = Collections.singleton("org.jactr.launching");
    ManifestTools.addPluginReferences(modelBase, launching);

    ManifestTools.addEclipseBuddies(modelBase, launching);
  }

  protected void createExtensionPoint(IPluginModelBase modelBase)
      throws CoreException
  {
    IExtensions extContainer = modelBase.getExtensions(true);
    IPluginExtension[] extensions = extContainer.getExtensions();

    String id = "org.eclipse.core.runtime.products";
    /*
     * we are looking for org.eclipse.core.runtime.products
     */
    IPluginExtension productExt = null;
    for (IPluginExtension ext : extensions)
      if (ext.getPoint().equals(id))
      {
        productExt = ext;
        break;
      }

    if (productExt != null) // max one permitted
      return;

    IExtensionsModelFactory factory = modelBase.getFactory();

    if (productExt == null)
    {
      productExt = factory.createExtension();
      productExt.setPoint(id);
      productExt.setId("application");
    }

    /*
     * now we build the actual extension for this run..
     */
    IPluginElement product = factory.createElement(productExt);
    product.setName("product");
    product.setAttribute("application", "org.jactr.launching.application");
    product.setAttribute("name", "jACT-R");

    /*
     * and appName
     */
    IPluginElement attr = factory.createElement(product);
    attr.setName("property");
    attr.setAttribute("name", "appName");
    attr.setAttribute("value", "jACT-R");
    product.add(attr);

    productExt.add(product);
    
    extContainer.add(productExt);
  }

  /**
   * make sure that the manifest has Export-Packages : demo/launchName
   * 
   * @param modelBase
   * @param launchName
   */
  protected void ensureExport(IPluginModelBase modelBase, String launchName)
  {
    /*
     * not needed anymore now that we have moved configuration/ into the classpath
     */
//    ManifestTools.addExportPackages(modelBase, Collections.singleton("demo."
//        + launchName));
  }

  @Override
  protected void execute(IProgressMonitor monitor) throws CoreException,
      InvocationTargetException, InterruptedException
  {
    if (monitor != null)
      monitor = new SubProgressMonitor(monitor, 4);
    else
      monitor = new NullProgressMonitor();

    try
    {
      monitor.beginTask("Fetch plugin information from " + _project.getName(),
          4);

      IPluginModelBase modelBase = PluginRegistry.findModel(_project);
      modelBase.load();
      monitor.worked(1);

      monitor.setTaskName("Updating plugin dependencies");
      ensureDependencies(modelBase, _project);
      monitor.worked(1);

      monitor.setTaskName("Ensuring export of configuration/demo/"
          + _launchName);
      ensureExport(modelBase, _launchName);
      monitor.worked(1);

      monitor.setTaskName("Creating deployable extension point");
      createExtensionPoint(modelBase);

      ManifestTools.save(modelBase);
    }
    finally
    {
      monitor.done();
    }

  }

}
