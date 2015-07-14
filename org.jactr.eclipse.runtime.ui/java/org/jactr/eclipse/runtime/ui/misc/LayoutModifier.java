package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.debug.elements.ACTRDebugElement;
import org.jactr.eclipse.runtime.launching.iterative.IterativeSession;
import org.jactr.eclipse.runtime.launching.session.AbstractSession;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.eclipse.runtime.session.ILocalSession;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.impl.Session2SessionAdapter;
import org.jactr.eclipse.runtime.session.manager.ISessionManagerListener;
import org.jactr.eclipse.ui.perspective.ACTRRuntimePerspective;

/**
 * LayoutModifier listens for two things. 1) new ACTR Sesssions (using the out
 * of date ACTRSession), 2) debug events from the ACTR runtime.
 * 
 * @author harrison
 */
public class LayoutModifier implements ISessionManagerListener
{
  /**
   * Logger definition
   */
  static private final transient Log   LOGGER                  = LogFactory
                                                                   .getLog(LayoutModifier.class);

  static public final String           ENABLE_AUTO_LAYOUT_PREF = LayoutModifier.class
                                                                   .getName()
                                                                   + ".enable";

  private final IDebugEventSetListener _debugListener;

  public LayoutModifier()
  {

    _debugListener = new IDebugEventSetListener() {

      public void handleDebugEvents(DebugEvent[] events)
      {
        for (DebugEvent event : events)
        {
          if (!(event.getSource() instanceof ACTRDebugElement)) continue;

          int details = event.getDetail();
          if (details == DebugEvent.BREAKPOINT) switchToDebugView(true);
          // Display.getDefault().asyncExec(new Runnable() {
          //
          // public void run()
          // {
          // }
          //
          // });
        }
      }
    };

    DebugPlugin.getDefault().addDebugEventListener(_debugListener);
  }

  public void sessionAdded(ISession session)
  {
    // if (!RuntimePlugin.getDefault().getPreferenceStore()
    // .getBoolean(ENABLE_AUTO_LAYOUT_PREF)) return;

    if (hasDebugTarget(session))
      switchToDebugView(false);
    else if (isNotIterative(session)) switchToRuntimeView();
  }

  public void sessionRemoved(ISession session)
  {
  }

  private IWorkbenchPage getWorkbenchPage()
  {
    IWorkbench workbench = PlatformUI.getWorkbench();

    IWorkbenchWindow window = PlatformUI.getWorkbench()
        .getActiveWorkbenchWindow();
    if (window == null)
    {
      IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
      if (windows.length == 0) return null;
      window = windows[0];
    }

    IWorkbenchPage page = window.getActivePage();
    if (page == null) return null;

    return page;
  }

  private IPerspectiveDescriptor getPerspective()
  {
    IWorkbenchPage page = getWorkbenchPage();
    if (page == null) return null;
    return page.getPerspective();
  }

  private void switchToRuntimeView()
  {
    if (isInPerspective(ACTRRuntimePerspective.ID)) return;
    /*
     * query
     */
    IPreferenceStore prefs = RuntimePlugin.getDefault().getPreferenceStore();
    boolean dontAsk = prefs.getBoolean(RuntimePreferences.DONT_ASK_RUN_SWITCH);
    int rtnCode = 2; // yes

    if (!dontAsk)
    {
      MessageDialogWithToggle mdwt = MessageDialogWithToggle
          .openYesNoQuestion(
              PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
              "Switch to jACT-R Run Perspective?",
              "Models are best inspected using the jACT-R Run perspective. Switch now?",
              "Don't ask again", false, prefs,
              RuntimePreferences.DONT_ASK_RUN_SWITCH);

      rtnCode = mdwt.getReturnCode();

      if (mdwt.getToggleState())
      {
        prefs.setValue(RuntimePreferences.SWITCH_TO_RUN_PERSPECTIVE,
            rtnCode == 2);
        prefs.setValue(RuntimePreferences.DONT_ASK_RUN_SWITCH, true);
      }
      else
        prefs.setValue(RuntimePreferences.DONT_ASK_RUN_SWITCH, false);
    }
    else if (prefs.getBoolean(RuntimePreferences.SWITCH_TO_RUN_PERSPECTIVE))
      rtnCode = 2;
    else
      rtnCode = 3;

    if (rtnCode == 2) switchToPerspective(ACTRRuntimePerspective.ID);
  }

  private void switchToDebugView(boolean dueToBreakpoint)
  {
    if (isInPerspective("org.eclipse.debug.ui.DebugPerspective")) return;

    IPreferenceStore prefs = RuntimePlugin.getDefault().getPreferenceStore();
    boolean dontAsk = prefs
        .getBoolean(RuntimePreferences.DONT_ASK_DEBUG_SWITCH);
    int rtnCode = 2;

    if (!dontAsk)
    {
      MessageDialogWithToggle mdwt = MessageDialogWithToggle
          .openYesNoQuestion(
              PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
              dueToBreakpoint ? "Switch to jACT-R Run Perspective?"
                  : "Switch to Debug Perspective?",
              dueToBreakpoint ? "Model breakpoint detected! Switch to debug perspective?"
                  : "Debug models start suspended. To resume, use the Debug Perspective. Switch now?",
              "Don't ask again", false, prefs,
              RuntimePreferences.DONT_ASK_DEBUG_SWITCH);

      rtnCode = mdwt.getReturnCode();

      if (mdwt.getToggleState())
      {
        prefs.setValue(RuntimePreferences.SWITCH_TO_DEBUG_PERSPECTIVE,
            rtnCode == 2);
        prefs.setValue(RuntimePreferences.DONT_ASK_DEBUG_SWITCH, true);
      }
      else
        prefs.setValue(RuntimePreferences.DONT_ASK_DEBUG_SWITCH, false);
    }
    else if (prefs.getBoolean(RuntimePreferences.SWITCH_TO_DEBUG_PERSPECTIVE))
      rtnCode = 2;
    else
      rtnCode = 3;

    if (rtnCode == 2)
      switchToPerspective("org.eclipse.debug.ui.DebugPerspective");
  }

  private boolean isInPerspective(String perspectiveId)
  {
    IPerspectiveDescriptor perspective = getPerspective();
    return perspectiveId.equals(perspective.getId());
  }

  private void switchToPerspective(String perspectiveId)
  {
    IPerspectiveDescriptor perspective = getPerspective();
    if (!perspective.getId().equals(perspectiveId))
      getWorkbenchPage().setPerspective(
          getWorkbenchPage().getWorkbenchWindow().getWorkbench()
              .getPerspectiveRegistry().findPerspectiveWithId(perspectiveId));
  }

  private boolean isNotIterative(ISession session)
  {
    if (session instanceof Session2SessionAdapter)
    {
      AbstractSession oldSession = ((Session2SessionAdapter) session)
          .getOldSession();
      if (oldSession instanceof IterativeSession) return false;
    }

    return true;
  }

  private boolean hasDebugTarget(ISession session)
  {
    if (session instanceof ILocalSession)
    {
      ILocalSession lSession = (ILocalSession) session;
      ILaunch launch = lSession.getLaunch();
      if (launch.getLaunchMode().equals(ILaunchManager.DEBUG_MODE))
        return true;
    }

    return false;
  }

}
