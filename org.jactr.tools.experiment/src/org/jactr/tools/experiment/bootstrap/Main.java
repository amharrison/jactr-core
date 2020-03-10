package org.jactr.tools.experiment.bootstrap;

import java.util.concurrent.Executors;

import org.commonreality.time.impl.RealtimeClock;
import org.jactr.tools.experiment.IExperiment;
import org.jactr.tools.experiment.dc.DataCollector;

/*
 * default logging
 */

import org.slf4j.LoggerFactory;

public class Main
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(Main.class);

  /**
   * @param args
   */
  public static void main(String[] args)
  {
    IExperiment experiment = StartModelExperiments.loadExperiment(args[0],
        (exp) -> {
          exp.setClock(
              new RealtimeClock(Executors.newSingleThreadScheduledExecutor()));
          exp.getVariableContext().set(StartModelExperiments.SUBJECT_ID,
              DataCollector.createSubjectId(System.currentTimeMillis()));
        });

    experiment.start();
  }

}
