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
package org.commonreality.sensors;

import org.commonreality.agents.IAgent;
import org.commonreality.net.message.credentials.ICredentials;
import org.commonreality.object.manager.IRequestableAfferentObjectManager;
import org.commonreality.object.manager.IRequestableEfferentObjectManager;
import org.commonreality.participant.IParticipant;
import org.commonreality.reality.IReality;

/**
 * The sensor provides afferent and efferent objects to all the {@link IAgent}
 * instances that it wants to communicate with. Only {@link ISensor} and
 * {@link IReality} are permitted to create objects. In order to create objects,
 * the sensor should use {@link IRequestableEfferentObjectManager} and
 * {@link IRequestableAfferentObjectManager} to request new object instances.
 * For more details, see {@link XMLSensor}
 * 
 * @author developer
 */
public interface ISensor extends IParticipant
{

  public void setCredentials(ICredentials credentials);

  public IRequestableEfferentObjectManager getEfferentObjectManager();

  public IRequestableAfferentObjectManager getAfferentObjectManager();
}
