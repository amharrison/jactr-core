package org.commonreality.sensors.swing;

/*
 * default logging
 */
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.MouseEvent;

import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.motor.TranslateCommand;
import org.commonreality.object.IEfferentObject;
import org.commonreality.reality.CommonReality;
import org.commonreality.sensors.ISensor;
import org.commonreality.sensors.handlers.EfferentCommandHandler;
import org.commonreality.sensors.keyboard.DefaultActuator;
import org.commonreality.sensors.keyboard.DefaultKeyboardSensor;
import org.commonreality.sensors.keyboard.PressCommand;
import org.commonreality.sensors.keyboard.ReleaseCommand;
import org.commonreality.sensors.keyboard.map.IDeviceMap;
import org.commonreality.sensors.swing.processors.SizeAndLocationProcessor;

/**
 * actuator for the {@link DefaultKeyboardSensor} that allows it to talk with
 * the AWT robot interface
 * 
 * @author harrison
 */
public class SwingActuator extends DefaultActuator
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SwingActuator.class);

  private Robot                      _robot;

  private SizeAndLocationProcessor   _sizeAndLocationProcessor;

  public SwingActuator(EfferentCommandHandler handler, IDeviceMap deviceMap)
  {
    super(handler, deviceMap);
    try
    {
      _robot = new Robot();
    }
    catch (AWTException e)
    {
      LOGGER.error("SwingActuator.SwingActuator threw AWTException : ", e);
      throw new IllegalStateException("Could not create robot", e);
    }
  }

  protected void press(PressCommand command, EfferentCommandHandler handler)
  {
    int keyCode = getCode(command, handler);
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("pressing " + keyCode + " ("
          + KeyStroke.getKeyStroke(keyCode, 0, false) + ")");

    if (keyCode == MouseEvent.BUTTON1 || keyCode == MouseEvent.BUTTON2
        || keyCode == MouseEvent.BUTTON3)
      _robot.mousePress(keyCode);
    else
      _robot.keyPress(keyCode);
  }

  protected void release(ReleaseCommand command, EfferentCommandHandler handler)
  {
    int keyCode = getCode(command, handler);
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Releasing " + keyCode + " ("
          + KeyStroke.getKeyStroke(keyCode, 0, true) + ")");

    if (keyCode == MouseEvent.BUTTON1 || keyCode == MouseEvent.BUTTON2
        || keyCode == MouseEvent.BUTTON3)
      _robot.mouseRelease(keyCode);
    else
      _robot.keyRelease(keyCode);
  }

  protected void positionMouse(TranslateCommand command,
      EfferentCommandHandler handler, IEfferentObject mouse, double[] position)
  {
    if (_sizeAndLocationProcessor == null) getSizeAndLocationProcessor();

    /*
     * convert from retino to screen..
     */
    double[] location = _sizeAndLocationProcessor.toScreenLocation(position);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Placing mouse @ %.2f, %.2f", location[0],
          location[1]));

    _robot.mouseMove((int) location[0], (int) location[1]);
  }

  private void getSizeAndLocationProcessor()
  {
    /*
     * find the sensor and processor
     */
    for (ISensor sensor : CommonReality.getSensors())
      if (sensor instanceof DefaultSwingSensor)
      {
        _sizeAndLocationProcessor = ((DefaultSwingSensor) sensor)
            .getSizeAndLocationProcessor();
        break;
      }

    if (_sizeAndLocationProcessor == null)
      throw new IllegalStateException(
          "Cannot position mouse w/o SwingSensor installed");
  }
}
