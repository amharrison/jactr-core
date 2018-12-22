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

import org.commonreality.modalities.IModalityPropertyHandler;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.UnknownPropertyNameException;

/**
 * @author developer
 */
public interface IAuralPropertyHandler extends IModalityPropertyHandler
{

  static public final String AURAL_MODALITY      = "aural.isAural";

  static public final String IS_AUDIBLE = "aural.audible";

  static public final String TYPE       = "aural.type";

  static public final String TOKEN      = "aural.token";  // will contain
  
  static public final String ONSET = "aural.onset";
  
  static public final String DURATION = "aural.duration";

  // actual value

  public boolean isAudible(ISimulationObject object)
      throws UnknownPropertyNameException;

  public String[] getTypes(ISimulationObject object)
      throws UnknownPropertyNameException;

  public String getToken(ISimulationObject object)
      throws UnknownPropertyNameException;
  
  public double getOnset(ISimulationObject object) throws UnknownPropertyNameException;
  
  public double getDuration(ISimulationObject object) throws UnknownPropertyNameException;
}
