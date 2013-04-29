package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.control.ISessionController;
import org.jactr.eclipse.runtime.session.control.ISessionController2;
import org.jactr.eclipse.ui.images.JACTRImages;

/**
 * general action that handles most cases for the play/pause session control.
 * When using this, you either provide it with the selection provider that will
 * propogate session selection events, or you must call setCurrentSession when
 * the session selection changes.
 * 
 * @author harrison
 */
public class PlayPauseAction extends Action implements
    ISessionSelectionListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER     = LogFactory
                                                    .getLog(PlayPauseAction.class);

  static private final IMenuCreator  EMPTY_MENU = new IMenuCreator() {
                                                  public void dispose()
                                                  {
                                                    // do nothing
                                                  }

                                                  public Menu getMenu(
                                                      Control parent)
                                                  {
                                                    // do nothing
                                                    return null;
                                                  }

                                                  public Menu getMenu(
                                                      Menu parent)
                                                  {
                                                    // do nothing
                                                    return null;
                                                  }
                                                };

  private ISession                   _currentSession;

  private RunContentMenuCreator      _menuCreator;

  public PlayPauseAction()
  {
    super("", AS_DROP_DOWN_MENU);

    mark(false, "Play/Pause",
        JACTRImages.getImageDescriptor(JACTRImages.RESUME));
  }

  public void refresh()
  {
    updateStateAndIcons();
  }

  public void sessionSelected(ISession selectedSession)
  {
    if (_currentSession != null && !_currentSession.equals(selectedSession))
      detachSession(_currentSession);

    if (_currentSession != selectedSession) attachSession(selectedSession);

    updateStateAndIcons();
  }

  protected void detachSession(ISession session)
  {
    if (session == _currentSession) _currentSession = null;
  }

  protected void attachSession(ISession session)
  {
    _currentSession = session;
    ISessionController2 controller2 = null;

    if (session != null)
    {
      ISessionController controller = session.getController();
      if (controller instanceof ISessionController2)
        controller2 = (ISessionController2) controller;
    }

    if (controller2 == null)
    {
      setMenuCreator(null);
      _menuCreator = null;
    }
    else
    {
      if (_menuCreator != null) _menuCreator.dispose();

      _menuCreator = new RunContentMenuCreator(controller2,
          new RunLabelProvider());
      setMenuCreator(_menuCreator);
    }
  }

  public ISession getCurrentSession()
  {
    return _currentSession;
  }

  protected void markInactive()
  {
    mark(false, "Runtime inactive",
        JACTRImages.getImageDescriptor(JACTRImages.RESUME));
  }

  protected void mark(boolean enabled, String toolTip, ImageDescriptor img)
  {
    setEnabled(enabled);
    setToolTipText(toolTip);
    setImageDescriptor(img);
  }

  protected void updateStateAndIcons()
  {
    ISession session = getCurrentSession();
    ISessionController controller = session == null ? null : session
        .getController();
    if (session == null || controller == null)
    {
      markInactive();
      return;
    }

    if (_menuCreator != null) _menuCreator.refresh();

    if (controller.isTerminated() || !session.isOpen()) markInactive();

    if (controller.canSuspend())
    {
      mark(true, "Suspend", JACTRImages.getImageDescriptor(JACTRImages.SUSPEND));
      setMenuCreator(null);
    }

    if (controller.canResume())
    {
      IAction last = getLastRunForAction();

      mark(true, last == null ? "Resume" : last.getText(),
          JACTRImages.getImageDescriptor(JACTRImages.RESUME));
    }
  }

  @Override
  public void run()
  {
    ISession session = getCurrentSession();
    if (session == null) return;

    ISessionController controller = session.getController();
    if (controller == null) return;
    if (!controller.isRunning()) return;

    IAction lastRunForAction = getLastRunForAction();
    if (lastRunForAction != null)
      {
      lastRunForAction.run();
      return;
      }

    if (controller.isSuspended() && controller.canResume())
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("resuming %s", session));
      try
      {
        controller.resume();
      }
      catch (Exception e)
      {
        RuntimePlugin.error("Failed to resume", e);
      }
    }
    else if (!controller.isSuspended() && controller.canSuspend())
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("suspending %s", session));
      try
      {
        controller.suspend();
      }
      catch (Exception e)
      {
        RuntimePlugin.error("Failed to suspend ", e);
      }
    }
  }

  private IAction getLastRunForAction()
  {
    if (_menuCreator != null)
    {
      IAction lastAction = _menuCreator.getLastRun();
      if (lastAction != null) return lastAction;
    }
    return null;
  }
}
