/*
 * Created on Jul 17, 2006
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
package org.commonreality.modalities.spatial;

import org.commonreality.modalities.IModalityPropertyHandler;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.UnknownPropertyNameException;


public interface ISpatialPropertyHandler extends IModalityPropertyHandler
{

  static final public String IS_SPATIAL = "spatial.isSpatial";
  static final public String LOCATION = "spatial.location"; 
  static final public String ORTHOGONAL_BOUNDING_BOX = "spatial.bounding.box";//2 triple coordinates. lower close left, upper distant right
  static final public String ORIENTATION = "spatial.orientation"; //HPR of the object
  static final public String ORIENTATION_PROJECTION = "spatial.orientation.projection"; //2d projection of orientation
  
  /**
   * return the angular coordinates of the lower,near,left and upper,near,right orthogonal bounding box
   * @param realObject
   * @return [0]lower, near, left [1]upper, near, right 
   * @throws UnknownPropertyNameException
   */
  public double[][] getOrthogonalBoundingBox(ISimulationObject realObject) throws UnknownPropertyNameException;
  
  /**
   * return the angular coordinates of the center of the object
   * coordinates are bearing,pitch,distance relative to the viewer
   * @param realObject
   * @return
   * @throws UnknownPropertyNameException
   */
  public double[] getSpatialLocation(ISimulationObject realObject) throws UnknownPropertyNameException;
  
  /**
   * return the heading, pitch, roll of the object being perceived. relative to the viewer (i.e. 0 heading is facing
   * the same direction as the viewer)
   * @param realObject
   * @return
   * @throws UnknownPropertyNameException
   */
  public double[] getSpatialOrientation(ISimulationObject realObject) throws UnknownPropertyNameException;
  
  /**
   * returns the 2d projection of the heading, pitch, roll orthogonal vectors. this information can be
   * computed using the orientation but that depends upon having more information than most clients
   * have access to, so this info can be provided by the sensor. <br>
   * <br>
   * These are the angles made by the projection of the orthogonal axes to 2d space. Specifically: heading-pitch, heading-roll,
   * and pitch-roll.<br>
   * <br>
   * What's the point? heading-pitch of a person's head will tell us the direction they are looking mapped onto the retina,
   * which provides a depth-free (and therefor noisey) estimate of where they are looking<br>
   * <br>
   * Providing this information is entirely optional. There may be no array at all, or the array may contain
   * {@link Double#NaN} for any value it does not provide
   * @param realObject
   * @return
   * @throws UnknownPropertyNameException
   */
  public double[] getSpatialOrientationProjection(ISimulationObject realObject) throws UnknownPropertyNameException;
}


