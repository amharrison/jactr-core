package org.jactr.tools.experiment.actions.jactr;

/*
 * default logging
 */
import java.util.concurrent.Future;

import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.core.runtime.controller.IController;
import org.jactr.tools.experiment.actions.IAction;
import org.jactr.tools.experiment.impl.IVariableContext;
import org.slf4j.LoggerFactory;

public class WaitForACTRAction implements IAction
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(WaitForACTRAction.class);

  private boolean                    _waitForStart = true;

  /**
   * @param waitForStart
   *          false if waiting for completion
   */
  public WaitForACTRAction(boolean waitForStart)
  {
    _waitForStart = waitForStart;
  }

  public void fire(IVariableContext context)
  {
    IController controller = ACTRRuntime.getRuntime().getController();

    if (controller == null)
      return;

    try
    {
      Future<Boolean> future = null;
      if (_waitForStart)
        future = controller.waitForStart();
      else
        future = controller.waitForCompletion();

      if (!future.get())
        throw new IllegalStateException("Runtime has stopped prematurely");
    }
    catch (IllegalStateException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Was unable to wait for ACTR ", e);
    }
  }

}
