/*
 * Created on May 11, 2007 Copyright (C) 2001-2007, Anthony Harrison
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
package org.commonreality.object.delta;

import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.object.ISimulationObject;

/**
 * @author developer
 */
public class FullObjectDelta extends ObjectDelta
{
  /**
   * 
   */
  private static final long serialVersionUID = 6971326266106420527L;
  /**
   * logger definition
   */
  static private final Log LOGGER = LogFactory.getLog(FullObjectDelta.class);

  /**
   * @param identifier
   * @param newValues
   * @param oldValues
   */
  public FullObjectDelta(ISimulationObject object)
  {
    super(object.getIdentifier(), Collections.EMPTY_MAP, Collections.EMPTY_MAP);
    for (String key : object.getProperties())
      _newValues.put(key, object.getProperty(key));
  }

}
