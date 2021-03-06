package org.jactr.tools.experiment.actions.common;

/*
 * default logging
 */

 
import org.slf4j.LoggerFactory;
import org.jactr.tools.experiment.IExperiment;
import org.jactr.tools.experiment.actions.IAction;
import org.jactr.tools.experiment.impl.IVariableContext;

public class UnlockAction implements IAction
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(UnlockAction.class);
  
  private IExperiment _experiment;
  private String _name;
  
  public UnlockAction(String lockName, IExperiment experiment)
  {
    _experiment = experiment;
    _name = lockName;
  }

  public void fire(IVariableContext context)
  {
    String lock = _experiment.getVariableResolver().resolve(_name, context).toString();
   _experiment.getLockManager().unlock(lock); 
  }

}
