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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IMessage;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.delta.IObjectDelta;
import org.commonreality.object.delta.ObjectDelta;
import org.commonreality.object.manager.IMutableObjectManager;
import org.commonreality.object.manager.impl.AfferentObject;
import org.commonreality.object.manager.impl.AgentObject;
import org.commonreality.object.manager.impl.EfferentObject;
import org.commonreality.object.manager.impl.RealObject;
import org.commonreality.object.manager.impl.SensorObject;
import org.commonreality.participant.IParticipant;

/**
 * handles both IObjectData and IObjectCommand
 * 
 * @author developer
 */
public class GeneralObjectHandler
{
  /**
   * logger definition
   */
  static private final Log                     LOGGER = LogFactory
                                                          .getLog(GeneralObjectHandler.class);

  private IParticipant                         _participant;

  private Map<IIdentifier, List<IObjectDelta>> _pendingObjectData;

  /**
   * out of order processing is a temporary hack. in some odd instances,
   * messages which are sent in order arrive out of order. How this is occuring
   * is currently unknown.
   */
  private Set<IIdentifier>                     _outOfOrderAdd;

  private Set<IIdentifier>                     _outOfOrderUpdate;

  public GeneralObjectHandler(IParticipant participant)
  {
    _participant = participant;
    _pendingObjectData = new HashMap<IIdentifier, List<IObjectDelta>>();
    _outOfOrderAdd = new HashSet<IIdentifier>();
    _outOfOrderUpdate = new HashSet<IIdentifier>();
  }

  protected IParticipant getParticipant()
  {
    return _participant;
  }

  synchronized public void storeObjectData(Collection<IObjectDelta> data,
      IMessage sourceMessage)
  {
    for (IObjectDelta delta : data)
    {
      IIdentifier id = delta.getIdentifier();

      List<IObjectDelta> dataList = _pendingObjectData.get(id);
      if (dataList == null)
      {
        dataList = new ArrayList<IObjectDelta>();
        _pendingObjectData.put(id, dataList);
      }

      dataList.add(delta);
      int size = dataList.size();
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Storing data for " + id + " totalUpdates:" + size);

      if (size > 2)
        if (LOGGER.isWarnEnabled())
          LOGGER.warn("Too many updates of " + id
              + ", having difficulty keeping up " + size);

      if (_outOfOrderAdd.remove(id))
      {
        if (LOGGER.isWarnEnabled())
          LOGGER.warn("Recovering out of order add of " + id
              + " from message : " + sourceMessage.getMessageId());
        addObjects(Collections.singleton(id), sourceMessage);
      }
      else if (_outOfOrderUpdate.remove(id))
      {
        if (LOGGER.isWarnEnabled())
          LOGGER.warn("Recovering out of order update of " + id
              + " from message : " + sourceMessage.getMessageId());
        updateObjects(Collections.singleton(id), sourceMessage);
      }

    }
  }

  synchronized public Collection<IObjectDelta> getPendingData(
      IIdentifier identifier)
  {
    Collection<IObjectDelta> delta = _pendingObjectData.get(identifier);
    if (delta == null) delta = Collections.emptyList();
    return delta;
  }

  @SuppressWarnings("unchecked")
  synchronized public Collection<IMutableObject> addObjects(
      Collection<IIdentifier> identifiers, IMessage sourceMessage)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Adding " + identifiers);
    Collection<IMutableObject> added = new ArrayList<IMutableObject>(
        identifiers.size());

    for (IIdentifier id : identifiers)
    {
      IIdentifier.Type type = id.getType();
      List<IObjectDelta> dataList = _pendingObjectData.get(id);

      if (dataList == null)
      {
        LOGGER.error("Have no data for " + id
            + ", defering add until we receive data. from message : "
            + sourceMessage.getMessageId());
        _outOfOrderAdd.add(id);
        continue;
      }

      /**
       * if we've received data for this object before, it is a duplicate add.
       * HOWEVER, this can happen for sensors and agents if multiple
       * participants connect at roughly the same time, see
       * reality.impl.handler.ConnectionHandler.messageReceived()
       */
      if (dataList.size() == 0)
      {
        if (!(type.equals(IIdentifier.Type.AGENT) || type
            .equals(IIdentifier.Type.SENSOR)))
          LOGGER
              .error("Got a duplicate add command for "
                  + id
                  + ", CR or a participant has screwed up big time. Ignoring. from message : "
                  + sourceMessage.getMessageId());
        else if (LOGGER.isDebugEnabled())
          LOGGER
              .debug("Got a duplicate add command for "
                  + id
                  + ", this is possible when multiple participants connect at the sametime. Ignoring.");
        continue;
      }

      IObjectDelta delta = dataList.remove(0);

      IMutableObject mo = null;
      IMutableObjectManager manager = getActualManager(type);
      switch (type)
      {
        case SENSOR:
          mo = new SensorObject(id);
          break;
        case AGENT:
          mo = new AgentObject(id);
          break;
        case AFFERENT:
          mo = new AfferentObject(id);
          break;
        case EFFERENT:
          mo = new EfferentObject(id);
          break;
        case OBJECT:
          mo = new RealObject(id);
          break;
        case EFFERENT_COMMAND:
          /*
           * this takes some more finese
           */
          String className = (String) delta
              .getNewValue(IEfferentCommand.COMMAND_CLASS_NAME);
          try
          {
            Class commandClass = getClass().getClassLoader().loadClass(
                className);
            Constructor<IEfferentCommand> cons = commandClass
                .getConstructor(IIdentifier.class);
            mo = (IMutableObject) cons.newInstance(id);
          }
          catch (Exception e)
          {
            /**
             * Error : error
             */
            LOGGER.error("Could not load efferent command class " + className
                + " ", e);
          }
          break;

        default:
          LOGGER.error("No clue what type of object to create for " + id);
      }

      if (delta instanceof ObjectDelta && mo != null) try
      {
        ((ObjectDelta) delta).apply(mo);
      }
      catch (Exception e)
      {
        LOGGER.error("Could not apply delta for " + id, e);
      }

      if (manager != null && mo != null)
      {
        manager.add(mo);
        added.add(mo);
      }
      else
        LOGGER.error("Null object or manager for " + id);
    }
    return added;
  }

  @SuppressWarnings("unchecked")
  synchronized public void removeObjects(Collection<IIdentifier> identifiers,
      IMessage sourceMessage)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Removing " + identifiers);
    for (IIdentifier id : identifiers)
    {
      List<IObjectDelta> dataList = _pendingObjectData.remove(id);

      if (dataList == null)
        LOGGER
            .error("Duplicate remove of "
                + id
                + " was received. CR or a participant screwed up. Ignoring. from message :"
                + sourceMessage.getMessageId());
      else if (dataList.size() != 0)
        LOGGER.error(dataList.size()
            + " Unprocessed object deltas were still available at removal of "
            + id + ". Safe to ignore if isolated or at the end of a run.");

      if (_outOfOrderAdd.remove(id))
        LOGGER.error("Never did receive any data for the out of order add of "
            + id + " from message :" + sourceMessage.getMessageId());
      if (_outOfOrderUpdate.remove(id))
        LOGGER
            .error("Never did receive any data for the out of order update of "
                + id + " from message :" + sourceMessage.getMessageId());

      IMutableObjectManager manager = getActualManager(id.getType());

      if (manager != null)
        manager.remove(id);
      else
        LOGGER.error("Null object manager for " + id);
    }
  }

  @SuppressWarnings("unchecked")
  synchronized public void updateObjects(Collection<IIdentifier> identifiers,
      IMessage sourceMessage)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Updating " + identifiers);
    for (IIdentifier id : identifiers)
    {
      List<IObjectDelta> dataList = _pendingObjectData.get(id);

      if (dataList == null || dataList.size() == 0)
      {
        if (LOGGER.isWarnEnabled())
          LOGGER.warn("Have no data for " + id
              + ", defering update until data arrives. from message : "
              + sourceMessage.getMessageId());
        _outOfOrderUpdate.add(id);
        continue;
      }

      IObjectDelta delta = dataList.remove(0);

      if (dataList.size() != 0 && LOGGER.isDebugEnabled())
        LOGGER.debug(dataList.size() + " deltas remain for " + id);

      IMutableObjectManager manager = getActualManager(id.getType());

      if (manager != null)
        manager.update(delta);
      else
        LOGGER.error("Null object manager for " + id);
    }
  }

  @SuppressWarnings("unchecked")
  public IMutableObjectManager getActualManager(IIdentifier.Type type)
  {
    try
    {
      switch (type)
      {
        case AGENT:
          return (IMutableObjectManager) _participant.getAgentObjectManager();
        case SENSOR:
          return (IMutableObjectManager) _participant.getSensorObjectManager();
        case AFFERENT:
          return (IMutableObjectManager) _participant
              .getAfferentObjectManager();
        case EFFERENT:
          return (IMutableObjectManager) _participant
              .getEfferentObjectManager();
        case OBJECT:
          return (IMutableObjectManager) _participant.getRealObjectManager();
        case EFFERENT_COMMAND:
          return (IMutableObjectManager) _participant
              .getEfferentCommandManager();
      }
    }
    catch (Exception e)
    {
      LOGGER.error("Could not get object manager for " + type, e);
    }
    return null;
  }
}
