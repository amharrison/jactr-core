package org.commonreality.sensors.motor;

/*
 * default logging
 */
import org.commonreality.modalities.motor.MovementCommand;
import org.commonreality.object.IAgentObject;
import org.commonreality.sensors.handlers.EfferentCommandHandler;


/**
 * delegate interface to actually perform some motor movement
 * @author harrison
 *
 */
public interface IActuator
{

  public void start(IAgentObject agent, MovementCommand movement, EfferentCommandHandler handler);
  
  public void abort(IAgentObject agent, MovementCommand movement, EfferentCommandHandler handler);
}
