package org.jactr.tools.experiment.misc;

import org.jactr.core.model.IModel;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.tools.experiment.IExperiment;
import org.jactr.tools.experiment.bootstrap.StartModelExperiments;
import org.jactr.tools.experiment.impl.IVariableContext;
import org.jactr.tools.experiment.impl.VariableResolver;

/*
 * default logging
 */

import org.slf4j.LoggerFactory;

public class ExperimentUtilities
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(ExperimentUtilities.class);

  static public IExperiment getModelsExperiment(IModel model)
  {
    IExperiment exp = (IExperiment) model
        .getMetaData(StartModelExperiments.MODELS_EXPERIMENT);

    if (exp == null) // stored in ACTRRuntime? by StartExperiment
      exp = (IExperiment) ACTRRuntime.getRuntime().getApplicationData();
    return exp;
  }

  static public IModel getExperimentsModel(IExperiment experiment)
  {
    VariableResolver resolver = experiment.getVariableResolver();
    IVariableContext context = experiment.getVariableContext();
    Object resolved = resolver.resolve(
        String.format("${%s}", StartModelExperiments.EXPERIMENT_MODEL),
        context);
    IModel model = null;

    if (resolved instanceof IModel) model = (IModel) resolved;

    if (model == null)
    {
      Object tmp = resolver.resolve("${=model}", context);
      if (tmp instanceof IModel) model = (IModel) tmp;
    }

    return model;
  }
}
