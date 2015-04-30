package org.jactr.eclipse.ui.wizards.project;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.internal.ui.wizards.IProjectProvider;
import org.eclipse.pde.internal.ui.wizards.plugin.PluginFieldData;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.IPluginContentWizard;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.ide.IDE;
import org.jactr.eclipse.core.bundles.registry.InstrumentRegistry;
import org.jactr.eclipse.core.bundles.registry.ModuleRegistry;
import org.jactr.eclipse.core.bundles.registry.SensorRegistry;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.wizards.pages.CommonExtensionDescriptorLabelProvider;
import org.jactr.eclipse.ui.wizards.pages.CommonExtensionWizardPage;
import org.jactr.eclipse.ui.wizards.pages.ToolsExplanationWizardPage;
import org.jactr.eclipse.ui.wizards.templates.ModuleWizard;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the
 * provided container. If the container resource (a folder or a project) is
 * selected in the workspace when the wizard is opened, it will accept it as the
 * target container. The wizard creates one file with the extension "mpe". If a
 * sample multi-page editor (also available as a template) is registered for the
 * same extension, it will be able to open it.
 */

public class NewACTRProjectWizard extends Wizard implements INewWizard
{
  /**
   * Logger definition
   */

  static private final transient Log            LOGGER       = LogFactory
                                                                 .getLog(NewACTRProjectWizard.class);

  static public final String                    ID           = NewACTRProjectWizard.class
                                                                 .getName();

  private WizardNewProjectCreationPage          mainPage;

  private ACTRWizardSelectionPage               wizardPage;

  private Collection<CommonExtensionWizardPage> _commonPages = new ArrayList<CommonExtensionWizardPage>();

  /**
   * Constructor for SampleNewWizard.
   */
  public NewACTRProjectWizard()
  {
    super();
    setWindowTitle("jACT-R Project Wizard");
    setNeedsProgressMonitor(true);
  }

  /**
   * Adding the page to the wizard.
   */
  @Override
  public void addPages()
  {
    mainPage = new WizardNewProjectCreationPage("ACT-R Project");
    mainPage.setInitialProjectName("edu.your.institution.lab.project");
    mainPage
        .setDescription("Provide a unique name for the project, preferably using the java reverse URL convention.");
    addPage(mainPage);

    addPage(new ToolsExplanationWizardPage(
        "toolsExp",
        "Library of Tools",
        "On the following pages you will select what tools you'd like your project to use.\n You can always change your mind later.",
        "jACT-R uses modular bundles of code to contribute or change your model's behavior.\n You need to select those tools in order to use them."
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

    Collection<IWizardNode> wizards = createTemplateWizards();

    wizardPage = new ACTRWizardSelectionPage("ACT-R Selection", wizards);
    wizardPage
        .setDescription("Optionally, you can select code generating wizard for further customization, or just Finish.");
    addPage(wizardPage);

  }

  protected Collection<IWizardNode> createTemplateWizards()
  {
    ArrayList<IWizardNode> nodes = new ArrayList<IWizardNode>();

    /*
     * we want to find all the wizard extensions that are under
     * org.jactr.project.wizard
     */

    IExtensionRegistry extReg = Platform.getExtensionRegistry();
    IExtensionPoint extPoint = extReg
        .getExtensionPoint("org.eclipse.pde.ui.pluginContent");
    IExtension[] extensions = extPoint.getExtensions();
    for (IExtension extension : extensions)
    {
      IConfigurationElement[] configs = extension.getConfigurationElements();
      // looking for wizard
      for (IConfigurationElement element : configs)
        if (element.getName().equals("wizard"))
          // category :
          // org.jactr.project.wizard
          if (element.getAttribute("category") != null
              && element.getAttribute("category").equals(
                  "org.jactr.project.wizard"))
          {
            String className = element.getAttribute("class");
            String description = element.getAttribute("name");
            try
            {
              nodes.add(new WizardNode((Class<? extends IWizard>) getClass()
                  .getClassLoader().loadClass(className), description));
            }
            catch (Exception e)
            {
              UIPlugin.log(IStatus.ERROR, String.format(
                  "Could not load %s from %s", className, extension
                      .getContributor().getName()), e);
            }
          }
    }

    // nodes.add(new WizardNode(ModuleWizard.class,
    // "Create a new ACT-R Module"));
    // nodes.add(new WizardNode(InstrumentWizard.class,
    // "Create a new ACT-R Instrument"));
    return nodes;
  }

  /**
   * This method is called when 'Finish' button is pressed in the wizard. We
   * will create an operation and run it using wizard as execution context.
   */
  @Override
  public boolean performFinish()
  {
    final String name = mainPage.getProjectName();
    final IProject project = mainPage.getProjectHandle();
    final IPath location = mainPage.getLocationPath();

    /*
     * the first thing we do is delegate
     */
    IProjectProvider provider = new IProjectProvider() {

      public IPath getLocationPath()
      {
        return location;
      }

      public IProject getProject()
      {
        return project;
      }

      public String getProjectName()
      {
        return name;
      }

    };

    PluginFieldData pluginData = new PluginFieldData();
    pluginData.setDoGenerateClass(false);
    pluginData.setRCPApplicationPlugin(false);
    pluginData.setUIPlugin(false);
    pluginData.setName(name);

    pluginData.setVersion("0.0.0.qualifier");
    pluginData.setId(name.replaceAll("[^a-zA-Z0-9\\._]", "_"));

    pluginData.setSimple(false); // so that java is available
    pluginData.setLegacy(false);
    pluginData.setSourceFolderName("java");
    pluginData.setOutputFolderName("bin");
    pluginData.setHasBundleStructure(true);
    pluginData.setProvider(System.getProperty("user.name"));

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("doGen:" + pluginData.doGenerateClass() + " ui:"
          + pluginData.isUIPlugin());

    try
    {
      /**
       * make sure we are always at the very least running one of the wizards
       */
      IPluginContentWizard wizard = wizardPage.getSelectedWizard();
      if (wizard == null) wizard = new ModuleWizard();

      getContainer().run(
          false,
          false,
          new NewProjectCreationOperation(pluginData, provider, wizard,
              _commonPages));

    }
    catch (InterruptedException e)
    {
      return false;
    }
    catch (InvocationTargetException e)
    {
      Throwable realException = e.getTargetException();
      UIPlugin.log("Could not perform finish", realException);
      return false;
    }

    return true;
  }

  public void init(IWorkbench workbench, IStructuredSelection selection)
  {
    // TODO Auto-generated method stub

  }

  static public class NewProjectCreationOperation extends
      org.eclipse.pde.internal.ui.wizards.plugin.NewProjectCreationOperation
  {
    private final IProject                              _project;

    private final Collection<CommonExtensionWizardPage> _commonPages;

    public NewProjectCreationOperation(IFieldData data,
        IProjectProvider provider, IPluginContentWizard contentWizard,
        Collection<CommonExtensionWizardPage> commonPages)
    {
      super(data, provider, contentWizard);
      _project = provider.getProject();
      _commonPages = commonPages;
    }

    @Override
    protected void execute(IProgressMonitor monitor) throws CoreException,
        InvocationTargetException, InterruptedException
    {

      super.execute(monitor);



      /*
       * the above automatically opens manifest, I want to close it..
       */
      final IWorkbenchWindow ww = PlatformUI.getWorkbench()
          .getActiveWorkbenchWindow();
      ww.getShell().getDisplay().asyncExec(new Runnable() {
        public void run()
        {
          IWorkbenchPage page = ww.getActivePage();
          if (page != null)
            try
            {
              IEditorPart part = IDE.openEditor(page,
                  _project.getFile("META-INF/MANIFEST.MF"), true);
              if (part != null) page.closeEditor(part, false);

            }
            catch (Exception e)
            {

            }

          /*
           * make sure everyone is closed.. This is done here, otherwise we can
           * get some weird unmodifiable messages.
           */
          for (CommonExtensionWizardPage extPage : _commonPages)
            extPage.ensureDependencies(_project);
        }
      });
    }
  }

  static protected class WizardNode implements IWizardNode
  {

    private final Class<? extends IWizard> wizardClass;

    private IWizard                        wizard;

    private final String                   description;

    public WizardNode(Class<? extends IWizard> wizard, String description)
    {
      wizardClass = wizard;
      this.description = description;
    }

    public void dispose()
    {
      if (wizard != null) wizard.dispose();
      wizard = null;
    }

    public Point getExtent()
    {
      return new Point(-1, -1);
    }

    public IWizard getWizard()
    {
      if (wizard == null) try
      {
        wizard = wizardClass.newInstance();
      }
      catch (Exception e)
      {
        LOGGER.error("Could not create wizard from " + wizardClass, e);
      }

      return wizard;
    }

    public boolean isContentCreated()
    {
      return wizard != null && wizard.getPageCount() != 0;
    }

    @Override
    public String toString()
    {
      return description;
    }

  }
}