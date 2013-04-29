package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.control.ISessionController;
import org.jactr.eclipse.ui.images.JACTRImages;

public class TerminateAction extends Action implements
    ISessionSelectionListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(TerminateAction.class);

  private ISession                   _currentSession;

  public TerminateAction()
  {
    setEnabled(false);
    setToolTipText("");
    setImageDescriptor(JACTRImages.getImageDescriptor(JACTRImages.TERIMINATE));
  }

  public void sessionSelected(ISession selectedSession)
  {
    if (_currentSession != null && !_currentSession.equals(selectedSession))
      detachSession(_currentSession);

    if (_currentSession != selectedSession) attachSession(selectedSession);

    refresh();
  }

  public void refresh()
  {
    updateStateAndIcons();
  }


  protected void detachSession(ISession session)
  {
    if (session == _currentSession) _currentSession = null;
  }

  protected void attachSession(ISession session)
  {
    _currentSession = session;
  }

  public ISession getCurrentSession()
  {
    return _currentSession;
  }

  protected void mark(boolean enabled, String toolTip)
  {
    setEnabled(enabled);
    setToolTipText(toolTip);
  }

  protected void updateStateAndIcons()
  {
    ISession session = getCurrentSession();
    ISessionController controller = session == null ? null : session
        .getController();

    if (session == null || controller == null)
    {
      mark(false, "Runtime inactive");
      return;
    }

    if (controller.isTerminated() || !session.isOpen())
      mark(false, "Runtime inactive");

    if (controller.canTerminate() && controller.isRunning()) mark(true, "Terminate runtime");

  }

  @Override
  public void run()
  {
    ISession session = getCurrentSession();
    if (session == null) return;
    ISessionController controller = session.getController();
    if (controller == null) return;

    if (controller.canTerminate() && controller.isRunning())
      try
      {
        controller.terminate();
      }
      catch (Exception e)
      {
        RuntimePlugin.error("Failed to stop session", e);

        LOGGER.error(
            "AbstractRuntimeModelViewPart.stopSession threw Exception : ", e);
      }
  }
}
