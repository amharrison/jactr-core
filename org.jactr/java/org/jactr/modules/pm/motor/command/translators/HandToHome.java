package org.jactr.modules.pm.motor.command.translators;

import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;

public class HandToHome extends AbstractHandToTranslator
{

  public HandToHome()
  {

  }

  @Override
  public boolean handles(ChunkTypeRequest request)
  {
    return handles("hand-to-home", request);
  }

  @Override
  protected void testPosition(IModel model) throws IllegalArgumentException
  {
    // check to see if we are on mouse
    if (!rightHandIsOnMouse(model))
      throw new IllegalArgumentException("Right hand is not on mouse");
  }

  @Override
  protected double[] getTarget(ChunkTypeRequest request)
  {
    return new double[] { 7, 4 };
  }

}
