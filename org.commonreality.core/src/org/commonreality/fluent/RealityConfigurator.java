package org.commonreality.fluent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.agents.IAgent;
import org.commonreality.net.message.credentials.ICredentials;
import org.commonreality.reality.CommonReality;
import org.commonreality.reality.IReality;
import org.commonreality.reality.control.RealitySetup;
import org.commonreality.reality.control.RealityShutdown;
import org.commonreality.reality.impl.DefaultReality;
import org.commonreality.sensors.ISensor;

public class RealityConfigurator extends ParticipantConfigurator<Runnable>
{

  /**
  * Logger definition
  */
  static private final transient Log LOGGER = LogFactory
      .getLog(RealityConfigurator.class);

  private Collection<ISensor>        _sensors = new ArrayList<>();

  private Collection<IAgent>         _agents  = new ArrayList<>();

  private Map<ICredentials, Boolean> _credentials = new HashMap<>();

  public RealityConfigurator()
  {
    super(new DefaultReality());
  }

  public ParticipantConfigurator<RealityConfigurator> sensor(ISensor sensor)
  {
    _sensors.add(sensor);
    return new ParticipantConfigurator<>(sensor, this);
  }

  public ParticipantConfigurator<RealityConfigurator> agent(IAgent agent)
  {
    _agents.add(agent);
    return new ParticipantConfigurator<>(agent, this);
  }

  public RealityConfigurator credentials(ICredentials credentials,
      boolean isClockOwner)
  {
    _credentials.put(credentials, isClockOwner);
    return this;
  }

  @Override
  public RealityConfigurator credentials(ICredentials credentials)
  {
    return this.credentials(credentials, false);
  }

  /**
   * terminal operation. configure reality interfaces. returns runnable that
   * when run will initialize and connect all the participants.
   * 
   * @return
   * @throws Exception
   */
  @Override
  public Runnable configure(Map<String, String> properties) throws Exception
  {
    IReality reality = CommonReality.getReality();

    _credentials.forEach((k, v) -> {
      reality.add(k, v);
    });

    reality.configure(properties);
    return new RealitySetup(reality, _sensors, _agents);
  }

  static public Runnable shutdownRunnable(boolean waitForStop)
  {
    return new RealityShutdown(CommonReality.getReality(), waitForStop);
  }
}
