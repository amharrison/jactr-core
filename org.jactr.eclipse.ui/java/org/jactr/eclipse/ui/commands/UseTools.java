package org.jactr.eclipse.ui.commands;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jactr.eclipse.ui.wizards.deps.UseToolsWizard;

public class UseTools extends AbstractHandler
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory.getLog(UseTools.class);

  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    IStructuredSelection selection = (IStructuredSelection) HandlerUtil
        .getCurrentSelection(event);

    // assuming the selection is correct

    UseToolsWizard wizard = new UseToolsWizard();
    wizard.init(null, selection);

    WizardDialog wd = new WizardDialog(Display.getDefault().getActiveShell(),
        wizard);
    wd.setTitle(wizard.getWindowTitle());
    wd.open();

    return null;
  }

}
