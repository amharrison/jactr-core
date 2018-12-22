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
package org.commonreality.object.identifier;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.identifier.impl.BasicIdentifier;

/**
 * @author developer
 */
public class BasicSensoryIdentifier extends BasicIdentifier implements
    ISensoryIdentifier, Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 2028095333851077044L;

  /**
   * logger definition
   */
  static private final Log LOGGER = LogFactory
                                      .getLog(BasicSensoryIdentifier.class);

  private IIdentifier      _agent;
  private IIdentifier _sensor;

  /**
   * @param name
   * @param type
   * @param owner
   */
  public BasicSensoryIdentifier(String name, Type type, IIdentifier owner, IIdentifier sensor,
      IIdentifier agent)
  {
    super(name, type, owner);
    _agent = agent;
    _sensor = sensor;
  }

  /**
   * @see org.commonreality.object.identifier.ISensoryIdentifier#getAgent()
   */
  public IIdentifier getAgent()
  {
    return _agent;
  }

  /**
   * @see org.commonreality.object.identifier.ISensoryIdentifier#getSensor()
   */
  public IIdentifier getSensor()
  {
    return _sensor;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_agent == null) ? 0 : _agent.hashCode());
    result = prime * result + ((_sensor == null) ? 0 : _sensor.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    final BasicSensoryIdentifier other = (BasicSensoryIdentifier) obj;
    if (_agent == null)
    {
      if (other._agent != null) return false;
    }
    else if (!_agent.equals(other._agent)) return false;
    if (_sensor == null)
    {
      if (other._sensor != null) return false;
    }
    else if (!_sensor.equals(other._sensor)) return false;
    return true;
  }

  
}
