package org.commonreality.agents;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.impl.EfferentCommandManager;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.identifier.IIdentifier.Type;
import org.commonreality.identifier.impl.BasicIdentifier;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.manager.impl.AfferentObjectManager;
import org.commonreality.object.manager.impl.AgentObject;
import org.commonreality.object.manager.impl.AgentObjectManager;
import org.commonreality.object.manager.impl.EfferentObjectManager;
import org.commonreality.object.manager.impl.SensorObjectManager;
import org.commonreality.participant.impl.ThinParticipant;
import org.commonreality.reality.CommonReality;
import org.commonreality.time.IClock;

public class ThinAgent extends ThinParticipant implements IAgent
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ThinAgent.class);

  private String                     _uniqueName;

  public ThinAgent(String name, IClock clock)
  {
    super(Type.AGENT);
    _uniqueName = name;
    setIdentifier(new BasicIdentifier(_uniqueName, Type.AGENT, null));
    setClock(clock);
  }

  protected IAgentObject createAgent(IIdentifier identifier)
  {
    AgentObject ao = new AgentObject(identifier);
    ao.setProperty("name", _uniqueName);
    return ao;
  }

  @Override
  public void setIdentifier(IIdentifier identifier)
  {
    if (getIdentifier() != null)
      throw new RuntimeException("identifier is already set");

    IAgentObject agent = createAgent(identifier);

    getAgentObjectManager().add(agent);

    super.setIdentifier(identifier);
  }

  @Override
  public SensorObjectManager getSensorObjectManager()
  {
    return (SensorObjectManager) super.getSensorObjectManager();
  }

  @Override
  public AfferentObjectManager getAfferentObjectManager()
  {
    return (AfferentObjectManager) super.getAfferentObjectManager();
  }

  @Override
  public EfferentObjectManager getEfferentObjectManager()
  {
    return (EfferentObjectManager) super.getEfferentObjectManager();
  }

  @Override
  public AgentObjectManager getAgentObjectManager()
  {
    return (AgentObjectManager) super.getAgentObjectManager();
  }

  @Override
  public EfferentCommandManager getEfferentCommandManager()
  {
    return (EfferentCommandManager) super.getEfferentCommandManager();
  }

  @Override
  public void connect() throws Exception
  {
    IClock clock = getClock();
    if (clock == null)
      throw new IllegalStateException("Clock must be defined before connecting");

    super.connect();

    CommonReality.addAgent(this);
  }

  @Override
  public void disconnect(boolean force) throws Exception
  {
    CommonReality.removeAgent(this);
    setClock(null);
    super.disconnect(force);
  }
}
