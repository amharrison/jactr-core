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
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.identifier.BasicSensoryIdentifier;
import org.commonreality.object.identifier.ISensoryIdentifier;
import org.commonreality.object.manager.IRequestableAfferentObjectManager;
import org.commonreality.object.manager.impl.AfferentObject;
import org.commonreality.object.manager.impl.AfferentObjectManager;
import org.commonreality.participant.IParticipant;

/**
 * @author developer
 */
public class RequestableAfferentObjectManager extends AfferentObjectManager
    implements IRequestableAfferentObjectManager
{
  /**
   * logger definition
   */
  static private final Log                 LOGGER = LogFactory
                                                      .getLog(RequestableAfferentObjectManager.class);

  private IParticipant                     _participant;

  private RequestableObjectManagerDelegate _delegate;

  /**
   * 
   */
  public RequestableAfferentObjectManager(IParticipant participant)
  {
    _participant = participant;
    _delegate = new RequestableObjectManagerDelegate(participant, 5) {

      @Override
      protected Collection<? extends IIdentifier> createTemplates(Object key)
      {
        return Collections.nCopies(10, new BasicSensoryIdentifier("",
            IIdentifier.Type.AFFERENT, _participant.getIdentifier(),
            _participant.getIdentifier(), (IIdentifier) key));
      }

      @Override
      protected Object getKey(IIdentifier freeIdentifier)
      {
        if (freeIdentifier.getType() != IIdentifier.Type.AFFERENT) return null;
        return ((ISensoryIdentifier) freeIdentifier).getAgent();
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
  public IAfferentObject request(IIdentifier agentIdentifier)
  {
    IIdentifier afferentId = requestIdentifier(agentIdentifier);
    if (afferentId == null) return null;
    return new AfferentObject(afferentId);
  }

  public IIdentifier requestIdentifier(IIdentifier agentIdentifier)
  {
    IIdentifier afferentId = _delegate.getFreeIdentifier(agentIdentifier);
    if (afferentId == null) return null;
    return afferentId;
  }

  public void prefetch(IIdentifier agentIdentifier)
  {
    _delegate.request(agentIdentifier);
  }
}
