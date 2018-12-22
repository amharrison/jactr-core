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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.visual.DefaultVisualPropertyHandler;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.ISensoryObject;
import org.commonreality.object.UnknownPropertyNameException;
public class DefaultSpatialPropertyHandler extends DefaultVisualPropertyHandler implements ISpatialPropertyHandler
{
  /**
   logger definition
   */
  static public final Log LOGGER = LogFactory
                                     .getLog(DefaultSpatialPropertyHandler.class);

  
  public boolean hasModality(ISensoryObject object)
  {
    return object.hasProperty(IS_SPATIAL);
  }
  
  public double[][] getOrthogonalBoundingBox(ISimulationObject realObject) throws UnknownPropertyNameException
  {
    check(ORTHOGONAL_BOUNDING_BOX, realObject);
    double[] val = (double[]) realObject.getProperty(ORTHOGONAL_BOUNDING_BOX);
    
    if(val.length!=6)
      throw new IllegalArgumentException(ORTHOGONAL_BOUNDING_BOX+" was expecting 2x3[6] doubles");
    
    double[][] rtn = new double[2][3];
    
    System.arraycopy(val, 0, rtn[0], 0, 3);
    System.arraycopy(val, 3, rtn[1], 0, 3);
    return rtn;
  }

  public double[] getSpatialLocation(ISimulationObject realObject) throws UnknownPropertyNameException
  {
    double[] rtn = getDoubles(LOCATION, realObject);
    return rtn;
  }

  public double[] getSpatialOrientation(ISimulationObject realObject) throws UnknownPropertyNameException
  {
    return getDoubles(ORIENTATION, realObject);
  }
  
  public double[] getSpatialOrientationProjection(ISimulationObject realObject) throws UnknownPropertyNameException
  {
    return getDoubles(ORIENTATION_PROJECTION, realObject);
  }
  

}


