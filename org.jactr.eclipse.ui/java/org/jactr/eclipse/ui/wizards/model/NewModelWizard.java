package org.jactr.eclipse.ui.wizards.model;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.bundles.descriptors.ModuleDescriptor;
import org.jactr.eclipse.core.parser.ProjectSensitiveParserImportDelegate;
import org.jactr.io.IOUtilities;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.compiler.CompilationError;
import org.jactr.io.antlr3.compiler.CompilationWarning;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.io.generator.CodeGeneratorFactory;
import org.jactr.io.generator.ICodeGenerator;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the
 * provided container. If the container resource (a folder or a project) is
 * selected in the workspace when the wizard is opened, it will accept it as the
 * target container. The wizard creates one file with the extension "mpe". If a
 * sample multi-page editor (also available as a template) is registered for the
 * same extension, it will be able to open it.
 */

public class NewModelWizard extends Wizard implements INewWizard
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(NewModelWizard.class);

  static public final String         ID     = NewModelWizard.class.getName();

  private NewModelWizardPage1        page;

  private ISelection                 selection;

  /**
   * Constructor for SampleNewWizard.
   */
  public NewModelWizard()
  {
    super();
    setNeedsProgressMonitor(true);
  }

  /**
   * Adding the page to the wizard.
   */

  @Override
  public void addPages()
  {
    page = new NewModelWizardPage1(selection);
    addPage(page);
  }

  /**
   * This method is called when 'Finish' button is pressed in the wizard. We
   * will create an operation and run it using wizard as execution context.
   */
  @Override
  public boolean performFinish()
  {
    final String projectName = page.getProjectName();
    final String fileName = page.getFileName();
    final Collection<ModuleDescriptor> extensions = page.getSelectedModules();
    IRunnableWithProgress op = new IRunnableWithProgress() {

      public void run(IProgressMonitor monitor)
          throws InvocationTargetException
      {
        // ClassLoader cl = UIPlugin.changeClassLoader();
        try
        {
          doFinish(projectName, fileName, extensions, monitor);
        }
        catch (CoreException e)
        {
          throw new InvocationTargetException(e);
        }
        finally
        {
          monitor.done();
          // UIPlugin.restoreClassLoader(cl);
        }
      }
    };
    try
    {
      getContainer().run(true, false, op);
    }
    catch (InterruptedException e)
    {
      return false;
    }
    catch (InvocationTargetException e)
    {
      Throwable realException = e.getTargetException();
      MessageDialog.openError(getShell(), "Error", realException.getMessage());
      return false;
    }
    return true;
  }

  /**
   * The worker method. It will find the container, create the file if missing
   * or just replace its contents, and open the editor on the newly created
   * file.
   */

  private void doFinish(final String projectName, final String fileName,
      final Collection<ModuleDescriptor> modules, IProgressMonitor monitor)
      throws CoreException
  {
    // create a sample file
    monitor.beginTask("Creating " + fileName, 2);

    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = root.getProject(projectName);
    if (!project.exists())
      throwCoreException("Project \"" + projectName + "\" does not exist.");

    /*
     * make sure we have the model directory
     */
    IFolder folder = project.getFolder("models");
    if (!folder.exists()) folder.create(true, true, null);

    final IFile file = folder.getFile(new Path(fileName));
    try
    {
      InputStream stream = openContentStream(project, fileName, modules);
      if (file.exists()) file.setContents(stream, true, true, monitor);
      else
        file.create(stream, true, monitor);
      stream.close();
    }
    catch (Exception e)
    {
      CorePlugin.error("Could not find code generator for " + fileName, e);
    }

    monitor.worked(1);
    monitor.setTaskName("Opening file for editing...");
    getShell().getDisplay().asyncExec(new Runnable() {

      public void run()
      {
        IWorkbenchPage page = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage();
        try
        {
          if (file.exists()) IDE.openEditor(page, file, true);
        }
        catch (PartInitException e)
        {
        }
      }
    });
    monitor.worked(1);
    monitor.done();
  }

  /**
   * We will initialize file contents with a sample text.
   */

  @SuppressWarnings("unchecked")
  private InputStream openContentStream(IProject project, String modelName,
      Collection<ModuleDescriptor> modules)
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    BufferedOutputStream bos = new BufferedOutputStream(baos);
    try
    {
      CommonTree modelTree = IOUtilities.createModelDescriptor(modelName
          .substring(
          0, modelName.lastIndexOf(".")));

      ProjectSensitiveParserImportDelegate delegate = new ProjectSensitiveParserImportDelegate();
      delegate.setProject(project);
      CommonTree modulesRoot = ASTSupport.getFirstDescendantWithType(modelTree,
          JACTRBuilder.MODULES);
      for (ModuleDescriptor moduleExt : modules)
        try
        {
          String moduleClass = moduleExt.getClassName();
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("Installing module into new model " + moduleClass);

          /*
           * and do the actual import of content
           */
          CommonTree node = delegate.importModuleInto(modelTree, moduleClass,
              true);
          modulesRoot.addChild(node);
        }
        catch (CompilationWarning warning)
        {
          // this would come up if we can't get the code generator
          CorePlugin.warn("Problems get module code for " + moduleExt.getName()
              + " in " + modelName, warning);
        }
        catch (CompilationError error)
        {
          CorePlugin.warn("Problems get module code for " + moduleExt.getName()
              + " in " + modelName, error);
        }

      String extension = modelName.substring(modelName.lastIndexOf(".") + 1,
          modelName.length()).toLowerCase();
      ICodeGenerator codeGen = CodeGeneratorFactory.getCodeGenerator(extension);
      if (codeGen != null)
      {
        PrintStream ps = new PrintStream(bos);
        Collection<StringBuilder> text = codeGen.generate(modelTree, true);
        for (StringBuilder line : text)
          ps.println(line.toString());
      }
      else
        CorePlugin.error("Could not find code generator for " + modelName
            + " based on " + extension);

      bos.flush();
      bos.close();
    }
    catch (Exception e)
    {
      CorePlugin.error("Could not create model code for " + modelName, e);
    }

    return new ByteArrayInputStream(baos.toByteArray());
  }

  private void throwCoreException(String message) throws CoreException
  {
    IStatus status = new Status(IStatus.ERROR, "org.jactr.eclipse.ui",
        IStatus.OK, message, null);
    throw new CoreException(status);
  }

  /**
   * We will accept the selection in the workbench to see if we can initialize
   * from it.
   * 
   * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
   */
  public void init(IWorkbench workbench, IStructuredSelection selection)
  {
    this.selection = selection;
  }
}