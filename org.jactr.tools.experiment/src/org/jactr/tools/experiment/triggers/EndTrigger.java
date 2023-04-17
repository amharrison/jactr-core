package org.jactr.tools.experiment.triggers;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.tools.experiment.IExperiment;

public class EndTrigger extends ImmediateTrigger
{
  public EndTrigger(IExperiment experiment)
  {
    super(experiment);
    // TODO Auto-generated constructor stub
  }

  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(EndTrigger.class);

}
