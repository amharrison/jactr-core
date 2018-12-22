package org.commonreality.reality.control;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.agents.IAgent;
import org.commonreality.participant.IParticipant.State;
import org.commonreality.reality.IReality;
import org.commonreality.sensors.ISensor;

public class RealitySetup implements Runnable
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RealitySetup.class);

  private final IReality             _reality;

  private Collection<ISensor>        _sensors;

  private Collection<IAgent>         _agents;

  public RealitySetup(IReality reality, Collection<ISensor> sensors,
      Collection<IAgent> agents)
  {
    _reality = reality;
    _sensors = new ArrayList<ISensor>(sensors);
    _agents = new ArrayList<IAgent>(agents);
  }

  public void run()
  {
    /*
     * initialize CR
     */
    if (_reality != null) try
    {
      _reality.initialize();
      _reality.waitForState(State.INITIALIZED);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Could not initialize common reality ", e);
    }

    /*
     * and connect everyone
     */
    for (ISensor sensor : _sensors)
      try
      {
        sensor.connect();
        /*
         * we could wait for initialized, but by doing this we allow the
         * participants to initialize in parallel. we must check for both states
         * because it is possible that it would reach initialized before wait
         * for state(connected) is even called.
         */
        sensor.waitForState(State.CONNECTED, State.INITIALIZED);
        // sensor.waitForState(State.INITIALIZED);
      }
      catch (Exception e)
      {
        throw new RuntimeException("Could not connect sensor " + sensor, e);
      }

    for (IAgent agent : _agents)
      try
      {
        agent.connect();
        agent.waitForState(State.CONNECTED, State.INITIALIZED);
        // agent.waitForState(State.INITIALIZED);
      }
      catch (Exception e)
      {
        throw new RuntimeException("Could not connect agent " + agent, e);
      }

    /*
     * now, one last time, make sure everyone is initialized before returning
     */
    for (ISensor sensor : _sensors)
      try
      {
        sensor.waitForState(State.INITIALIZED);
      }
      catch (Exception e)
      {
        throw new RuntimeException("Could not initialize sensor " + sensor, e);
      }

    for (IAgent agent : _agents)
      try
      {
        agent.waitForState(State.INITIALIZED);
      }
      catch (Exception e)
      {
        throw new RuntimeException("Could not initialize agent " + agent, e);
      }

      if (LOGGER.isDebugEnabled())
      LOGGER.debug("Reality, sensors and agents are ready to go!");
  }

}
