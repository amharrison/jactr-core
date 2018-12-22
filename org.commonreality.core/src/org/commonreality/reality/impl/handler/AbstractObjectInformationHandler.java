/*
 * Created on May 12, 2007 Copyright (C) 2001-2007, Anthony Harrison
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
package org.commonreality.reality.impl.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.participant.impl.handlers.GeneralObjectHandler;
import org.commonreality.reality.IReality;
import org.commonreality.reality.impl.StateAndConnectionManager;

/**
 * @author developer
 */
public abstract class AbstractObjectInformationHandler
{
  /**
   * logger definition
   */
  static private final Log LOGGER = LogFactory.getLog(AbstractObjectInformationHandler.class);
  
  private StateAndConnectionManager _manager;
  private GeneralObjectHandler _objectHandler;
  private IReality _reality;
  
  /**
   * @param participant
   */
  public AbstractObjectInformationHandler(IReality reality, StateAndConnectionManager manager, GeneralObjectHandler objectHandler)
  {
    _reality = reality;
    _manager = manager;
    _objectHandler = objectHandler;
  }

  protected IReality getParticipant()
  {
    return _reality;
  }

  protected GeneralObjectHandler getObjectHandler()
  {
    return _objectHandler;
  }
  
  protected StateAndConnectionManager getManager()
  {
    return _manager;
  }
}
