package org.jactr.modules.pm.motor.command.translators;

import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;

public class HandToMouse extends AbstractHandToTranslator
{

  public HandToMouse()
  {

  }

  @Override
  public boolean handles(ChunkTypeRequest request)
  {
    return handles("hand-to-mouse", request);
  }

  @Override
  protected void testPosition(IModel model) throws IllegalArgumentException
  {
    // check to see if we are on mouse
    if (!rightHandIsOnHome(model))
      throw new IllegalArgumentException("Right hand is not on home");
  }

  @Override
  protected double[] getTarget(ChunkTypeRequest request)
  {
    return new double[] { 28, 2 };
  }

}
