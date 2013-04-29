/*
 * Created on Jul 16, 2004 Copyright (C) 2001-4, Anthony Harrison anh23@pitt.edu
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this library; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.jactr.eclipse.ui.perspective;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

/**
 * @author harrison TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Generation - Code and
 *         Comments
 */
public class ACTRPerspective implements IPerspectiveFactory
{

  static public final String ID = "org.jactr.eclipse.ui.perspective.ACTRPerspective";
  
  static public final String  CONFLICT_ID = "org.jactr.eclipse.runtime.ui.production.productionView";

  // since its in runtime ui
  static public final String LOG_ID      = "org.jactr.eclipse.runtime.ui.log2.ModelLogView2";

  static public final String  BUFFER_ID  = "org.jactr.eclipse.runtime.ui.buffer.bufferView";

  static public final String VISICON_ID  = "org.jactr.eclipse.runtime.ui.visicon.VisiconViewer";

  static public final String PROBE_ID    = "org.jactr.eclipse.runtime.ui.probe.ModelProbeView";

  /**
   * 
   */
  public ACTRPerspective()
  {
    super();
  }

  public void createInitialLayout(IPageLayout layout)
  {
    // createOldLayout(layout);
    createLayout(layout);
    addShortCuts(layout);
    addActionSets(layout);
  }

  protected void addActionSets(IPageLayout layout)
  {
    /*
     * actions sets..
     */

    layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
    layout.addActionSet(IDebugUIConstants.DEBUG_ACTION_SET);
    layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
    layout.addActionSet(JavaUI.ID_ACTION_SET);
  }

  protected void addShortCuts(IPageLayout layout)
  {
    /*
     * short cuts
     */

    // views - debugging
    layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);

    // views - standard workbench
    layout.addShowViewShortcut(LOG_ID);
    layout.addShowViewShortcut(BUFFER_ID);
    layout.addShowViewShortcut(CONFLICT_ID);
    layout.addShowViewShortcut(VISICON_ID);
    layout.addShowViewShortcut(PROBE_ID);
    layout.addShowViewShortcut("org.eclipse.pde.runtime.LogView"); //$NON-NLS-1$
    layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
    layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
    layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
    layout.addShowViewShortcut(JavaUI.ID_PACKAGES);

    // new actions -
    layout
        .addNewWizardShortcut("org.jactr.eclipse.ui.wizards.project.NewACTRProjectWizard");
    layout
        .addNewWizardShortcut("org.jactr.eclipse.ui.wizards.model.NewModelWizard"); //$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
    layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
  }

  protected void createLayout(IPageLayout layout)
  {
    String editorArea = layout.getEditorArea();

    // for navigation & visicon
    IFolderLayout leftSideFolder = layout.createFolder("left",
        IPageLayout.LEFT, 0.15f, editorArea);
    IFolderLayout leftPocketFolder = layout.createFolder("leftPocket",
        IPageLayout.BOTTOM, 0.7f, "left");

    // for log,probe,console, etc.
    IFolderLayout bottomFolder = layout.createFolder("bottom",
        IPageLayout.BOTTOM, 0.7f, editorArea);

    // for ouline
    IFolderLayout outlineFolder = layout.createFolder("outline",
        IPageLayout.RIGHT, 0.8f, editorArea);

    /*
     * left side gets explorer, navigator
     */
    leftSideFolder.addView(JavaUI.ID_PACKAGES);
    leftSideFolder.addView(IPageLayout.ID_RES_NAV);

    /*
     * left pocket gets visicon
     */
    leftPocketFolder.addView(VISICON_ID);
    leftPocketFolder.addPlaceholder(VISICON_ID);

    /*
     * bottom gets log, probe, problems, console
     */
    bottomFolder.addView(LOG_ID);
    bottomFolder.addPlaceholder(LOG_ID);
    bottomFolder.addView(PROBE_ID);
    bottomFolder.addPlaceholder(PROBE_ID);
    bottomFolder.addView(IPageLayout.ID_PROBLEM_VIEW);
    bottomFolder.addPlaceholder(IPageLayout.ID_PROBLEM_VIEW);
    bottomFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
    bottomFolder.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);

    /**
     * outline contains just that..
     */
    outlineFolder.addView(IPageLayout.ID_OUTLINE);
  }



}
