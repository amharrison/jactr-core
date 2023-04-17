package org.jactr.modules.pm.motor.command.translators;

import java.util.Optional;
import java.util.stream.Collectors;

import org.commonreality.agents.IAgent;
import org.commonreality.efferent.ICompoundCommand;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.modalities.motor.MotorUtilities;
import org.commonreality.modalities.motor.TranslateCommand;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.manager.IEfferentObjectManager;
import org.commonreality.sensors.keyboard.PressCommand;
import org.commonreality.sensors.keyboard.ReleaseCommand;
import org.commonreality.sensors.keyboard.map.ACTRDeviceMap;
import org.eclipse.collections.impl.block.factory.Comparators;
import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.core.slot.ISlot;
import org.jactr.modules.pm.motor.IMotorModule;

public class PressKeyTranslator extends AbstractManualTranslator
{

  ACTRDeviceMap _deviceMap = new ACTRDeviceMap();

  @Override
  public boolean handles(ChunkTypeRequest request)
  {
    return handles("press-key", request);
  }

  private double[] getKeyLocation(ChunkTypeRequest request)
      throws IllegalArgumentException
  {
    _recycledSlotContainer.clear();
    Optional<ISlot> keySlot = request.getSlots(_recycledSlotContainer).stream()
        .filter((s) -> s.getName().equals("key")).findFirst();

    if (!keySlot.isPresent()) throw new IllegalArgumentException(
        "press-key needs slot named key to the defined");

    String key = keySlot.get().getValue().toString().toUpperCase();
    int keyCode = _deviceMap.getKeyCode(key);
    return _deviceMap.getKeyLocation(keyCode);
  }

  private double distanceSquared(double[] muscleLocation, double[] location)
  {
    double deltaSq = 0;
    for (int i = 0; i < location.length; i++)
      deltaSq += Math.pow(muscleLocation[i] - location[i], 2);
    return deltaSq;
  }

  /**
   * using the key slot provided in request, find the finger closest to it. this
   * will be the efferent object that we build the command for.
   */
  @Override
  public IEfferentObject getMuscle(ChunkTypeRequest request, IModel model)
      throws IllegalArgumentException
  {
    final IEfferentObjectManager manager = ACTRRuntime.getRuntime()
        .getConnector().getAgent(model).getEfferentObjectManager();

    // the key to press should be specified in the request
    final double[] keyLocation = getKeyLocation(request);

    // the closest muscle
    Optional<IEfferentObject> closest = manager.getIdentifiers().stream()
        .map((id) -> {
          return manager.get(id);
        }).filter((eff) -> {
          return MotorUtilities.isMotor(eff) && isEndEffector(eff);
        }).collect(Collectors.minBy(Comparators.byDoubleFunction((eff) -> {
          double[] muscleLocation = MotorUtilities.getPosition(eff);
          if (muscleLocation == null) return Double.MAX_VALUE;
          return distanceSquared(muscleLocation, keyLocation);
        })));

    if (!closest.isPresent())
      throw new IllegalArgumentException("No muscle close to key?");

    return closest.get();
  }

  protected boolean isEndEffector(IEfferentObject efferentObject)
  {
    return MotorUtilities.getChildIdentifiers(efferentObject).size() == 0;
  }

  /**
   * build the actual command to move.
   */
  @Override
  public IEfferentCommand translate(ChunkTypeRequest request,
      IEfferentObject muscle, IModel model) throws IllegalArgumentException
  {
    try
    {
      IAgent agent = ACTRRuntime.getRuntime().getConnector().getAgent(model);
      IMotorModule motor = (IMotorModule) model.getModule(IMotorModule.class);

      // the key to press should be specified in the request
      double[] key2DLocation = getKeyLocation(request);
      double[] keyLocation = new double[] { key2DLocation[0], key2DLocation[1],
          1 };
      double[] muscleLocation = MotorUtilities.getPosition(muscle);

      /*
       * the movement will be translate, press, release, translate
       */
      ICompoundCommand compound = (ICompoundCommand) getTemplateNamed(
          "compound", muscle).instantiate(agent, muscle);

      TranslateCommand translateTo = (TranslateCommand) getTemplateNamed(
          "translate", muscle).instantiate(agent, muscle);
      TranslateCommand translateFrom = (TranslateCommand) getTemplateNamed(
          "translate", muscle).instantiate(agent, muscle);

      double[] peckRate = computeRate(muscleLocation, keyLocation,
          computeFitts(getPeckFittsCoefficient(motor),
              Math.sqrt(distanceSquared(muscleLocation, keyLocation)), 1));

      translateTo.translate(muscleLocation, keyLocation, peckRate);

      peckRate = computeRate(keyLocation, muscleLocation,
          computeFitts(getPeckFittsCoefficient(motor),
              Math.sqrt(distanceSquared(muscleLocation, keyLocation)), 1));

      translateFrom.translate(keyLocation, muscleLocation, peckRate);

      double[] punchTarget = new double[] { keyLocation[0], keyLocation[1], 0 };

      /*
       * all the time for a peck is eaten up by the movement, not the punch..
       * silly, but this is ACT-R cannonical
       */
      double[] punchRate = computeRate(keyLocation, punchTarget, 0.000001);

      PressCommand press = (PressCommand) getTemplateNamed("press", muscle)
          .instantiate(agent, muscle);
      press.press(keyLocation, punchTarget, punchRate);

      ReleaseCommand release = (ReleaseCommand) getTemplateNamed("release",
          muscle).instantiate(agent, muscle);
      release.release(punchTarget, keyLocation, punchRate);

      compound.add(translateTo);
      compound.add(press);
      compound.add(release);
      compound.add(translateFrom);

      return compound;
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
