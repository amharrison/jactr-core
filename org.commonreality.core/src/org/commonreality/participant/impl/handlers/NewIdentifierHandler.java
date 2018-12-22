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
package org.commonreality.participant.impl.handlers;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.message.request.object.NewIdentifierAcknowledgement;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.object.manager.IRequestableObjectManager;

/**
 * handles both IObjectData and IObjectCommand
 * 
 * @author developer
 */
public class NewIdentifierHandler implements
    IMessageHandler<NewIdentifierAcknowledgement>
{
  /**
   * logger definition
   */
  static private final Log     LOGGER = LogFactory
                                          .getLog(NewIdentifierHandler.class);

  private GeneralObjectHandler _objectHandler;

  public NewIdentifierHandler(GeneralObjectHandler objectHandler)
  {
    _objectHandler = objectHandler;
  }

  // public void handleMessage(IoSession session, INewIdentifierAcknowledgement
  // ack) throws Exception
  // {
  //
  // Collection<IIdentifier> newIdentifiers = ack.getIdentifiers();
  // if (LOGGER.isDebugEnabled())
  // LOGGER.debug("Adding new identifiers " + newIdentifiers);
  // /*
  // * we snag the id of the first one to figure out which IRequestObjectManager
  // * to send this to
  // */
  // IIdentifier first = newIdentifiers.iterator().next();
  // IRequestableObjectManager manager = (IRequestableObjectManager)
  // _objectHandler.getActualManager(first
  // .getType());
  // if (manager != null)
  // manager.addFreeIdentifiers(newIdentifiers);
  // else if (LOGGER.isWarnEnabled())
  // LOGGER.warn("No clue who to give these identifiers to");
  //
  // }

  @Override
  public void accept(ISessionInfo<?> t, NewIdentifierAcknowledgement ack)
  {
    Collection<IIdentifier> newIdentifiers = ack.getIdentifiers();
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Adding new identifiers " + newIdentifiers);
    /*
     * we snag the id of the first one to figure out which IRequestObjectManager
     * to send this to
     */
    IIdentifier first = newIdentifiers.iterator().next();
    IRequestableObjectManager manager = (IRequestableObjectManager) _objectHandler
        .getActualManager(first.getType());
    if (manager != null)
      manager.addFreeIdentifiers(newIdentifiers);
    else if (LOGGER.isWarnEnabled())
      LOGGER.warn("No clue who to give these identifiers to");

  }

}
