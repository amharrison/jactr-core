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
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.message.command.object.IObjectCommand;
import org.commonreality.net.message.command.object.ObjectCommand;
import org.commonreality.net.message.impl.BaseAcknowledgementMessage;
import org.commonreality.net.message.request.object.ObjectCommandRequest;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.ISensorObject;
import org.commonreality.object.delta.IObjectDelta;
import org.commonreality.participant.impl.handlers.GeneralObjectHandler;
import org.commonreality.reality.IReality;
import org.commonreality.reality.impl.StateAndConnectionManager;

/**
 * @author developer
 */
public class ObjectCommandHandler extends AbstractObjectInformationHandler
    implements IMessageHandler<ObjectCommandRequest>
{
  /**
   * logger definition
   */
  static private final Log LOGGER = LogFactory
                                      .getLog(ObjectCommandHandler.class);

  /**
   * @param participant
   */
  public ObjectCommandHandler(IReality reality,
      StateAndConnectionManager manager, GeneralObjectHandler objectHandler)
  {
    super(reality, manager, objectHandler);
  }

  public Collection<IObjectDelta> getPendingData(IIdentifier pendingId)
  {
    return getObjectHandler().getPendingData(pendingId);
  }

  @Override
  public void accept(ISessionInfo<?> session, ObjectCommandRequest arg1)
  {
    IIdentifier from = arg1.getSource();
    IIdentifier to = arg1.getDestination();
    Collection<IIdentifier> identifiers = arg1.getIdentifiers();
    IObjectCommand.Type type = arg1.getType();

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Got " + type + " from " + from + " to " + to + " : "
          + identifiers);

    /*
     * ideally there should be some security checking going on..
     */
    Collection<IMutableObject> added = Collections.emptyList();
    switch (type)
    {
      case ADDED:
        added = getObjectHandler().addObjects(identifiers, arg1);
        break;
      case REMOVED:
        getObjectHandler().removeObjects(identifiers, arg1);
        break;
      case UPDATED:
        getObjectHandler().updateObjects(identifiers, arg1);
        break;
    }

    IReality reality = getParticipant();

    /*
     * always send ack
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

    IObjectCommand command = new ObjectCommand(reality.getIdentifier(), type,
        identifiers);

    /*
     * if destination is not all, make sure we echo the command back to the
     * sender
     */
    if (!IIdentifier.ALL.equals(to)) reality.send(from, command);

    reality.send(to, command);

    /**
     * now we want to check to see if the new additions were participants. If
     * so, we need to signal the state and connection manager so that it can
     * finish up the pariticpant registration we have to do this before sending
     * out the acknowledgments.</br> We are assuming that when the participant
     * is sending info about itself it does it in a single update.
     */
    if (added.size() == 1)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Added : " + added);
      for (IMutableObject object : added)
        if (object instanceof ISensorObject || object instanceof IAgentObject)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(object.getIdentifier()
                + " is sensor or agent, completing connection");
          getManager().acceptParticipant(session, object, this);
        }
    }

  }

}
