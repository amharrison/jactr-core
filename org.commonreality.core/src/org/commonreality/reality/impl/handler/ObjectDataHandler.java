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

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.message.command.object.ObjectData;
import org.commonreality.net.message.impl.BaseAcknowledgementMessage;
import org.commonreality.net.message.request.object.ObjectDataRequest;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.object.delta.IObjectDelta;
import org.commonreality.participant.impl.handlers.GeneralObjectHandler;
import org.commonreality.reality.IReality;
import org.commonreality.reality.impl.StateAndConnectionManager;

/**
 * @author developer
 */
public class ObjectDataHandler extends AbstractObjectInformationHandler
    implements IMessageHandler<ObjectDataRequest>
{
  /**
   * logger definition
   */
  static private final Log LOGGER = LogFactory.getLog(ObjectDataHandler.class);

  /**
   * @param participant
   */
  public ObjectDataHandler(IReality reality, StateAndConnectionManager manager,
      GeneralObjectHandler objectHandler)
  {
    super(reality, manager, objectHandler);
  }

  // public void handleMessage(IoSession session, IObjectDataRequest arg1)
  // throws Exception
  // {
  // }

  @Override
  public void accept(ISessionInfo<?> session, ObjectDataRequest arg1)
  {
    IIdentifier from = arg1.getSource();
    IIdentifier to = arg1.getDestination();
    Collection<IObjectDelta> data = arg1.getData();
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Got data from " + from + " to " + to + " : " + data);

    /*
     * we hold on to the data until we get the official go ahead
     */
    getObjectHandler().storeObjectData(data, arg1);

    /*
     * but we do send it on to the destination and send out a confirmation
     */
    IReality reality = getParticipant();

    /*
     * always acknowledge the receipt of data, but if the destination is
     * everyone, should we send this? when data is sent to all (such as a
     * RealObject), the sender will get two copies - this is not correct
     */
    try
    {
      session.write(new BaseAcknowledgementMessage(reality
          .getIdentifier(),
          arg1.getMessageId()));
      session.flush();
    }
    catch (Exception e)
    {
      LOGGER.error("Failed to send ack", e);
    }

    reality.send(to, new ObjectData(reality.getIdentifier(), data));

  }

}
