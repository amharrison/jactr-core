package org.jactr.eclipse.runtime.ui.looper;

/*
 * default logging
 */
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.eclipse.ui.UIPlugin;

public class LoopNotifier implements ILoopListener
{
  /**
   * Logger definition
   */
  static private final transient Log          LOGGER = LogFactory
                                                         .getLog(LoopNotifier.class);

  private volatile LoopDialog                 _dialog;

  private Map<ACTRSession, Set<List<String>>> _ignorePatterns;

  private List<String>                        _notifyingProductionLoop;

  private ACTRSession                         _notifyingSession;

  public LoopNotifier()
  {
    _ignorePatterns = new HashMap<ACTRSession, Set<List<String>>>();
  }

  public void loopDetected(final ACTRSession session, final String modelName,
      final List<String> productionLoop, final int iterations)
  {
    if (iterations <= 1) return;

    Set<List<String>> ignoreSet = _ignorePatterns.get(session);
    if (ignoreSet != null && ignoreSet.contains(productionLoop)) return;

    Display.getDefault().asyncExec(new Runnable() {

      public void run()
      {
        try
        {
          notifyUser(session, modelName, productionLoop, iterations);
        }
        catch (Exception e)
        {
          LOGGER.debug("Failed to notify user ", e);
        }
      }

    });
  }

  synchronized public void add(ACTRSession session)
  {
    _ignorePatterns.put(session, new HashSet<List<String>>());
  }

  synchronized public void clear(ACTRSession session)
  {
    Set<List<String>> ignore = _ignorePatterns.remove(session);
    if (ignore != null) ignore.clear();

    if (_dialog != null && _notifyingSession == session) _dialog.close();
  }

  synchronized protected void ignore(ACTRSession session, List<String> pattern)
  {
    Set<List<String>> ignore = _ignorePatterns.get(session);
    if (ignore != null) ignore.add(pattern);
  }

  private void notifyUser(ACTRSession session, String modelName,
      List<String> productionLoop, int iterations)
  {
    _notifyingProductionLoop = productionLoop;
    _notifyingSession = session;

    if (!session.isActive() || session.isDestroyed())
    {
      if (_dialog != null) _dialog.close();
      return;
    }

    String message = String.format(
        "Loop detected in %s. Sequence %s repeated %d times. Stop execution?",
        modelName, productionLoop, iterations);

    if (_dialog == null)
    {
      String toggleMessage = "Ignore identical patterns in this run.";

      _dialog = new LoopDialog(Display.getDefault().getActiveShell(),
          "Loop detected. Suspend?", null, message, MessageDialog.QUESTION,
          new String[] { IDialogConstants.STOP_LABEL,
              IDialogConstants.IGNORE_LABEL }, 0, toggleMessage, false);

      Display.getDefault().beep();
      _dialog.setBlockOnOpen(false);
      _dialog.open();
    }

    _dialog.setMessage(message);
  }

  protected void terminate(ACTRSession session)
  {
    if (session.isDestroyed() || !session.isActive()) return;

    try
    {
      session.stop();
    }
    catch (CoreException ce)
    {
      // should probably do something witht his..
      UIPlugin.log(ce.getStatus());
    }
  }

  private class LoopDialog extends MessageDialogWithToggle
  {

    public LoopDialog(Shell parentShell, String dialogTitle, Image image,
        String message, int dialogImageType, String[] dialogButtonLabels,
        int defaultIndex, String toggleMessage, boolean toggleState)
    {
      super(parentShell, dialogTitle, image, message, dialogImageType,
          dialogButtonLabels, defaultIndex, toggleMessage, toggleState);
    }

    public void setMessage(String message)
    {
      messageLabel.setText(message);
      getShell().pack();
    }

    private boolean _wasSet = false;

    @Override
    protected void setReturnCode(int code)
    {
      _wasSet = true;
      super.setReturnCode(code);
    }

    @Override
    protected Button createToggleButton(Composite parent)
    {
      Button button = super.createToggleButton(parent);
      button.addSelectionListener(new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e)
        {

        }

        public void widgetSelected(SelectionEvent e)
        {
          getButton(0).setFocus();
        }

      });

      return button;
    }

    @Override
    public boolean close()
    {
      if (_wasSet)
      {
        if (getToggleState())
          ignore(_notifyingSession, _notifyingProductionLoop);

        // yes
        if (getReturnCode() == IDialogConstants.STOP_ID)
          terminate(_notifyingSession);
      }
      _wasSet = false;
      _dialog = null;
      _notifyingSession = null;
      _notifyingProductionLoop = null;
      return super.close();
    }
  }
}
