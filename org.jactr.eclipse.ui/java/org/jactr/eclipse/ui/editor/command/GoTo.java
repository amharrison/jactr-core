package org.jactr.eclipse.ui.editor.command;

/*
 * default logging
 */
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.io.antlr3.misc.DetailedCommonTree;

public class GoTo
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory.getLog(GoTo.class);

  static public void goTo(DetailedCommonTree node)
  {
    URL url = node.getSource();
    if (url == null || node.getStartOffset() == -1
        || node.getStopOffset() == -1)
    {
      MessageDialog.openInformation(Display.getDefault().getActiveShell(),
          "Error", "Could not find model definition");
      return;
    }

    try
    {
      url = FileLocator.toFileURL(url);

      IFileStore fileStore = EFS.getLocalFileSystem().getStore(url.toURI());

      if (fileStore == null || fileStore.fetchInfo().isDirectory()
          || !fileStore.fetchInfo().exists())
      {
        MessageDialog.openInformation(Display.getDefault().getActiveShell(),
            "Error", "Could not find model file " + url.toURI());
        return;
      }

      IWorkbenchPage page = PlatformUI.getWorkbench()
          .getActiveWorkbenchWindow().getActivePage();

      IEditorPart editor = IDE.openEditorOnFileStore(page, fileStore);

      ACTRModelEditor modelEditor = (ACTRModelEditor) editor;

      modelEditor.selectAndReveal(node.getStartOffset(), node.getStopOffset()
          - node.getStartOffset() + 1);
    }
    catch (Exception e)
    {
      MessageDialog.openInformation(Display.getDefault().getActiveShell(),
          "Error", "Could find no editor to open model " + url);
      CorePlugin.error("Could find no editor to open model " + url, e);
    }
  }
}
