/*
 * Created on May 10, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.sensors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.executor.InlineExecutor;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.command.object.IObjectCommand;
import org.commonreality.net.message.request.object.ObjectCommandRequest;
import org.commonreality.net.message.request.object.ObjectDataRequest;
import org.commonreality.net.message.request.time.RequestTime;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.ISensorObject;
import org.commonreality.object.delta.FullObjectDelta;
import org.commonreality.object.manager.IAfferentObjectManager;
import org.commonreality.object.manager.IAgentObjectManager;
import org.commonreality.object.manager.IEfferentObjectManager;
import org.commonreality.object.manager.IRealObjectManager;
import org.commonreality.object.manager.IRequestableAfferentObjectManager;
import org.commonreality.object.manager.IRequestableEfferentObjectManager;
import org.commonreality.object.manager.IRequestableRealObjectManager;
import org.commonreality.object.manager.event.IAgentListener;
import org.commonreality.object.manager.event.IObjectEvent;
import org.commonreality.object.manager.impl.AfferentObjectManager;
import org.commonreality.object.manager.impl.AgentObjectManager;
import org.commonreality.object.manager.impl.EfferentObjectManager;
import org.commonreality.object.manager.impl.SensorObject;
import org.commonreality.participant.addressing.IAddressingInformation;
import org.commonreality.participant.impl.AbstractParticipant;
import org.commonreality.participant.impl.RequestableAfferentObjectManager;
import org.commonreality.participant.impl.RequestableEfferentObjectManager;
import org.commonreality.participant.impl.RequestableRealObjectManager;
import org.commonreality.reality.CommonReality;
import org.commonreality.time.IClock;
import org.commonreality.time.impl.NetworkedClock;

/**
 * @author developer
 */
public abstract class AbstractSensor extends AbstractParticipant implements
    ISensor
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractSensor.class);



  private Set<IIdentifier>           _relevantAgents;

  /**
   * @param type
   */
  public AbstractSensor()
  {
    super(IIdentifier.Type.SENSOR);
    _relevantAgents = new HashSet<IIdentifier>();
  }



  /**
   * are we currently interfaced with this agent
   * 
   * @param identifier
   * @return
   */
  protected boolean isInterfacedAgent(IIdentifier identifier)
  {
    return _relevantAgents.contains(identifier);
  }

  public Collection<IIdentifier> getInterfacedAgents()
  {
    return getInterfacedAgents(new ArrayList<IIdentifier>(
        _relevantAgents.size()));
  }

  public Collection<IIdentifier> getInterfacedAgents(
      Collection<IIdentifier> container)
  {
    container.addAll(_relevantAgents);
    return container;
  }

  /**
   * should we provide afferent/efferent objects to this agent default returns
   * true
   * 
   * @param agent
   * @return true
   */
  protected boolean shouldInterface(IAgentObject agent)
  {
    return true;
  }

  /**
   * callback when a new agent is added to te simulation (will only receive is
   * {@link #shouldInterface(IAgentObject)} is true). <b>This is called on the
   * IO thread, you should not make any blocking calls here</b>
   * 
   * @param agent
   */
  protected void agentAdded(IAgentObject agent)
  {
    IIdentifier id = agent.getIdentifier();
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Interfacing with " + id + ", prefetching");
    _relevantAgents.add(agent.getIdentifier());
    ((RequestableAfferentObjectManager) getAfferentObjectManager())
        .prefetch(id);
    ((RequestableEfferentObjectManager) getEfferentObjectManager())
        .prefetch(id);
  }

  /**
   * callback when an interfaced agent is removed from the simulation <b>This is
   * called on the IO thread, you should not make any blocking calls here</b>
   * 
   * @param agent
   */
  protected void agentRemoved(IAgentObject agent)
  {
    IIdentifier agentId = agent.getIdentifier();
    _relevantAgents.remove(agentId);
    /*
     * clean house
     */
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Agent " + agentId
          + " has been removed, cleaning up objects");

    Collection<IIdentifier> affToRemove = new ArrayList<IIdentifier>(
        getAfferentObjectManager().getIdentifiersByAgent(agentId));
    Collection<IIdentifier> effToRemove = new ArrayList<IIdentifier>(
        getEfferentObjectManager().getIdentifiersByAgent(agentId));

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Removing afferents " + affToRemove);
    // use the bulk operation
    ((AfferentObjectManager) getAfferentObjectManager()).remove(affToRemove);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("removing efferents " + effToRemove);
    ((EfferentObjectManager) getEfferentObjectManager()).remove(effToRemove);
  }

  /**
   * callback for when an actively interfaced agent is updated <b>This is called
   * on the IO thread, you should not make any blocking calls here</b>
   * 
   * @param agent
   */
  protected void agentUpdated(IAgentObject agent)
  {

  }

  @Override
  protected IAgentObjectManager createAgentObjectManager()
  {
    IAgentObjectManager aom = new AgentObjectManager();
    IAgentListener agentListener = new IAgentListener() {

      public void objectsAdded(IObjectEvent<IAgentObject, ?> addEvent)
      {
        for (IAgentObject agent : addEvent.getObjects())
          if (shouldInterface(agent)) agentAdded(agent);
      }

      public void objectsRemoved(IObjectEvent<IAgentObject, ?> removeEvent)
      {
        /*
         * clean up
         */
        for (IAgentObject agent : removeEvent.getObjects())
        {
          IIdentifier agentId = agent.getIdentifier();
          if (isInterfacedAgent(agentId)) agentRemoved(agent);
        }
      }

      public void objectsUpdated(IObjectEvent<IAgentObject, ?> updateEvent)
      {
        for (IAgentObject agent : updateEvent.getObjects())
          if (isInterfacedAgent(agent.getIdentifier())) agentUpdated(agent);
      }

    };

    aom.addListener(agentListener, InlineExecutor.get());

    return aom;
  }

  @Override
  protected IAfferentObjectManager createAfferentObjectManager()
  {
    return new RequestableAfferentObjectManager(this);
  }

  @Override
  protected IEfferentObjectManager createEfferentObjectManager()
  {
    return new RequestableEfferentObjectManager(this);
  }

  @Override
  protected IRealObjectManager createRealObjectManager()
  {
    return new RequestableRealObjectManager(this);
  }

  protected ISensorObject createSensor(IIdentifier identifier)
  {
    return new SensorObject(identifier);
  }

  @Override
  public void setIdentifier(IIdentifier identifier)
  {
    if (getIdentifier() != null)
      throw new RuntimeException("identifier is already set");

    ISensorObject sensor = createSensor(identifier);

    super.setIdentifier(identifier);
    /*
     * tell CR what I am like..
     */
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Notifying CR about myself");
    if (getSession() != null)
    {
      send(new ObjectDataRequest(identifier, IIdentifier.ALL,
          Collections.singleton(new FullObjectDelta(sensor))));
      send(new ObjectCommandRequest(identifier, IIdentifier.ALL,
          IObjectCommand.Type.ADDED, Collections.singleton(identifier)));
    }
    else if (LOGGER.isWarnEnabled())
      LOGGER
          .warn(String.format("Session is null but we have received our ID?"));
  }

  @Override
  public IRequestableEfferentObjectManager getEfferentObjectManager()
  {
    return (IRequestableEfferentObjectManager) super.getEfferentObjectManager();
  }

  @Override
  public IRequestableAfferentObjectManager getAfferentObjectManager()
  {
    return (IRequestableAfferentObjectManager) super.getAfferentObjectManager();
  }

  @Override
  public IRequestableRealObjectManager getRealObjectManager()
  {
    return (IRequestableRealObjectManager) super.getRealObjectManager();
  }

  /**
   * by default, we dont open any listeners, so we return null
   * 
   * @see org.commonreality.participant.impl.AbstractParticipant#getAddressingInformation()
   */
  @Override
  public IAddressingInformation getAddressingInformation()
  {
    return null;
  }

  @Override
  public void shutdown() throws Exception
  {
    /*
     * send out the remove command for ourselves
     */
    if (getSession() != null)
      send(new ObjectCommandRequest(getIdentifier(), IIdentifier.ALL,
          IObjectCommand.Type.REMOVED, Collections.singleton(getIdentifier())));
    else if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Shutdown but not connected?"));

    super.shutdown();
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
    CommonReality.addSensor(this);
  }

  @Override
  public void disconnect(boolean force) throws Exception
  {
    CommonReality.removeSensor(this);
    setClock(null);
    super.disconnect(force);
  }
}
