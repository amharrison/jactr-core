package org.jactr.modules.pm.motor.command.translators;

import org.commonreality.agents.IAgent;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.modalities.motor.MotorUtilities;
import org.commonreality.object.IEfferentObject;
import org.commonreality.sensors.keyboard.PressCommand;
import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.modules.pm.motor.IMotorModule;

/*
 * default logging
 */

import org.slf4j.LoggerFactory;

public class PressTranslator extends AbstractManualTranslator
{
  /**
   * Logger definition
   */
  static final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(PressTranslator.class);

  public boolean handles(ChunkTypeRequest request)
  {
    return handles("press", request);
  }

  public IEfferentCommand translate(ChunkTypeRequest request,
      IEfferentObject muscle, IModel model) throws IllegalArgumentException
  {
    try
    {
      IAgent agent = ACTRRuntime.getRuntime().getConnector().getAgent(model);
      IMotorModule motor = (IMotorModule) model.getModule(IMotorModule.class);


      double[] origin = MotorUtilities.getPosition(muscle);
      double[] target = new double[] { origin[0], origin[1], 0 };
      double[] rate = computeRate(origin, target, getMotorBurstTime(motor) / 2);

      PressCommand press = (PressCommand) getTemplateNamed("press", muscle)
          .instantiate(agent, muscle);
      press.press(origin, target, rate);

      return press;
    }
    catch (IllegalArgumentException iae)
    {
      throw iae;
    }
    catch (Exception e)
    {
      throw new IllegalArgumentException(
          "Could not create command for " + request + " ", e);
    }
  }

}
