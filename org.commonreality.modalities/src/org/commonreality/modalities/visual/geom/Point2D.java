package org.commonreality.modalities.visual.geom;

/*
 * default logging
 */
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Point2D implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = -5527834319915415507L;

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory.getLog(Point2D.class);

  private double _x;
  private double _y;
  
  public Point2D(double x, double y)
  {
    setLocation(x,y);
  }
  
  public double getX(){return _x;}
  
  public double getY(){return _y;}
  
  public void setLocation(double x, double y)
  {
    _x = x;
    _y = y;    
  }
  
  public String toString()
  {
    return "Loc:"+_x+", "+_y;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_x);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_y);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final Point2D other = (Point2D) obj;
    if (Double.doubleToLongBits(_x) != Double.doubleToLongBits(other._x))
      return false;
    if (Double.doubleToLongBits(_y) != Double.doubleToLongBits(other._y))
      return false;
    return true;
  }
  
  
}
