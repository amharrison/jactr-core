package org.jactr.modules.pm.motor.command.translators;

import java.util.Optional;

import org.commonreality.agents.IAgent;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.modalities.motor.MotorUtilities;
import org.commonreality.modalities.motor.TranslateCommand;
import org.commonreality.object.IEfferentObject;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.core.slot.ISlot;
import org.jactr.modules.pm.motor.IMotorModule;
import org.jactr.modules.pm.visual.IVisualModule;

public class MoveCursorTranslator extends AbstractManualTranslator
{

  public MoveCursorTranslator()
  {
    // TODO Auto-generated constructor stub
  }

  @Override
  public boolean handles(ChunkTypeRequest request)
  {
    return handles("move-cursor", request);
  }

  protected double[] getTarget(ChunkTypeRequest request)
      throws IllegalArgumentException
  {
    _recycledSlotContainer.clear();
    Optional<ISlot> locSlot = request.getSlots(_recycledSlotContainer).stream()
        .filter((s) -> s.getName().equals("location")).findFirst();

    if (!locSlot.isPresent()) throw new IllegalArgumentException(
        "move-cursor needs slot named location to the defined");

    IChunk visLoc = (IChunk) locSlot.get().getValue();
    double x = ((Number) visLoc.getSymbolicChunk()
        .getSlot(IVisualModule.SCREEN_X_SLOT).getValue()).doubleValue();
    double y = ((Number) visLoc.getSymbolicChunk()
        .getSlot(IVisualModule.SCREEN_Y_SLOT).getValue()).doubleValue();
    return new double[] { x, y };
  }

  @Override
  public IEfferentCommand translate(ChunkTypeRequest request,
      IEfferentObject muscle, IModel model) throws IllegalArgumentException
  {
    if (!rightHandIsOnMouse(model))
      LOGGER.warn("Right hand is not on mouse");

    double[] target = getTarget(request);

    try
    {
      IAgent agent = ACTRRuntime.getRuntime().getConnector().getAgent(model);
      IMotorModule motor = (IMotorModule) model.getModule(IMotorModule.class);

      TranslateCommand translate = (TranslateCommand) getTemplateNamed(
          "translate", muscle).instantiate(agent, muscle);

      double[] origin = MotorUtilities.getPosition(muscle);

      double[] rate = computeRate(origin, target,
          computeFitts(getPeckFittsCoefficient(motor),
              computeDistance(origin, target), 0.5));

      translate.translate(origin, target, rate);

      return translate;
    }
    catch (Exception e)
    {
      throw new IllegalArgumentException(e);
    }
  }

}
