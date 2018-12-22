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
package org.commonreality.participant.impl;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.identifier.impl.BasicIdentifier;
import org.commonreality.object.IRealObject;
import org.commonreality.object.manager.IRequestableObjectManager;
import org.commonreality.object.manager.IRequestableRealObjectManager;
import org.commonreality.object.manager.event.IRealObjectListener;
import org.commonreality.object.manager.impl.RealObject;
import org.commonreality.object.manager.impl.RealObjectManager;
import org.commonreality.participant.IParticipant;

/**
 * @author developer
 */
public class RequestableRealObjectManager extends RealObjectManager implements
    IRequestableRealObjectManager
{
  /**
   * logger definition
   */
  static private final Log                 LOGGER = LogFactory
                                                      .getLog(RequestableRealObjectManager.class);

  private IParticipant                     _participant;

  private RequestableObjectManagerDelegate _delegate;

  /**
   * 
   */
  public RequestableRealObjectManager(IParticipant participant)
  {
    _participant = participant;
    _delegate = new RequestableObjectManagerDelegate(participant, 5) {

      @Override
      protected Collection<? extends IIdentifier> createTemplates(Object key)
      {
        return Collections.nCopies(10, new BasicIdentifier("",
            IIdentifier.Type.OBJECT, _participant.getIdentifier()));
      }

      @Override
      protected Object getKey(IIdentifier freeIdentifier)
      {
        if (freeIdentifier.getType() != IIdentifier.Type.OBJECT) return null;
        return _participant;
      }

    };
  }

  /**
   * @see org.commonreality.object.manager.IRequestableObjectManager#addFreeIdentifiers(java.util.Collection)
   */
  public void addFreeIdentifiers(Collection<IIdentifier> freeIdentifiers)
  {
    _delegate.addFreeIdentifiers(freeIdentifiers);
  }

  /**
   * @see org.commonreality.object.manager.IRequestableObjectManager#request(org.commonreality.identifier.IIdentifier)
   */
  public IRealObject request(IIdentifier participantIdentifier)
  {
    IIdentifier id = requestIdentifier(participantIdentifier);
    if (id == null) return null;
    return new RealObject(id);
  }

  public IIdentifier requestIdentifier(IIdentifier participantIdentifier)
  {
    return _delegate.getFreeIdentifier(_participant);
  }

  public void prefetch(IIdentifier participantIdentifier)
  {
    _delegate.request(_participant);
  }
}
