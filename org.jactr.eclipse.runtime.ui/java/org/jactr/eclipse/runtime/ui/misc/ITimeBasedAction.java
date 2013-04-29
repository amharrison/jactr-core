package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */
import org.eclipse.jface.action.IAction;

public interface ITimeBasedAction extends IAction
{

  public double getActionTime();

  public void update(double currentTime);
}
