package org.jactr.modules.pm.motor.command.translators;

import org.commonreality.agents.IAgent;
import org.commonreality.efferent.ICompoundCommand;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.motor.MotorUtilities;
import org.commonreality.modalities.motor.TranslateCommand;
import org.commonreality.object.IEfferentObject;
import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.modules.pm.motor.IMotorModule;

public abstract class AbstractHandToTranslator extends AbstractManualTranslator
{

  public AbstractHandToTranslator()
  {
    super();
  }

  abstract protected void testPosition(IModel model)
      throws IllegalArgumentException;

  abstract protected double[] getTarget(ChunkTypeRequest request);

  @Override
  public IEfferentCommand translate(ChunkTypeRequest request, IEfferentObject muscle, IModel model)
      throws IllegalArgumentException
  {
    testPosition(model);
    try
    {
      IAgent agent = ACTRRuntime.getRuntime().getConnector().getAgent(model);
      IMotorModule motor = (IMotorModule) model.getModule(IMotorModule.class);
      /*
       * we need a compound, since parent/child is not handled automatically, we
       * need to create a compound of the hand and its fingers
       */
      ICompoundCommand compound = (ICompoundCommand) getTemplateNamed(
          "compound", muscle).instantiate(agent, muscle);
      compound.setParallel(true);
  
      TranslateCommand translate = (TranslateCommand) getTemplateNamed(
          "translate", muscle).instantiate(agent, muscle);
  
      double[] origin = MotorUtilities.getPosition(muscle);
      double[] target = getTarget(request);
      double[] rate = computeRate(origin, target, computeFitts(
          getPeckFittsCoefficient(motor), computeDistance(origin, target), 1));
  
      translate.translate(origin, target, rate);
      compound.add(translate);
  
      for (IIdentifier fingerId : MotorUtilities.getChildIdentifiers(muscle))
      {
        IEfferentObject finger = agent.getEfferentObjectManager().get(fingerId);
  
        TranslateCommand fTranslate = (TranslateCommand) getTemplateNamed(
            "translate", muscle).instantiate(agent, muscle);
  
        double[] fOrigin = MotorUtilities.getPosition(finger);
  
        // relative
        double[] fOffset = new double[2];
        for (int i = 0; i < 2; i++)
          fOffset[i] = fOrigin[i] - origin[i];
  
        double[] fTarget = new double[] { 28 + fOffset[0], 2 + fOffset[1] };
  
        fTranslate.translate(fOrigin, fTarget, rate);
        compound.add(fTranslate);
      }
  
      return compound;
    }
    catch (Exception e)
    {
      throw new IllegalArgumentException(e);
    }
  }

}