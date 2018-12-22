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
package org.commonreality.participant.impl.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.message.request.connect.ConnectionAcknowledgment;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.participant.impl.AbstractParticipant;

/**
 * @author developer
 */
public class ConnectionHandler implements
    IMessageHandler<ConnectionAcknowledgment>
{
  /**
   * logger definition
   */
  static private final Log    LOGGER = LogFactory
                                         .getLog(ConnectionHandler.class);

  private AbstractParticipant _participant;

  public ConnectionHandler(AbstractParticipant participant)
  {
    _participant = participant;
  }

  @Override
  public void accept(ISessionInfo<?> t, ConnectionAcknowledgment ack)
  {
    IIdentifier id = ack.getAssignedIdentifier();

    _participant.setCommonRealityIdentifier(ack.getSource());

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Got connection acknowledgement, our id : " + id);

    if (_participant.getIdentifier() != null)
      if (LOGGER.isWarnEnabled())
        LOGGER
            .warn("We already have a valid identifier, but we got an acknowledgment anyway?");

    /*
     * if id is null, we're about to crap out..
     */
    if (id == null)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn("Connection denied " + ack.getResponseMessage());

      throw new SecurityException("Connection denied : "
          + ack.getResponseMessage());
    }

    _participant.setIdentifier(id);

  }

  // public void handleMessage(IoSession session, IConnectionAcknowledgement
  // ack)
  // throws Exception
  // {
  // IIdentifier id = ack.getAssignedIdentifier();
  //
  // _participant.setCommonRealityIdentifier(ack.getSource());
  //
  // if (LOGGER.isDebugEnabled())
  // LOGGER.debug("Got connection acknowledgement, our id : " + id);
  //
  // if (_participant.getIdentifier() != null)
  // if (LOGGER.isWarnEnabled())
  // LOGGER
  // .warn("We already have a valid identifier, but we got an acknowledgment anyway?");
  //
  // /*
  // * if id is null, we're about to crap out..
  // */
  // if (id == null)
  // {
  // if (LOGGER.isWarnEnabled())
  // LOGGER.warn("Connection denied " + ack.getResponseMessage());
  //
  // throw new SecurityException("Connection denied : "
  // + ack.getResponseMessage());
  // }
  //
  // _participant.setIdentifier(id);
  // }

}
