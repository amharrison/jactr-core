package org.jactr.tools.experiment.triggers;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.tools.experiment.IExperiment;

public class StartTrigger extends ImmediateTrigger
{
  public StartTrigger(IExperiment experiment)
  {
    super(experiment);
  }

  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(StartTrigger.class);

  

}
