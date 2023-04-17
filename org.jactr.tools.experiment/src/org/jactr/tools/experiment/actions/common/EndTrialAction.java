package org.jactr.tools.experiment.actions.common;

import org.jactr.tools.experiment.IExperiment;
import org.jactr.tools.experiment.actions.IAction;
import org.jactr.tools.experiment.impl.IVariableContext;
import org.jactr.tools.experiment.trial.ITrial;

/*
 * default logging
 */

 
import org.slf4j.LoggerFactory;

public class EndTrialAction implements IAction
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(EndTrialAction.class);

  private IExperiment                _experiment;

  private ITrial                                  _trial;

  public EndTrialAction(IExperiment experiment)
  {
    _experiment = experiment;
  }

  public EndTrialAction(ITrial trial, IExperiment experiment)
  {
    _trial = trial;
    _experiment = experiment;
  }

  public void fire(IVariableContext context)
  {
    ITrial current = _trial;
    if (current == null) current = _experiment.getTrial();
    if (current != null) if (current.isRunning()) current.stop();
  }
}
