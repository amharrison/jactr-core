/*
 * Created on Feb 28, 2006
 * Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu (jactr.org) This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.modalities.visual.geom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * this is pathetic - java.awt.geom.Dimension2D only naturally supports 
 * integer precision with java.awt.Dimension, there is no double impl
 * @author developer
 *
 */
public class Dimension2D  implements java.io.Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = -4086131189328450894L;

  /**
   logger definition
   */
  static public final Log LOGGER = LogFactory.getLog(Dimension2D.class);

  private double _width;
  private double _height;
  
  
  public Dimension2D(double width, double height)
  {
    setSize(width, height);
  }
  
  
  public double getWidth()
  {
    return _width;
  }

  
  public double getHeight()
  {
    return _height;
  }

  
  public void setSize(double arg0, double arg1)
  {
    _width = arg0;
    _height = arg1;
  }

  
  public String toString()
  {
    StringBuilder sb = new StringBuilder("(Dim:");
    sb.append(getWidth()).append("x").append(getHeight()).append(")");
    return sb.toString();
  }
  /** 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode()
  {
    final int PRIME = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_width);
    result = PRIME * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_height);
    result = PRIME * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /** 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final Dimension2D other = (Dimension2D) obj;
    if (Double.doubleToLongBits(_width) != Double.doubleToLongBits(other._width)) return false;
    if (Double.doubleToLongBits(_height) != Double.doubleToLongBits(other._height)) return false;
    return true;
  }

}


