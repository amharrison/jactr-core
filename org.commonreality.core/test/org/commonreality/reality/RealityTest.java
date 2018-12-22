/*
 * Created on Apr 15, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
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
package org.commonreality.reality;

import java.net.SocketAddress;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.agents.AbstractAgent;
import org.commonreality.agents.IAgent;
import org.commonreality.net.message.credentials.PlainTextCredentials;
import org.commonreality.net.protocol.IProtocolConfiguration;
import org.commonreality.net.service.IClientService;
import org.commonreality.net.service.IServerService;
import org.commonreality.net.transport.ITransportProvider;
import org.commonreality.netty.protocol.NOOPProtocol;
import org.commonreality.netty.service.ClientService;
import org.commonreality.netty.service.ServerService;
import org.commonreality.netty.transport.LocalTransportProvider;
import org.commonreality.participant.IParticipant.State;
import org.commonreality.reality.impl.DefaultReality;
import org.commonreality.sensors.AbstractSensor;
import org.commonreality.sensors.ISensor;

import junit.framework.TestCase;

/**
 * @author developer
 */
public class RealityTest extends TestCase
{
  /**
   * logger definition
   */
  static private final Log LOGGER = LogFactory.getLog(RealityTest.class);

  IReality                 _reality;

  @Override
  protected void setUp() throws Exception
  {
    _reality = new DefaultReality();
    assertFalse(_reality.stateMatches(State.INITIALIZED));
    assertFalse(_reality.stateMatches(State.STARTED));
    setupConnection(_reality);

    _reality.initialize();
    assertTrue(_reality.stateMatches(State.INITIALIZED));
  }

  protected void setupConnection(IReality reality) throws Exception
  {
    /*
     * set up default local connection
     */
    ITransportProvider local = new LocalTransportProvider();
    IProtocolConfiguration protocol = new NOOPProtocol();
    IServerService service = new ServerService();
    ((DefaultReality) reality).addServerService(service, local, protocol,
        local
        .createAddress(1));
  }

  @Override
  protected void tearDown() throws Exception
  {
    _reality.shutdown();
    assertFalse(_reality.stateMatches(State.INITIALIZED));
  }

  public void notestStartUp() throws Exception
  {

    _reality.start();
    assertTrue(_reality.stateMatches(State.STARTED));

    _reality.stop();
    assertTrue(_reality.stateMatches(State.STOPPED));

    _reality.reset(false);
    assertTrue(!_reality.stateMatches(State.STARTED));

//    _reality.shutdown();
    assertTrue(_reality.stateMatches(State.INITIALIZED));
  }

  protected ISensor createSensor() throws Exception
  {
    /*
     * mock participant
     */
    AbstractSensor participant = new AbstractSensor() {

      @Override
      public String getName()
      {
        return "mock";
      }
    };

    participant.setCredentials(new PlainTextCredentials("sensor", "pass"));

    SocketAddress address = ((DefaultReality) _reality)
        .getAddressingInformation().getSocketAddress();

    /*
     * connect..
     */
    ITransportProvider local = new LocalTransportProvider();
    IProtocolConfiguration protocol = new NOOPProtocol();
    IClientService service = new ClientService();
    participant.addClientService(service, local, protocol, address);

    assertFalse(participant.stateMatches(State.INITIALIZED));
    assertFalse(participant.stateMatches(State.STARTED));
    assertNull(participant.getIdentifier());

    return participant;
  }

  protected IAgent createAgent() throws Exception
  {
    /*
     * mock participant
     */
    AbstractAgent participant = new AbstractAgent() {

      @Override
      public String getName()
      {
        return "agent";
      }
    };

    participant.setCredentials(new PlainTextCredentials("agent", "pass"));

    SocketAddress address = ((DefaultReality) _reality)
        .getAddressingInformation().getSocketAddress();

    /*
     * connect..
     */
    ITransportProvider local = new LocalTransportProvider();
    IProtocolConfiguration protocol = new NOOPProtocol();
    IClientService service = new ClientService();
    participant.addClientService(service, local, protocol, address);

    assertFalse(participant.stateMatches(State.INITIALIZED));
    assertFalse(participant.stateMatches(State.STARTED));
    assertNull(participant.getIdentifier());

    return participant;
  }

  public void testMockSensor() throws Exception
  {
    AbstractSensor sensor = (AbstractSensor) createSensor();
    AbstractAgent agent = (AbstractAgent) createAgent();

    _reality.add(new PlainTextCredentials("agent", "pass"), false);
    _reality.add(new PlainTextCredentials("sensor", "pass"), false);
    _reality.configure(new TreeMap<String, String>());

    sensor.connect();
    sensor.waitForState(State.INITIALIZED);
    assertTrue(sensor.stateMatches(State.INITIALIZED));
    assertNotNull(sensor.getIdentifier());

    agent.connect();
    agent.waitForState(State.INITIALIZED);
    assertTrue(agent.stateMatches(State.INITIALIZED));
    assertNotNull(agent.getIdentifier());

    _reality.start();
    assertTrue(_reality.stateMatches(State.STARTED));

    sensor.waitForState(State.STARTED);
    assertTrue(sensor.stateMatches(State.STARTED));

    agent.waitForState(State.STARTED);
    assertTrue(agent.stateMatches(State.STARTED));

    // now with two kids playing, we can't test the clock..
    // IClock pClock = sensor.getClock();
    // IClock rClock = _reality.getClock();
    //
    // assertEquals(1.0, pClock.waitForTime(1));
    // assertEquals(1.0, rClock.getTime());
    // assertEquals(1.5, pClock.waitForTime(1.5));
    // assertEquals(1.5, rClock.waitForTime(1.5));

    _reality.stop();



    _reality.reset(true);
    //
    // assertEquals(0.0, rClock.getTime());
    // assertEquals(0.0, pClock.waitForTime(0));

  }
}
