package org.commonreality.sensors.swing;

/*
 * default logging
 */
import java.awt.Robot;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.executor.InlineExecutor;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.manager.IAgentObjectManager;
import org.commonreality.sensors.base.BaseSensor;
import org.commonreality.sensors.handlers.EfferentCommandHandler;
import org.commonreality.sensors.handlers.ICommandTimingEquation;
import org.commonreality.sensors.keyboard.KeyboardMotorHandler;
import org.commonreality.sensors.keyboard.MuscleUtilities;
import org.commonreality.sensors.keyboard.SerialDurationEquation;
import org.commonreality.sensors.keyboard.map.ACTRDeviceMap;
import org.commonreality.sensors.keyboard.map.IDeviceMap;
import org.commonreality.sensors.motor.IActuator;
import org.commonreality.sensors.motor.interpolator.BasicInterpolator;
import org.commonreality.sensors.motor.interpolator.IActuatorCompletion;
import org.commonreality.sensors.motor.interpolator.IInterpolator;
import org.commonreality.sensors.motor.interpolator.InterpolatorActuator;

/**
 * provides interpolated motor control interfaced directly to the keyboard and
 * mouse via {@link Robot}.
 * 
 * @author harrison
 */
public class InterpolatedMotorInterface
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(InterpolatedMotorInterface.class);

  private IDeviceMap                 _deviceMap;

  private ICommandTimingEquation     _durationEquation;

  private IInterpolator              _interpolator;

  private IActuator                  _actuator;

  private IActuatorCompletion        _completion;

  private BaseSensor                    _sensor;

  private EfferentCommandHandler     _commandHandler;

  private KeyboardMotorHandler       _keyboardHandler;

  public InterpolatedMotorInterface(BaseSensor sensor)
  {
    _sensor = sensor;

   
  }

  public void initialize(URL configuration) throws Exception
  {
    _deviceMap = new ACTRDeviceMap();
    /*
     * listens for efferent commands and routes specific types to delegate
     * handlers, in this case the keyboard motor handler
     */
    _commandHandler = new EfferentCommandHandler(_sensor);
    _keyboardHandler = new KeyboardMotorHandler();

    _commandHandler.add(_keyboardHandler);

    /*
     * keyboard will send this equation out for the estimation of completion
     * time
     */
    _keyboardHandler
        .setTimingEquation(_durationEquation = new SerialDurationEquation());

    /*
     * the actual work will be done by an actuator, in particular, one that can
     * be notified of interpolation events. This is where the magic happens
     */

    SwingActuator actuator = new SwingActuator(_commandHandler, _deviceMap);
    _actuator = actuator;
    _completion = actuator;

    /*
     * the interpolator gets called ever iteration (but not necessarily at a
     * fixed interval, it depends on configuration) to update the interpolated
     * position of the motors and notifying the completion (to position the
     * mouse and intermediate finger positions)
     */
    _interpolator = new BasicInterpolator(_commandHandler, _actuator,
        _completion);

    /*
     * and associate...
     */
    _keyboardHandler.setActuator(new InterpolatorActuator(_interpolator));

    /*
     * now, let's start listening for commands
     */
    _sensor.getEfferentCommandManager().addListener(_commandHandler,
        InlineExecutor.get());
  }
  
  public void start() throws Exception
  {
    /*
     * make sure the connected agents (preferrably one) have hands, etc..
     */
    Runnable adder = new Runnable() {
      public void run()
      {
        IAgentObjectManager agentManager = _sensor.getAgentObjectManager();
        Collection<IIdentifier> agentIds = agentManager.getIdentifiers();
        for (IIdentifier agentId : agentIds)
        {
          IAgentObject agent = agentManager.get(agentId);
          MuscleUtilities util = new MuscleUtilities(_sensor, agent);
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("Creating hands for " + agentId);
          util.create(true, true, _deviceMap);
        }
      }
    };
    
    
    _sensor.execute(adder);
  }
  
  public void stop() throws Exception
  {
    
  }

  public void dispose()
  {
    /*
     * and stop listening
     */
    _sensor.getEfferentCommandManager().removeListener(_commandHandler);
  }

  /**
   * note: the interpolator sends commands immediately and doesn't batch send like the base sensor..
   * @return
   */
  protected double process()
  {
    return _interpolator.update(_sensor.getClock().getTime());
  }
}
