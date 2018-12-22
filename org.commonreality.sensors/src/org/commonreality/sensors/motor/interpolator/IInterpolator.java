package org.commonreality.sensors.motor.interpolator;

/*
 * default logging
 */
import org.commonreality.modalities.motor.MovementCommand;
import org.commonreality.object.IAgentObject;
import org.commonreality.sensors.handlers.EfferentCommandHandler;

public interface IInterpolator
{

  public void start(IAgentObject agent, MovementCommand command);

  public void abort(IAgentObject agent, MovementCommand command);

  /**
   * update all the pending movements. the value returned is the time of the
   * next start/complete of the earliest event. It will return NaN if there are
   * no events pending
   * 
   * @param currentTime
   * @return
   */
  public double update(double currentTime);

}
