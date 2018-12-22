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
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.efferent.IEfferentCommandTemplate;
import org.commonreality.efferent.event.IEfferentCommandListener;
import org.commonreality.efferent.impl.EfferentCommandManager;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.identifier.BasicSensoryIdentifier;
import org.commonreality.object.identifier.ISensoryIdentifier;
import org.commonreality.object.manager.IRequestableObjectManager;
import org.commonreality.object.manager.impl.AfferentObject;
import org.commonreality.object.manager.impl.EfferentObject;
import org.commonreality.participant.IParticipant;

/**
 * @author developer
 */
public class RequestableEfferentCommandManager extends EfferentCommandManager
    implements
    IRequestableObjectManager<IEfferentCommand, IEfferentCommandListener>
{
  /**
   * logger definition
   */
  static private final Log                 LOGGER = LogFactory
                                                      .getLog(RequestableEfferentCommandManager.class);

  private IParticipant                     _participant;

  private RequestableObjectManagerDelegate _delegate;

  /**
   * 
   */
  public RequestableEfferentCommandManager(IParticipant participant)
  {
    _participant = participant;
    _delegate = new RequestableObjectManagerDelegate(participant, 5) {

      @Override
      protected Collection<? extends IIdentifier> createTemplates(Object key)
      {
        return Collections.nCopies(10, new BasicSensoryIdentifier("",
            IIdentifier.Type.EFFERENT_COMMAND, _participant.getIdentifier(),
            (IIdentifier) key, _participant.getIdentifier()));
      }

      @Override
      protected Object getKey(IIdentifier freeIdentifier)
      {
        if (freeIdentifier.getType() != IIdentifier.Type.EFFERENT_COMMAND)
          return null;
        return ((ISensoryIdentifier) freeIdentifier).getSensor();
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
   * Not implemented, will throw a {@link RuntimeException} if called. This is
   * becaused {@link IEfferentCommand}'s are customized based on the need and
   * we cannot use a default implementation (as in {@link AfferentObject} and
   * {@link EfferentObject}). Agents should rather call
   * {@link #requestIdentifier(IIdentifier)} to get the {@link IIdentifier} and
   * then instantiate the appropriate during the call to
   * {@link IEfferentCommandTemplate#instantiate(org.commonreality.agents.IAgent, org.commonreality.object.IEfferentObject)}
   * 
   * @see org.commonreality.object.manager.IRequestableObjectManager#request(org.commonreality.identifier.IIdentifier)
   */
  @Deprecated
  public IEfferentCommand request(IIdentifier sensorIdentifier)
  {
    throw new RuntimeException(
        getClass()
            + ".request(IIdentifier) is not implemented as it requires custom instantiation");
  }

  public IIdentifier requestIdentifier(IIdentifier sensorIdentifier)
  {
    return _delegate.getFreeIdentifier(sensorIdentifier);
  }

  public void prefetch(IIdentifier sensorIdentifier)
  {
    _delegate.request(sensorIdentifier);
  }
}
