package org.jactr.tools.misc;

/*
 * default logging
 */
import java.util.concurrent.Future;

 
import org.slf4j.LoggerFactory;
import org.jactr.core.model.IModel;
import org.jactr.core.module.procedural.six.learning.IProceduralLearningModule6;

public class ProceduralUtilities
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ProceduralUtilities.class);

  /**
   * reward/punish the model using {@link #executeLater(IModel, Runnable)}. This
   * assumes that the model has {@link IProceduralLearningModule6} installed
   * 
   * @param model
   * @param reward
   * @return
   * @throws IllegalStateException
   */
  static public Future<Boolean> reward(final IModel model, final double reward)
      throws IllegalStateException
  {
    return ExecutionUtilities.executeNow(model, new Runnable() {

      public void run()
      {
        IProceduralLearningModule6 plm = (IProceduralLearningModule6) model
            .getModule(IProceduralLearningModule6.class);

        if (plm != null)
          plm.reward(reward);
        else if (LOGGER.isWarnEnabled())
          LOGGER.warn(String.format(
              "Cannot reward %1$s, no IProceduralLearningModule6 installed.",
              model));
      }

    });
  }
}
