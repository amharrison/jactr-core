package org.jactr.eclipse.ui.commands;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class ArchiveRun extends AbstractHandler
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ArchiveRun.class);

  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    IStructuredSelection selection = (IStructuredSelection) HandlerUtil
        .getCurrentSelection(event);

    for (Object selected : selection.toArray())
      if(selected instanceof IFolder)
      {
        ArchiveAndDeleteJob job = new ArchiveAndDeleteJob((IFolder)selected, true);
        job.schedule();
      }
    return null;
  }

}
