package org.jactr.tools.experiment.bootstrap;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.tools.experiment.IExperiment;

public class Main
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Main.class);

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    IExperiment experiment = StartModelExperiments.loadExperiment(args[0], null);
    experiment.start();
  }

}
