package org.commonreality.sensors.keyboard.map;

/*
 * default logging
 */
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.visual.geom.Point2D;

/**
 * abstract device impl that caps the doubles to ints in 2 dimensions
 * @author harrison
 *
 */
public abstract class AbstractDeviceMap implements IDeviceMap
{
  /**
   * 
   */
  private static final long serialVersionUID = -3756743800754314321L;

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractDeviceMap.class);
  
  private SortedMap<Integer, Point2D> _keyToLocation;
  private Map<Point2D, Integer> _locationToKey;
  private SortedMap<Integer, Point2D> _buttonToLocation;
  private Map<Point2D, Integer> _locationToButton;
  
  public AbstractDeviceMap()
  {
    _keyToLocation = new TreeMap<Integer, Point2D>();
    _buttonToLocation = new TreeMap<Integer, Point2D>();
    _locationToKey = new HashMap<Point2D, Integer>();
    _locationToButton = new HashMap<Point2D, Integer>();
  }
  
  protected void addButton(int buttonCode, double x, double y)
  {
    addButton(buttonCode, new double[]{x,y});
  }
  
  synchronized protected void addButton(int buttonCode, double[] location)
  {
    Point2D loc = asPoint(location);
    _buttonToLocation.put(buttonCode, loc);
    _locationToButton.put(loc, buttonCode);
  }
  
  protected void addKey(int keyCode, double x, double y)
  {
    addKey(keyCode, new double[]{x,y});
  }
  
  synchronized protected void addKey(int keyCode, double[] location)
  {
    Point2D loc = asPoint(location);
    _keyToLocation.put(keyCode, loc);
    _locationToKey.put(loc, keyCode);
  }
  
  private Point2D asPoint(double[] location)
  {
    Point2D loc = new Point2D(Math.floor(location[0]),Math.floor(location[1]));
    return loc;
  }
  
  private double[] asDoubles(Point2D location)
  {
    return new double[]{location.getX(), location.getY()};
  }

  synchronized public int getKey(double[] location)
  {
    Point2D loc = asPoint(location);
    Integer value = _locationToKey.get(loc);
    if(value==null)
      throw new IllegalArgumentException("No key associated with "+loc);
    return value;
  }

  synchronized public double[] getKeyLocation(int keyCode)
  {
    if(_keyToLocation.containsKey(keyCode))
      return asDoubles(_keyToLocation.get(keyCode));
    throw new IllegalArgumentException(keyCode+" not defined");
  }

  synchronized public int getMouseButton(double[] location)
  {
    Point2D loc = asPoint(location);
    Integer value = _locationToButton.get(loc);
    if(value==null)
      throw new IllegalArgumentException("No button associated with "+loc);
    return value;
  }

  synchronized public double[] getMouseButtonLocation(int buttonCode)
  {
    if(_buttonToLocation.containsKey(buttonCode))
      return asDoubles(_buttonToLocation.get(buttonCode));
    throw new IllegalArgumentException(buttonCode+" not defined");
  }

}
