package org.jactr.eclipse.runtime.ui.command;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.playback.SessionArchive;
import org.jactr.eclipse.runtime.session.manager.internal.SessionManager;

public class ArchivalSessionLoader extends AbstractHandler
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ArchivalSessionLoader.class);

  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    IStructuredSelection selection = (IStructuredSelection) HandlerUtil
        .getCurrentSelection(event);

    for (Object selected : selection.toArray())
      if (selected instanceof IFolder)
        try
        {
          IFolder folder = (IFolder) selected;
          if (folder.getName().equalsIgnoreCase("sessionData"))
          {
            IResource file = folder.findMember("sessionData.index");

            if (file != null)
            {
              SessionArchive sa = new SessionArchive(file);
              // pump the first block of data
              sa.getController().resume();

              ((SessionManager) RuntimePlugin.getDefault().getSessionManager())
                  .addSession(sa);
            }
            else
              RuntimePlugin
                  .error("sessionData folder does not contain sessionData.index file");
          }

        }
        catch (Exception e)
        {
          LOGGER.error("failed to pump it up ", e);
        }

    return null;

  }

}
