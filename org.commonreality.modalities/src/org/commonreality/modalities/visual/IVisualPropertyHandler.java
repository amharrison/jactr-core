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




import org.commonreality.modalities.IModalityPropertyHandler;
import org.commonreality.modalities.visual.geom.Dimension2D;
import org.commonreality.modalities.visual.geom.Point2D;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.UnknownPropertyNameException;

public interface IVisualPropertyHandler extends IModalityPropertyHandler
{
  static final public String IS_VISUAL        = "visual.isVisual";

  static final public String VISIBLE          = "visual.visible";

  static final public String RETINAL_LOCATION = "visual.retinal.location";

  static final public String RETINAL_DISTANCE = "visual.retinal.distance";

  static final public String RETINAL_SIZE     = "visual.retinal.size";

  static final public String COLOR            = "visual.color";

  static final public String TYPE             = "visual.type";

  static final public String TOKEN            = "visual.token";

  static final public String TEXT             = "visual.text";

  static final public String SLOPE            = "visual.slope";

  public boolean isVisible(ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public Point2D getRetinalLocation(ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public double getRetinalDistance(ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public Dimension2D getRetinalSize(ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public Color[] getColors(ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public String[] getTypes(ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public String getToken(ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public String getText(ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public double getSlope(ISimulationObject realObject)
      throws UnknownPropertyNameException;

  /**
   * transforms an array of doubles into a point2d
   * 
   * @param object
   * @return
   * @throws IllegalArgumentException
   */
  public Point2D asPoint(Object object) throws IllegalArgumentException;

  public Dimension2D asDimension(Object object) throws IllegalArgumentException;

  public Color[] asColors(Object object) throws IllegalArgumentException;
}
