package org.commonreality.sensors.keyboard;

/*
 * default logging
 */
import org.commonreality.sensors.handlers.EfferentCommandHandler;
import org.commonreality.sensors.keyboard.map.IDeviceMap;
import org.commonreality.sensors.motor.IActuator;
import org.commonreality.sensors.motor.interpolator.IActuatorCompletion;

public interface IKeyboardActuator extends IActuator, IActuatorCompletion
{

  public void setHandler(EfferentCommandHandler handler);
  
  public void setDevice(IDeviceMap deviceMap);
}
;