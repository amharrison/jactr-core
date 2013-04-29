package org.jactr.eclipse.demo.export;

/*
 * default logging
 */
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.pde.internal.ui.wizards.exports.ProductExportWizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.jactr.eclipse.ui.UIPlugin;

public class ExportDeployableWizard extends Wizard implements IExportWizard
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ExportDeployableWizard.class);

  private IStructuredSelection       _selection;

  private RunConfigurationWizardPage _page;

  /**
   * Creates a wizard for exporting workspace resources to a zip file.
   */
  public ExportDeployableWizard()
  {

  }

  /*
   * (non-Javadoc) Method declared on IWizard.
   */
  public void addPages()
  {
    super.addPages();
    _page = new RunConfigurationWizardPage(_selection);
    addPage(_page);
  }

  /*
   * (non-Javadoc) Method declared on IWorkbenchWizard.
   */
  public void init(IWorkbench workbench, IStructuredSelection currentSelection)
  {
    this._selection = currentSelection;
    List selectedResources = IDE.computeSelectedResources(currentSelection);

    if (!selectedResources.isEmpty())
      _selection = new StructuredSelection(selectedResources);

    setWindowTitle("");
    setNeedsProgressMonitor(true);
  }

  /*
   * (non-Javadoc) Method declared on IWizard.
   */
  public boolean performFinish()
  {
    try
    {
      IFile productFile = _page.finish();

      if (productFile != null) requestExport(productFile);
    }
    catch (CoreException e)
    {
      UIPlugin.log(e.getStatus());
      return false;
    }

    return true;
  }

  protected void requestExport(final IFile productFile)
  {
    Runnable runner = new Runnable()
    {
      public void run()
      {
        ProductExportWizard wizard = new ProductExportWizard();
        
        IWorkbench workBench = PlatformUI.getWorkbench();
        
        wizard.init(workBench, new StructuredSelection(productFile));
        
        // Create the wizard dialog
        WizardDialog dialog = new WizardDialog(workBench.getActiveWorkbenchWindow()
            .getShell(), wizard);
        
        dialog.open();        
      }
    };
    
    Display.getCurrent().asyncExec(runner);
  }
}
