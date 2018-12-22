/*
 * Created on May 14, 2007 Copyright (C) 2001-2007, Anthony Harrison
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
package org.commonreality.reality;

import java.util.ArrayList;
import java.util.Collection;

import org.commonreality.agents.IAgent;
import org.commonreality.sensors.ISensor;

/**
 * @author developer
 */
public final class CommonReality
{

  static private IReality            _reality;

  static private Collection<ISensor> _connectedSensors = new ArrayList<ISensor>();

  static private Collection<IAgent>  _connectedAgents  = new ArrayList<IAgent>();

  static public void setReality(IReality reality)
  {
    _reality = reality;
  }

  static public IReality getReality()
  {
    return _reality;
  }

  static public void addSensor(ISensor sensor)
  {
    synchronized (_connectedSensors)
    {
      _connectedSensors.add(sensor);
    }
  }

  static public void removeSensor(ISensor sensor)
  {
    synchronized (_connectedSensors)
    {
      _connectedSensors.remove(sensor);
    }
  }

  static public void addAgent(IAgent agent)
  {
    synchronized (_connectedAgents)
    {
      _connectedAgents.add(agent);
    }
  }

  static public void removeAgent(IAgent agent)
  {
    synchronized (_connectedAgents)
    {
      _connectedAgents.remove(agent);
    }
  }

  static public Collection<IAgent> getAgents()
  {
    synchronized (_connectedAgents)
    {
      return new ArrayList<IAgent>(_connectedAgents);
    }
  }

  static public Collection<ISensor> getSensors()
  {
    synchronized (_connectedSensors)
    {
      return new ArrayList<ISensor>(_connectedSensors);
    }
  }
}
