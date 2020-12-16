package org.jactr.modules.pm.motor.command.translators;

import org.commonreality.agents.IAgent;
import org.commonreality.efferent.ICompoundCommand;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.modalities.motor.MotorUtilities;
import org.commonreality.modalities.motor.TranslateCommand;
import org.commonreality.object.IEfferentObject;
import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.modules.pm.motor.IMotorModule;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class PeckRecoilTranslator extends PeckTranslator
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(PeckRecoilTranslator.class);

  @Override
  public boolean handles(ChunkTypeRequest request)
  {
    return handles("peck-recoil", request);
  }

  @Override
  public IEfferentCommand translate(ChunkTypeRequest request,
      IEfferentObject muscle, IModel model) throws IllegalArgumentException
  {
    try
    {
      IAgent agent = ACTRRuntime.getRuntime().getConnector().getAgent(model);
      IMotorModule motor = (IMotorModule) model.getModule(IMotorModule.class);

      /*
       * use the peck configuration and add a final translate
       */
      ICompoundCommand compound = (ICompoundCommand) super.translate(request,
          muscle, model);

      TranslateCommand translate = (TranslateCommand) getTemplateNamed(
          "translate", muscle).instantiate(agent, muscle);

      double[] origin = MotorUtilities.getPosition(muscle);

      double[] target = getTarget(request, muscle);
      double[] peckRate = computeRate(
          target,
          origin,
          computeFitts(
          getPeckFittsCoefficient(motor),
              computeDistance(target, origin), 1));

      translate.translate(target, origin, peckRate);

      compound.add(translate);

      return compound;
    }
    catch (IllegalArgumentException iae)
    {
      throw iae;
    }
    catch (Exception e)
    {
      throw new IllegalArgumentException("Could not create command for "
          + request + " ", e);
    }
  }

}
