package org.jactr.eclipse.production.handler;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.production.view.ProductionSequenceView;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.io.antlr3.builder.JACTRBuilder;

public class VisualizeHandler extends AbstractHandler
{
  static private final transient Log LOGGER        = LogFactory
                                                       .getLog(VisualizeHandler.class);

  static public final String ALL  = "org.jactr.eclipse.production.command.visualize.all";
  static public final String PREVIOUS = "org.jactr.eclipse.production.command.visualize.radial.in";
  static public final String NEXT = "org.jactr.eclipse.production.command.visualize.radial.out";
  static public final String SEQUENCE = "org.jactr.eclipse.production.command.visualize.sequence";

  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    String type = event.getCommand().getId();
    ProductionSequenceView viewer = openViewer();
    ACTRModelEditor editor = (ACTRModelEditor) HandlerUtil
        .getActiveEditor(event);
    
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Executing "+type);

    if (ALL.equals(type))
      viewer.viewAll(editor);
    else if (PREVIOUS.equals(type))
      viewer.viewPrevious(editor, editor.getNearestAST(JACTRBuilder.PRODUCTION));
    else if (NEXT.equals(type))
      viewer.viewNext(editor, editor.getNearestAST(JACTRBuilder.PRODUCTION));
    else
      viewer.viewSequence(editor, editor.getNearestAST(JACTRBuilder.PRODUCTION));

    return null;
  }

  protected ProductionSequenceView openViewer()
  {
    try
    {
      IWorkbenchPage page = PlatformUI.getWorkbench()
          .getActiveWorkbenchWindow().getActivePage();
      ProductionSequenceView viewer = (ProductionSequenceView) page
          .showView(ProductionSequenceView.VIEW_ID);
      return viewer;
    }
    catch (PartInitException e)
    {
      CorePlugin.error(
          "VisualizeHandler.openViewer threw PartInitException : ", e);
      return null;
    }
  }
}
