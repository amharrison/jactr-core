package org.commonreality.sensors.motor.interpolator;

import org.commonreality.modalities.motor.MovementCommand;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.delta.DeltaTracker;

/*
 * default logging
 */

/**
 * marker for the completion of acuator behavior
 * @author harrison
 *
 */
public interface IActuatorCompletion
{

  public void completed(IAgentObject agent, MovementCommand command, Object extraInfo);
  
  public void aborted(IAgentObject agent, MovementCommand command, Object extraInfo);
  
  /**
   * @param agent
   * @param command
   * @param motorTracker
   */
  public void updated(IAgentObject agent, MovementCommand command,
      DeltaTracker<IMutableObject> motorTracker);
}
