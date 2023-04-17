package org.jactr.tools.experiment.actions.common;

import java.util.concurrent.ExecutionException;

import org.commonreality.time.IClock;
import org.jactr.core.model.IModel;
import org.jactr.tools.experiment.IExperiment;
import org.jactr.tools.experiment.actions.AbstractAction;
import org.jactr.tools.experiment.impl.IVariableContext;

public class DelayAction extends AbstractAction
{

  double _delayInSeconds;

  public DelayAction(double seconds, IExperiment experiment)
  {
    super(experiment);
    _delayInSeconds = seconds;
  }

  @Override
  protected void fire(IModel model, IExperiment experiment,
      IVariableContext context)
  {

    try
    {
      IClock clock = experiment.getClock();
      clock.waitForTime(clock.getTime() + _delayInSeconds).get();
    }
    catch (InterruptedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (ExecutionException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
