package org.commonreality.sensors.keyboard.map;

import java.io.Serializable;

/*
 * default logging
 */

/**
 * provides an abstract device mapping between locations and button codes.
 * The codes are going to be device specific
 */
public interface IDeviceMap extends Serializable
{
  
  static public final String DEVICE_MAP_PROPERTY = "deviceMap";

  /**
   * return the key code corresponding to this location
   * @param location
   * @return
   * @throws IllegalArgumentException
   */
  public int getKey(double[] location);
  
  /**
   * return the button code corresponding to this location
   * @param location
   * @return
   * @throws IllegalArgumentException
   */
  public int getMouseButton(double[] location);
  
  public double[] getKeyLocation(int keyCode);
  
  public double[] getMouseButtonLocation(int buttonCode);
  
  /**
   * try to convert the string into a keystroke.
   * @param string
   * @return keycode, or -1 if couldn't be determined
   */
  public int getKeyCode(String string);
}