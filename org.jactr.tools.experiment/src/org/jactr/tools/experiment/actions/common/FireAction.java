package org.jactr.tools.experiment.actions.common;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.tools.experiment.IExperiment;
import org.jactr.tools.experiment.actions.IAction;
import org.jactr.tools.experiment.impl.IVariableContext;

public class FireAction implements IAction
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(FireAction.class);
  private String _triggerName;
  private IExperiment _experiment;
  
  
  public FireAction(IExperiment experiment, String trigger)
  {
    _experiment = experiment;
    _triggerName = trigger;
  }

  public void fire(IVariableContext context)
  {
    String triggerName = _experiment.getVariableResolver().resolve(_triggerName, context).toString();
    _experiment.getTriggerManager().fire(triggerName, context);
  }

}
