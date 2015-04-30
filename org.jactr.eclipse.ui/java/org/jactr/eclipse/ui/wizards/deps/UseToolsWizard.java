package org.jactr.eclipse.ui.wizards.deps;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.jactr.eclipse.core.bundles.registry.InstrumentRegistry;
import org.jactr.eclipse.core.bundles.registry.ModuleRegistry;
import org.jactr.eclipse.core.bundles.registry.SensorRegistry;
import org.jactr.eclipse.ui.wizards.pages.CommonExtensionDescriptorLabelProvider;
import org.jactr.eclipse.ui.wizards.pages.CommonExtensionWizardPage;
import org.jactr.eclipse.ui.wizards.pages.ToolsExplanationWizardPage;

public class UseToolsWizard extends Wizard implements IWorkbenchWizard
{
  /**
   * Logger definition
   */
  static private final transient Log            LOGGER       = LogFactory
                                                                 .getLog(UseToolsWizard.class);

  static public final String                    ID           = UseToolsWizard.class
                                                                 .getName();

  private Collection<CommonExtensionWizardPage> _commonPages = new ArrayList<CommonExtensionWizardPage>();

  private IProject                              _project;

  public UseToolsWizard()
  {

  }

  @Override
  public void addPages()
  {
    addPage(new ToolsExplanationWizardPage(
        "toolsExp",
        "Library of Tools",
        "On the following pages you will select what tools you'd like your project to use.\nYou can always change your mind later.",
        "jACT-R uses modular bundles of code to contribute or change your model's behavior.\nYou need to select those tools in order to use them."
            + "\nThis is a convenience to avoid having to directly edit your projects dependencies."));

    CommonExtensionWizardPage inst = new CommonExtensionWizardPage(
        () -> ModuleRegistry.getRegistry().getAllDescriptors(),
        new CommonExtensionDescriptorLabelProvider(), "module", "Modules",
        "Select modules you'd like to use in your project.");
    addPage(inst);
    _commonPages.add(inst);

    inst = new CommonExtensionWizardPage(
        () -> org.jactr.eclipse.core.bundles.registry.ExtensionRegistry
            .getRegistry().getAllDescriptors(),
        new CommonExtensionDescriptorLabelProvider(), "ext", "Extensions",
        "Select runtime extensions that you'd like to use in your project.");
    addPage(inst);
    _commonPages.add(inst);

    inst = new CommonExtensionWizardPage(
        () -> SensorRegistry.getRegistry().getAllDescriptors(),
        new CommonExtensionDescriptorLabelProvider(),
        "sensor",
        "Interfaces",
        "Select sensors/interfaces with CommonReality that you'd like to use in your project.");
    addPage(inst);
    _commonPages.add(inst);

    inst = new CommonExtensionWizardPage(() -> InstrumentRegistry.getRegistry()
        .getAllDescriptors(), new CommonExtensionDescriptorLabelProvider(),
        "inst", "Instruments",
        "Select instruments you'd like to use in your project.");
    addPage(inst);
    _commonPages.add(inst);
  }

  public void init(IWorkbench workbench, IStructuredSelection selection)
  {
    Object obj = selection.getFirstElement();

    if (obj instanceof IResource)
    {
      IResource resource = (IResource) obj;
      _project = resource.getProject();
    }
    else
    if (obj instanceof IProject) _project = (IProject) obj;
    else
      if(obj instanceof IAdaptable)
      _project = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
    
    
    if(_project==null)
      LOGGER.error("I have no idea how to get a project out of this!! "
          + obj.getClass().getName());

  }

  @Override
  public boolean performFinish()
  {
    /*
     * make sure everyone is closed.. This is done here, otherwise we can get
     * some weird unmodifiable messages.
     */
    for (CommonExtensionWizardPage extPage : _commonPages)
      extPage.ensureDependencies(_project);

    return true;
  }

}
