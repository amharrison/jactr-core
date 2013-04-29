package org.jactr.eclipse.runtime.ui.command;

/*
 * default logging
 */
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jactr.eclipse.runtime.debug.marker.IDisableProductionMarker;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;

public class ProductionStateHandler extends AbstractHandler
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProductionStateHandler.class);

  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    ACTRModelEditor editor = (ACTRModelEditor) HandlerUtil
        .getActiveEditor(event);

    CommonTree production = editor.getNearestAST(JACTRBuilder.PRODUCTION);
    if (production == null) return null;

    IResource resource = editor.getResource();
    if (resource == null) return null;

    String name = ASTSupport.getName(production);
    int line = production.getLine();
    updateMarker(name, line, resource);

    return null;
  }

  protected void updateMarker(String productionName, int line,
      IResource resource)
  {
    try
    {
      for (IMarker marker : resource.findMarkers(
          IDisableProductionMarker.MARKER_TYPE, true, IResource.DEPTH_INFINITE))
        if (marker.getAttribute(IDisableProductionMarker.PRODUCTION_NAME_ATTR,
            "").equals(productionName))
        {
          marker.delete();
          return;
        }

      IMarker marker = resource
          .createMarker(IDisableProductionMarker.MARKER_TYPE);
      marker.setAttribute(IMarker.MESSAGE, "Disabled " + productionName);
      marker.setAttribute(IMarker.LINE_NUMBER, line);
      marker.setAttribute(IDisableProductionMarker.PRODUCTION_NAME_ATTR,
          productionName);
    }
    catch (CoreException e)
    {
      LOGGER.error("ProductionStateHandler.execute threw CoreException : ", e);
    }

  }

}
