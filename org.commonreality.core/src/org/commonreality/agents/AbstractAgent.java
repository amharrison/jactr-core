/*
 * Created on May 14, 2007 Copyright (C) 2001-2007, Anthony Harrison
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
package org.commonreality.agents;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.efferent.IEfferentCommandManager;
import org.commonreality.executor.InlineExecutor;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.command.object.IObjectCommand;
import org.commonreality.net.message.credentials.ICredentials;
import org.commonreality.net.message.request.object.ObjectCommandRequest;
import org.commonreality.net.message.request.object.ObjectDataRequest;
import org.commonreality.net.message.request.time.RequestTime;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.ISensorObject;
import org.commonreality.object.delta.FullObjectDelta;
import org.commonreality.object.identifier.ISensoryIdentifier;
import org.commonreality.object.manager.ISensorObjectManager;
import org.commonreality.object.manager.event.IObjectEvent;
import org.commonreality.object.manager.event.ISensorListener;
import org.commonreality.object.manager.impl.AgentObject;
import org.commonreality.object.manager.impl.SensorObjectManager;
import org.commonreality.participant.addressing.IAddressingInformation;
import org.commonreality.participant.impl.AbstractParticipant;
import org.commonreality.participant.impl.RequestableEfferentCommandManager;
import org.commonreality.reality.CommonReality;
import org.commonreality.time.IClock;
import org.commonreality.time.impl.NetworkedClock;

/**
 * @author developer
 */
public abstract class AbstractAgent extends AbstractParticipant implements
    IAgent
{

  /**
   * logger definition
   */
  static private final Log LOGGER = LogFactory.getLog(AbstractAgent.class);

  private ICredentials     _credentials;

  /**
   * @param type
   */
  public AbstractAgent()
  {
    super(IIdentifier.Type.AGENT);
  }

  /**
   * @see org.commonreality.participant.impl.AbstractParticipant#getCredentials()
   */
  @Override
  public ICredentials getCredentials()
  {
    return _credentials;
  }

  /**
   * @see org.commonreality.participant.impl.AbstractParticipant#getName()
   */
  @Override
  abstract public String getName();

  /**
   * @see org.commonreality.agents.IAgent#setCredentials(org.commonreality.net.message.credentials.ICredentials)
   */
  @Override
  public void setCredentials(ICredentials credentials)
  {
    _credentials = credentials;
  }

  /**
   * by default, we dont accept connections so return null
   * 
   * @see org.commonreality.participant.impl.AbstractParticipant#getAddressingInformation()
   */
  @Override
  public IAddressingInformation getAddressingInformation()
  {
    return null;
  }

  protected IAgentObject createAgent(IIdentifier identifier)
  {
    return new AgentObject(identifier);
  }

  /**
   * overriden to provide a {@link RequestableEfferentCommandManager} so that
   * agents can create new {@link IEfferentCommand}s on the fly.
   */
  @Override
  protected IEfferentCommandManager createEfferentCommandManager()
  {
    return new RequestableEfferentCommandManager(this);
  }

  @Override
  protected ISensorObjectManager createSensorObjectManager()
  {
    ISensorObjectManager som = new SensorObjectManager();
    ISensorListener sensorListener = new ISensorListener() {

      public void objectsAdded(IObjectEvent<ISensorObject, ?> addEvent)
      {
        for (ISensorObject sensor : addEvent.getObjects())
          sensorAdded(sensor);
      }

      public void objectsRemoved(IObjectEvent<ISensorObject, ?> removeEvent)
      {
        /*
         * clean up
         */
        for (ISensorObject sensor : removeEvent.getObjects())
        {
          sensor.getIdentifier();
          sensorRemoved(sensor);
        }
      }

      public void objectsUpdated(IObjectEvent<ISensorObject, ?> updateEvent)
      {
        for (ISensorObject sensor : updateEvent.getObjects())
          sensorUpdated(sensor);
      }

    };

    som.addListener(sensorListener, InlineExecutor.get());

    return som;
  }

  /**
   * callback in response to a sensor change. This is called on the main IO
   * thread, so there should be no blocking calls
   * 
   * @param sensor
   */
  protected void sensorAdded(ISensorObject sensor)
  {
    /*
     * prefetch some IEfferentCommands
     */
    ((RequestableEfferentCommandManager) getEfferentCommandManager())
        .prefetch(sensor.getIdentifier());
  }

  /**
   * callback in response to a sensor change. This is called on the main IO
   * thread, so there should be no blocking calls
   * 
   * @param sensor
   */
  protected void sensorRemoved(ISensorObject sensor)
  {
    IIdentifier sId = sensor.getIdentifier();

    /*
     * find all the efferent commands that havent been removed and remove them..
     */
    ArrayList<IIdentifier> toRemove = new ArrayList<IIdentifier>(1);
    for (IIdentifier identifier : getEfferentCommandManager().getIdentifiers())
    {
      IIdentifier sensorId = ((ISensoryIdentifier) identifier).getSensor();
      if (sensorId.equals(sId)) toRemove.add(identifier);
    }

    /*
     * and remove..
     */
    ((RequestableEfferentCommandManager) getEfferentCommandManager())
        .remove(toRemove);
  }

  /**
   * callback in response to a sensor change. This is called on the main IO
   * thread, so there should be no blocking calls
   * 
   * @param sensor
   */
  protected void sensorUpdated(ISensorObject sensor)
  {

  }

  @Override
  public void setIdentifier(IIdentifier identifier)
  {
    if (getIdentifier() != null)
      throw new RuntimeException("identifier is already set");

    IAgentObject agent = createAgent(identifier);

    super.setIdentifier(identifier);
    /*
     * tell everyone what I am like..
     */
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Notifying CR about myself");
    if (getSession() != null)
    {
      send(new ObjectDataRequest(identifier, IIdentifier.ALL,
          Collections.singleton(new FullObjectDelta(agent))));
      send(new ObjectCommandRequest(identifier, IIdentifier.ALL,
          IObjectCommand.Type.ADDED, Collections.singleton(identifier)));
    }
    else if (LOGGER.isWarnEnabled())
      LOGGER
          .warn(String.format("Session is null but we have received our ID?"));
  }

  @Override
  public void shutdown(boolean force) throws Exception
  {
    /*
     * send out the remove command for ourselves
     */
    if (getSession() != null)
      send(new ObjectCommandRequest(getIdentifier(), IIdentifier.ALL,
          IObjectCommand.Type.REMOVED, Collections.singleton(getIdentifier())));
    else if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Shutdown but not connected?"));

    super.shutdown(force);
  }

  @Override
  public void connect() throws Exception
  {
    super.connect();
    IClock clock = new NetworkedClock(0.05, (globalTime, netClock) -> {
      // timeshift is taken care of by the clock
        send(new RequestTime(getIdentifier(), globalTime));
      });
    setClock(clock);
    CommonReality.addAgent(this);
  }

  @Override
  public void disconnect() throws Exception
  {
    CommonReality.removeAgent(this);
    setClock(null);
    super.disconnect();
  }

}
