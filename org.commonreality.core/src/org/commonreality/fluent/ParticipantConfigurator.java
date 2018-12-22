package org.commonreality.fluent;

import java.util.Map;
import java.util.Objects;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.agents.IAgent;
import org.commonreality.fluent.service.ClientConfigurator;
import org.commonreality.fluent.service.ServerConfigurator;
import org.commonreality.net.message.credentials.ICredentials;
import org.commonreality.participant.IParticipant;
import org.commonreality.sensors.ISensor;

/**
 * @author harrison
 */
public class ParticipantConfigurator<R>
{
  /**
  * Logger definition
  */
  static private final transient Log LOGGER = LogFactory
      .getLog(ParticipantConfigurator.class);


  private ICredentials               _credentials;

  protected IParticipant             _participant;

  protected R                        _parent;

  public ParticipantConfigurator(IParticipant participant, R parent)
  {
    this(participant);
    _parent = parent;
  }

  public ParticipantConfigurator(IParticipant participant)
  {
    _participant = participant;
  }


  public ParticipantConfigurator<R> credentials(ICredentials creds)
  {
    _credentials = creds;
    return this;
  }

  public ServerConfigurator server()
  {
    return new ServerConfigurator(this, _participant);
  }

  public ClientConfigurator client()
  {
    return new ClientConfigurator(this, _participant);
  }

  public R configure(Map<String, String> properties) throws Exception
  {
    Objects.requireNonNull(_credentials, "credentials must be provided");

    if (_participant instanceof ISensor)
      ((ISensor) _participant).setCredentials(_credentials);
    if (_participant instanceof IAgent)
      ((IAgent) _participant).setCredentials(_credentials);


    _participant.configure(properties);
    return _parent;
  }

}
