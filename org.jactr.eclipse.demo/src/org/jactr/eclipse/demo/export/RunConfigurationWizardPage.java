package org.jactr.eclipse.demo.export;

/*
 * default logging
 */
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.internal.ui.wizards.product.BaseProductCreationOperation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.jactr.eclipse.core.project.ACTRProjectUtils;
import org.jactr.eclipse.demo.export.ops.ConfigurationOperation;
import org.jactr.eclipse.demo.export.ops.CustomizeProductOperation;
import org.jactr.eclipse.demo.export.ops.UpdateLaunchOperation;
import org.jactr.eclipse.demo.export.ops.WriteEnvironmentOperation;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.eclipse.ui.images.JACTRImages;

public class RunConfigurationWizardPage extends WizardPage
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RunConfigurationWizardPage.class);

  private ListViewer                 _actrConfigurations;

  private IProject                   _project;

  protected RunConfigurationWizardPage(IStructuredSelection selection)
  {
    super("Run Configuration");
    setDescription("Select the run configuration to use");
    checkProject(selection);
  }

  protected boolean checkProject(IStructuredSelection selection)
  {
    _project = (IProject) selection.getFirstElement();
    if (!ACTRProjectUtils.isACTRProject(_project))
    {
      setErrorMessage(_project.getName() + " is not a valid ACT-R project");
      return false;
    }
    setErrorMessage(null);
    return true;
  }

  protected boolean checkSelection()
  {
    if (getLaunchConfiguration() == null)
    {
      setErrorMessage("Must select a run configuration");
      return false;
    }

    setErrorMessage(null);

    return true;
  }

  public boolean isPageComplete()
  {
    boolean complete = checkSelection();

    return complete;
  }

  public void createControl(Composite parent)
  {
    Composite projComp = new Composite(parent, SWT.NONE);
    GridLayout projLayout = new GridLayout();
    projLayout.numColumns = 2;
    projComp.setLayout(projLayout);

    Composite wrapper = new Composite(projComp, SWT.NONE);
    wrapper.setLayout(new GridLayout(1, false));
    wrapper.setLayoutData(new GridData(GridData.FILL_BOTH));

    Label confLabel = new Label(wrapper, SWT.NONE);
    confLabel.setText("jACT-R Run Configuration");
    GridData gd = new GridData();
    gd.horizontalSpan = 2;
    confLabel.setLayoutData(gd);

    _actrConfigurations = new ListViewer(wrapper, SWT.VERTICAL | SWT.SINGLE
        | SWT.BORDER);
    _actrConfigurations.setContentProvider(new ArrayContentProvider());
    _actrConfigurations.setLabelProvider(new LabelProvider() {

      @Override
      public Image getImage(Object element)
      {
        return JACTRImages.getImage(JACTRImages.RUN);
      }

      @Override
      public String getText(Object element)
      {
        return element == null ? "" : ((ILaunchConfiguration) element).getName();//$NON-NLS-1$
      }
    });

    _actrConfigurations
        .addSelectionChangedListener(new ISelectionChangedListener() {

          public void selectionChanged(SelectionChangedEvent event)
          {
            checkSelection();
            getContainer().updateButtons();
          }
        });

    gd = new GridData(GridData.FILL_BOTH);
    _actrConfigurations.getControl().setLayoutData(gd);

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

      ArrayList<ILaunchConfiguration> confs = new ArrayList<ILaunchConfiguration>();
      for (ILaunchConfiguration conf : configurations)
        if (conf.getAttribute(
            IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "").equals(
            _project.getName())) confs.add(conf);

      _actrConfigurations.setInput(confs.toArray());
    }
    catch (Exception e)
    {
      LOGGER.error("Could not get configurations ", e);
    }

    setControl(projComp);
  }

  protected ILaunchConfiguration getLaunchConfiguration()
  {
    IStructuredSelection selection = (IStructuredSelection) _actrConfigurations
        .getSelection();
    if (selection.isEmpty()) return null;
    return (ILaunchConfiguration) selection.getFirstElement();
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

  public IFile finish() throws CoreException
  {
    ILaunchConfigurationWorkingCopy configuration = getLaunchConfiguration()
        .getWorkingCopy();

    try
    {
      getContainer().run(false, true, new UpdateLaunchOperation(configuration));
    }
    catch (InterruptedException e)
    {
      return null;
    }
    catch (InvocationTargetException e)
    {
      throw new CoreException(new Status(IStatus.ERROR,
          "org.jactr.eclipse.demo", e.getMessage(), e));
    }

    WriteEnvironmentOperation envOp = new WriteEnvironmentOperation(
        configuration, _project);

    try
    {
      getContainer().run(false, true, envOp);
    }
    catch (InterruptedException e)
    {
      return null;
    }
    catch (InvocationTargetException e)
    {
      throw new CoreException(new Status(IStatus.ERROR,
          "org.jactr.eclipse.demo", e.getMessage(), e));
    }

    try
    {
      getContainer().run(false, true,
          new ConfigurationOperation(_project, configuration.getName()));
    }
    catch (InterruptedException e)
    {
      return null;
    }
    catch (InvocationTargetException e)
    {
      // should do cleanup
      throw new CoreException(new Status(IStatus.ERROR,
          "org.jactr.eclipse.demo", e.getMessage(), e));
    }

    /*
     * create default product config
     */
    final IFile productConfiguration = ensureFile("configuration/demo/"
        + configuration.getName(), "demo.product");
    try
    {
      getContainer().run(false, true,
          new BaseProductCreationOperation(productConfiguration));
    }
    catch (InterruptedException e)
    {
      return productConfiguration;
    }
    catch (InvocationTargetException e)
    {
      // should do cleanup
      throw new CoreException(new Status(IStatus.ERROR,
          "org.jactr.eclipse.demo", e.getMessage(), e));
    }
    finally
    {
      /*
       * close the product window..
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
              IEditorPart part = IDE.openEditor(page, productConfiguration,
                  true);
              if (part != null) page.closeEditor(part, false);
            }
            catch (Exception e)
            {

            }
        }
      });
    }

    try
    {
      getContainer().run(false, true,
          new CustomizeProductOperation(configuration, productConfiguration));
    }
    catch (InterruptedException e)
    {
      return productConfiguration;
    }
    catch (InvocationTargetException e)
    {
      // should do cleanup
      throw new CoreException(new Status(IStatus.ERROR,
          "org.jactr.eclipse.demo", e.getMessage(), e));
    }

    return productConfiguration;
  }
};
