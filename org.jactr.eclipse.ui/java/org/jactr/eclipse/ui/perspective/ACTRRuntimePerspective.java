package org.jactr.eclipse.ui.perspective;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.console.IConsoleConstants;

public class ACTRRuntimePerspective extends ACTRPerspective
{
  static public final String         ID     = "org.jactr.eclipse.ui.perspective.ACTRRuntimePerspective";

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ACTRRuntimePerspective.class);

  @Override
  protected void createLayout(IPageLayout layout)
  {
    String editorArea = layout.getEditorArea();
    // hide the editor
    layout.setEditorAreaVisible(false);

    /*
     * right side for conflict set, buffer and visicon
     */
    IFolderLayout rightSideFolder = layout.createFolder("right",
        IPageLayout.RIGHT, 0.7f, editorArea);

    /*
     * bottom portion for log and console
     */
    IFolderLayout bottomFolder = layout.createFolder("bottom",
        IPageLayout.BOTTOM, 0.66f, editorArea);

    /*
     * now we split bottom in half for log/probe & console
     */
    IFolderLayout consoleBottom = layout.createFolder("console.bottom",
        IPageLayout.BOTTOM, 0.66f, "bottom");
    consoleBottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
    consoleBottom.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);

    bottomFolder.addView(LOG_ID);
    bottomFolder.addPlaceholder(LOG_ID);
    bottomFolder.addView(PROBE_ID);
    bottomFolder.addPlaceholder(PROBE_ID);

    /*
     * right folder gets split vertically for the visicon
     */
    IFolderLayout perceptualFolder = layout.createFolder("percept.bottom",
        IPageLayout.BOTTOM, 0.55f, "right");
    perceptualFolder.addView(VISICON_ID);
    perceptualFolder.addPlaceholder(VISICON_ID);

    /*
     * now we split right horizontally again..
     */
    IFolderLayout bufferState = layout.createFolder("buffer.state",
        IPageLayout.LEFT, 0.5f, "right");
    bufferState.addView(CONFLICT_ID);
    bufferState.addPlaceholder(CONFLICT_ID);

    rightSideFolder.addView(BUFFER_ID);
    rightSideFolder.addPlaceholder(BUFFER_ID);
  }
}
