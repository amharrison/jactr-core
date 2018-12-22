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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.message.request.object.NewIdentifierAcknowledgement;
import org.commonreality.net.message.request.object.NewIdentifierRequest;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.participant.impl.handlers.GeneralObjectHandler;
import org.commonreality.reality.IReality;
import org.commonreality.reality.impl.StateAndConnectionManager;

/**
 * @author developer
 */
public class NewIdentifierHandler extends AbstractObjectInformationHandler
    implements IMessageHandler<NewIdentifierRequest>
{

  static private final Log LOGGER = LogFactory
                                      .getLog(NewIdentifierHandler.class);
  /**
   * @param participant
   */
  public NewIdentifierHandler(IReality reality,
      StateAndConnectionManager manager, GeneralObjectHandler objectHandler)
  {
    super(reality, manager, objectHandler);
  }

  // public void handleMessage(IoSession session, INewIdentifierRequest arg1)
  // throws Exception
  // {
  // IIdentifier source = arg1.getSource();
  // IReality reality = getParticipant();
  // Collection<IIdentifier> templates = arg1.getIdentifiers();
  // Collection<IIdentifier> ids = new ArrayList<IIdentifier>(templates.size());
  // for (IIdentifier template : templates)
  // ids.add(reality.newIdentifier(source, template));
  //
  // session.write(new NewIdentifierAcknowledgement(reality
  // .getIdentifier(), arg1.getMessageId(), ids));
  //
  // }

  @Override
  public void accept(ISessionInfo<?> t, NewIdentifierRequest arg1)
  {
    IIdentifier source = arg1.getSource();
    IReality reality = getParticipant();
    Collection<IIdentifier> templates = arg1.getIdentifiers();
    Collection<IIdentifier> ids = new ArrayList<IIdentifier>(templates.size());
    for (IIdentifier template : templates)
      ids.add(reality.newIdentifier(source, template));

    try
    {
      t.write(new NewIdentifierAcknowledgement(reality.getIdentifier(),
          arg1
          .getMessageId(), ids));
      t.flush();
    }
    catch (Exception e)
    {
      LOGGER.error("Failed to write ", e);
    }

  }

}
