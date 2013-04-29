package org.jactr.eclipse.association.ui.handler;

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
import org.jactr.eclipse.association.ui.views.AssociationViewer;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.io.antlr3.builder.JACTRBuilder;

public class VisualizeHandler extends AbstractHandler
{
  static private final transient Log LOGGER        = LogFactory
                                                       .getLog(VisualizeHandler.class);

  static public final String         ALL      = "org.jactr.eclipse.association.command.visualize.all";

  static public final String         IN     = "org.jactr.eclipse.association.command.visualize.radial.in";

  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    String type = event.getCommand().getId();
    AssociationViewer viewer = openViewer();
    ACTRModelEditor editor = (ACTRModelEditor) HandlerUtil
        .getActiveEditor(event);
    
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Executing "+type);

    if (ALL.equals(type))
      viewer.viewAll(editor);
    else
      viewer.view(editor, editor.getNearestAST(JACTRBuilder.CHUNK));

    return null;
  }

  protected AssociationViewer openViewer()
  {
    try
    {
      IWorkbenchPage page = PlatformUI.getWorkbench()
          .getActiveWorkbenchWindow().getActivePage();
      AssociationViewer viewer = (AssociationViewer) page
          .showView(AssociationViewer.VIEW_ID);
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
