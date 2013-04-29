package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.marker.MarkerIndex.MarkerRecord;
import org.jactr.eclipse.runtime.session.control.ISessionController2;

public class TimeBasedAction extends Action implements ITimeBasedAction
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(TimeBasedAction.class);

  private Object                     _node;

  private boolean                    _isRunTo;

  private RunContentMenuCreator      _menuCreator;

  public TimeBasedAction(RunContentMenuCreator menuCreator, Object toWhen,
      boolean isRunTo)
  {
    super(toWhen.toString(), SWT.PUSH);
    _isRunTo = isRunTo;
    _node = toWhen;
    _menuCreator = menuCreator;
  }

  public double getActionTime()
  {
    if (_node instanceof Number) return ((Number) _node).doubleValue();
    if (_node instanceof MarkerRecord)
    {
      MarkerRecord record = (MarkerRecord) _node;
      return record._time;
    }
    return 0;
  }

  public void update(double currentTime)
  {
    if (_isRunTo)
    {
      boolean valid = currentTime < getActionTime();
      setEnabled(valid);
      if (!valid) setToolTipText("Time has passed");
    }
  }

  @Override
  public void run()
  {
    _menuCreator.setLastRun(null);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Firing %s", _node));

    ISessionController2 controller = _menuCreator.getController();

    if (_isRunTo)
    {
      if (controller.canRunTo(_node))
        try
        {
          controller.runTo(_node);
          _menuCreator.setLastRun(this);
        }
        catch (Exception e)
        {
          RuntimePlugin.error(
              String.format("Failed to run to %s", this.getText()), e);
        }
    }
    else if (controller.canRunFor(_node))
      try
      {
        controller.runFor(_node);
        _menuCreator.setLastRun(this);
      }
      catch (Exception e)
      {
        RuntimePlugin.error(
            String.format("Failed to run for %s", this.getText()), e);
      }
  }
}
