/*
 * Created on Feb 28, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.modalities.visual;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.DefaultPropertyHandler;
import org.commonreality.modalities.visual.geom.Dimension2D;
import org.commonreality.modalities.visual.geom.Point2D;
import org.commonreality.object.ISensoryObject;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.UnknownPropertyNameException;

public class DefaultVisualPropertyHandler extends DefaultPropertyHandler
    implements IVisualPropertyHandler
{
  /**
   * logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(DefaultVisualPropertyHandler.class);

  public boolean isVisible(ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    return getBoolean(VISIBLE, realObject);
  }

  public Point2D getRetinalLocation(ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    return asPoint(getDoubles(RETINAL_LOCATION, realObject));
  }

  public double getRetinalDistance(ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    return getDouble(RETINAL_DISTANCE, realObject);
  }

  public Dimension2D getRetinalSize(ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    return asDimension(getDoubles(RETINAL_SIZE, realObject));
  }

  public Color[] getColors(ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    return asColors(getDoubles(COLOR, realObject));
  }

  public String[] getTypes(ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    return getStrings(TYPE, realObject);
  }

  public String getToken(ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    return getString(TOKEN, realObject);
  }

  public String getText(ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    return getString(TEXT, realObject);
  }

  public Point2D asPoint(Object object) throws IllegalArgumentException
  {
    double[] array = null;
    try
    {
      array = (double[]) object;
    }
    catch (ClassCastException cce)
    {
      throw new IllegalArgumentException(
          "parameter must be a double array, got " + object.getClass());
    }
    if (array.length != 2)
      throw new IllegalArgumentException(
          "double array must be of length 2, got " + array.length);
    return new Point2D(array[0], array[1]);
  }

  public Dimension2D asDimension(Object object) throws IllegalArgumentException
  {
    double[] array = null;
    try
    {
      array = (double[]) object;
    }
    catch (ClassCastException cce)
    {
      throw new IllegalArgumentException(
          "parameter must be a double array, got " + object.getClass());
    }
    if (array.length != 2)
      throw new IllegalArgumentException(
          "double array must be of length 2, got " + array.length);
    return new Dimension2D(array[0], array[1]);
  }

  public Color[] asColors(Object object) throws IllegalArgumentException
  {
    double[] array = null;
    try
    {
      array = (double[]) object;
    }
    catch (ClassCastException cce)
    {
      throw new IllegalArgumentException(
          "parameter must be a double array, got " + object.getClass());
    }
    if (array.length % 4 != 0)
      throw new IllegalArgumentException(
          "double array must be a multiple of four (rgba) , got " +
              array.length);

    Color[] rtn = new Color[array.length / 4];
    for (int i = 0; i < rtn.length; i++)
    {
      int index = i * 4;
      rtn[i] = new Color((float) array[index++], (float) array[index++],
          (float) array[index++], (float) array[index]);
    }

    return rtn;
  }

  public double getSlope(ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    return getDouble(SLOPE, realObject);
  }

  /**
   * @see org.commonreality.modalities.IModalityPropertyHandler#hasModality(java.lang.String,
   *      org.commonreality.object.ISensoryObject)
   */
  public boolean hasModality(ISensoryObject sensoryObject)
  {
    return sensoryObject.hasProperty(IS_VISUAL);
  }

}
