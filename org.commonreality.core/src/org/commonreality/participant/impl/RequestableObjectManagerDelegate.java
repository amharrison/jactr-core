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
package org.commonreality.participant.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.request.object.NewIdentifierRequest;
import org.commonreality.participant.IParticipant;

/**
 * @author developer
 */
public abstract class RequestableObjectManagerDelegate
{
  /**
   * logger definition
   */
  static private final Log               LOGGER       = LogFactory
                                                          .getLog(RequestableObjectManagerDelegate.class);

  private Map<Object, List<IIdentifier>> _cachedIdentifiers;

  private IParticipant                   _participant;

  private int                            _requestMore = 2;

  public RequestableObjectManagerDelegate(IParticipant participant)
  {
    this(participant, 1);
  }

  public RequestableObjectManagerDelegate(IParticipant participant,
      int requestMore)
  {
    _cachedIdentifiers = new HashMap<Object, List<IIdentifier>>();
    _participant = participant;
    _requestMore = requestMore;
  }

  /**
   * get a free identifier that is keyed on key. this may block if none are
   * available
   * 
   * @param key
   * @return
   */
  final public IIdentifier getFreeIdentifier(Object key)
  {
    /*
     * first get the keyed list
     */
    List<IIdentifier> keyedIdentifiers = getKeyedCollection(key);

    IIdentifier rtn = null;
    boolean mustRequest = false;
    synchronized (keyedIdentifiers)
    {
      // don't actually request from within the sync.
      mustRequest = keyedIdentifiers.size() == 0;
    }

    if (mustRequest) request(key);

    synchronized (keyedIdentifiers)
    {
      try
      {
        while (keyedIdentifiers.size() == 0)
          keyedIdentifiers.wait();
      }
      catch (InterruptedException ie)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Interrupted while waiting for free identifier");
      }

      if (keyedIdentifiers.size() != 0) rtn = keyedIdentifiers.remove(0);

      mustRequest = keyedIdentifiers.size() <= _requestMore;
    }
    // don't request from within the synch
    if (mustRequest) request(key);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Returning new requested id " + rtn);
    return rtn;
  }

  final public void request(Object key)
  {
    _participant.send(new NewIdentifierRequest(_participant.getIdentifier(),
        createTemplates(key)));
  }

  /**
   * get the list that houses the cached identifiers for key. if none exists, it
   * will be created
   * 
   * @param key
   * @return
   */
  final protected List<IIdentifier> getKeyedCollection(Object key)
  {
    List<IIdentifier> keyedIdentifiers = null;
    /*
     * get or create the list that holds the identifiers for key
     */
    synchronized (_cachedIdentifiers)
    {
      keyedIdentifiers = _cachedIdentifiers.get(key);
      if (keyedIdentifiers == null)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("No collection for key " + key);

        keyedIdentifiers = new ArrayList<IIdentifier>();
        _cachedIdentifiers.put(key, keyedIdentifiers);
      }
    }
    return keyedIdentifiers;
  }

  /**
   * When a new block of identifiers is made available from CR, they are added
   * to the object manager
   * 
   * @param freeIdentifiers
   */
  final public void addFreeIdentifiers(Collection<IIdentifier> freeIdentifiers)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Adding free identifiers " + freeIdentifiers);
    for (IIdentifier freeId : freeIdentifiers)
    {
      Object key = getKey(freeId);
      if (key == null) continue;

      List<IIdentifier> keyedIdentifiers = getKeyedCollection(key);
      /*
       * add it.. and signal
       */
      synchronized (keyedIdentifiers)
      {
        keyedIdentifiers.add(freeId);
        keyedIdentifiers.notifyAll();
      }
    }
  }

  /**
   * return the key that this free identifier should be stored under. For
   * instance, if this iidentifier is for an afferent object, the key would
   * typically be the agent identifier portion.<br>
   * <br>
   * null should be returned if the identifier is not to be stored because it is
   * of the wrong type or some such other reason.
   * 
   * @param freeIdentifier
   * @return
   */
  abstract protected Object getKey(IIdentifier freeIdentifier);

  /**
   * create a bunch of template identifiers to be requested from CR
   * 
   * @param key
   * @return
   */
  abstract protected Collection< ? extends IIdentifier> createTemplates(
      Object key);
}
