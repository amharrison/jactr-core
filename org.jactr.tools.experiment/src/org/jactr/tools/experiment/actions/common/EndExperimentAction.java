package org.jactr.tools.experiment.actions.common;

/*
 * default logging
 */

 
import org.slf4j.LoggerFactory;
import org.jactr.tools.experiment.IExperiment;
import org.jactr.tools.experiment.actions.IAction;
import org.jactr.tools.experiment.impl.IVariableContext;

public class EndExperimentAction implements IAction
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(EndExperimentAction.class);
  
  private final IExperiment _experiment;
  
  public EndExperimentAction(IExperiment experiment)
  {
    _experiment = experiment;
  }

  public void fire(IVariableContext context)
  {
    _experiment.stop();
  }

}
