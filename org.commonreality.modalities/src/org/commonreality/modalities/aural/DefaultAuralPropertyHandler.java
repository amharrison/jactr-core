/*
 * Created on Jun 25, 2007 Copyright (C) 2001-2007, Anthony Harrison
 * anh23@pitt.edu (jactr.org) This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version. This library is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.modalities.aural;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.DefaultPropertyHandler;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.ISensoryObject;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.UnknownPropertyNameException;

/**
 * @author developer
 */
public class DefaultAuralPropertyHandler extends DefaultPropertyHandler
    implements IAuralPropertyHandler
{
  /**
   * logger definition
   */
  static private final Log LOGGER = LogFactory
                                      .getLog(DefaultAuralPropertyHandler.class);

  /**
   * @see org.commonreality.modalities.aural.IAuralPropertyHandler#isAudible()
   */
  public boolean isAudible(ISimulationObject object)
      throws UnknownPropertyNameException
  {
    return getBoolean(IS_AUDIBLE, object);
  }

  /**
   * This tests if an {@link IAfferentObject} is a aural..
   * @see org.commonreality.modalities.IModalityPropertyHandler#hasModality(org.commonreality.object.ISensoryObject)
   */
  public boolean hasModality(ISensoryObject sensoryObject)
  {
    return hasProperty(AURAL_MODALITY, sensoryObject);
  }

  /**
   * @see org.commonreality.modalities.aural.IAuralPropertyHandler#getToken(org.commonreality.object.ISimulationObject)
   */
  public String getToken(ISimulationObject object)
      throws UnknownPropertyNameException
  {
    return getString(TOKEN, object);
  }

  /**
   * @see org.commonreality.modalities.aural.IAuralPropertyHandler#getType(org.commonreality.object.ISimulationObject)
   */
  public String[] getTypes(ISimulationObject object)
      throws UnknownPropertyNameException
  {
    return getStrings(TYPE, object);
  }

  public double getDuration(ISimulationObject object)
      throws UnknownPropertyNameException
  {
    return getDouble(DURATION, object);
  }

  public double getOnset(ISimulationObject object)
      throws UnknownPropertyNameException
  {
    return getDouble(ONSET, object);
  }

}
