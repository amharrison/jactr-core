package org.commonreality.fluent.service;

import org.commonreality.fluent.ParticipantConfigurator;
import org.commonreality.participant.IParticipant;
import org.commonreality.participant.impl.AbstractParticipant;

public class ServerConfigurator extends ServiceConfigurator<ServerConfigurator>
{

  protected IParticipant _participant;

  public ServerConfigurator(ParticipantConfigurator<?> parent,
      IParticipant participant)
  {
    super(parent);
    _participant = participant;
  }



  public ParticipantConfigurator<?> configure()
  {
    ((AbstractParticipant) _participant).addServerService(_provider.newServer(),
        _transport, _protocol, _transport.createAddress(_address));

    return parent();
  }
}
