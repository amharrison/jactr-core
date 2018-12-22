package org.commonreality.sensors.keyboard;

/*
 * default logging
 */
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.motor.MotorConstants;
import org.commonreality.modalities.motor.MotorUtilities;
import org.commonreality.modalities.motor.MovementCommand;
import org.commonreality.modalities.motor.TranslateCommand;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.delta.DeltaTracker;
import org.commonreality.sensors.handlers.EfferentCommandHandler;
import org.commonreality.sensors.keyboard.map.IDeviceMap;
import org.commonreality.sensors.motor.interpolator.BasicActuatorCompletion;

public class DefaultActuator extends BasicActuatorCompletion implements
    IKeyboardActuator
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(DefaultActuator.class);

  private IDeviceMap                 _deviceMap;

  /**
   * must call {@link #setHandler(EfferentCommandHandler)} and
   * {@link #setDevice(IDeviceMap)} before using
   */
  public DefaultActuator()
  {

  }

  public DefaultActuator(EfferentCommandHandler handler, IDeviceMap deviceMap)
  {
    super(handler);
    setDevice(deviceMap);
  }

  public void setDevice(IDeviceMap deviceMap)
  {
    _deviceMap = deviceMap;
  }


  public void abort(IAgentObject agent, MovementCommand movement,
      EfferentCommandHandler handler)
  {
    // no op
  }

  public void start(IAgentObject agent, MovementCommand movement,
      EfferentCommandHandler handler)
  {
    // no op
  }

  @Override
  public void aborted(IAgentObject agent, MovementCommand command,
      Object extraInfo)
  {
    super.aborted(agent, command, extraInfo);
  }

  @Override
  public void completed(IAgentObject agent, MovementCommand command,
      Object extraInfo)
  {
    if (command instanceof PressCommand)
      press((PressCommand) command, getHandler());
    else if (command instanceof ReleaseCommand)
      release((ReleaseCommand) command, getHandler());

    super.completed(agent, command, extraInfo);
  }

  @Override
  public void updated(IAgentObject agent, MovementCommand command,
      DeltaTracker<IMutableObject> motorTracker)
  {
    if (command instanceof TranslateCommand)
    {
      IEfferentObject mouse = (IEfferentObject) motorTracker.get();
      if (MotorUtilities.isMotor(mouse)
          && MotorUtilities.getName(mouse).equalsIgnoreCase("mouse"))
      positionMouse((TranslateCommand) command, getHandler(), mouse,
          (double[]) motorTracker.getProperty(MotorConstants.POSITION));
    }
  }

  protected int getCode(MovementCommand command, EfferentCommandHandler handler)
  {
    /*
     * snag the target
     */
    double[] target = command.getTarget();
    int keyCode = 0;

    IEfferentObject muscle = handler.getSensor().getEfferentObjectManager()
        .get(command.getEfferentIdentifier());
    String name = MotorUtilities.getName(muscle);
    if ("mouse".equals(name))
      keyCode = _deviceMap.getMouseButton(target);
    else
      keyCode = _deviceMap.getKey(target);

    return keyCode;
  }

  protected void press(PressCommand command, EfferentCommandHandler handler)
  {
    int keyCode = getCode(command, handler);
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Pressing " + keyCode + " ("
          + KeyStroke.getKeyStroke(keyCode, 0, false) + ")");
  }

  protected void release(ReleaseCommand command, EfferentCommandHandler handler)
  {
    int keyCode = getCode(command, handler);
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Releasing " + keyCode + " ("
          + KeyStroke.getKeyStroke(keyCode, 0, true) + ")");
  }

  protected void positionMouse(TranslateCommand command,
      EfferentCommandHandler handler, IEfferentObject mouse, double[] position)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Placing mouse @ %.2f, %.2f", position[0],
          position[1]));
  }

}
