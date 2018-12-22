/*
 * Created on Feb 23, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
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
package org.commonreality.reality;

import java.util.concurrent.Future;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IAcknowledgement;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.credentials.ICredentials;
import org.commonreality.participant.IParticipant;
import org.commonreality.time.IClock;

/**
 * @author developer
 */
public interface IReality extends IParticipant
{

  /**
   * send a message to a specific participant. Note, this should handle
   * {@link IIdentifier#ALL} by sending the message to all participants
   * @param identifier
   * @param message
   * @return
   */
  public Future<IAcknowledgement> send(IIdentifier identifier, IMessage message);
  
  public Future<IAcknowledgement> send(Object session, IMessage message);
  

  public IClock getClock();

  public IIdentifier newIdentifier(IIdentifier owner, IIdentifier template);

  public void add(ICredentials credentials, boolean wantsClockControl);

  public void remove(ICredentials credentials);
}
