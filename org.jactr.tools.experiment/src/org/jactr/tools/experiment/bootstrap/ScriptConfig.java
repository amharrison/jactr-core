package org.jactr.tools.experiment.bootstrap;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.model.IModel;
import org.jactr.scripting.IScriptConfigurator;
import org.jactr.scripting.IScriptableFactory;
import org.jactr.scripting.ScriptSupport;
import org.jactr.tools.experiment.misc.ExperimentUtilities;

public class ScriptConfig implements IScriptConfigurator
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ScriptConfig.class);

  @Override
  public void configure(IScriptableFactory factory, IModel model,
      ScriptSupport support, Object scope)
  {
    // blindly assuming javascript
    support.setGlobal("jactrExperiment",
        ExperimentUtilities.getModelsExperiment(model));
  }
}
