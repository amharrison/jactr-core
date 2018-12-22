package org.commonreality.fluent.service;

import org.commonreality.fluent.ParticipantConfigurator;
import org.commonreality.participant.IParticipant;
import org.commonreality.participant.impl.AbstractParticipant;

public class ClientConfigurator extends ServiceConfigurator<ClientConfigurator>
{

  protected IParticipant _participant;

  public ClientConfigurator(ParticipantConfigurator<?> parent,
      IParticipant participant)
  {
    super(parent);
    _participant = participant;
  }

  public ParticipantConfigurator<?> configure()
  {
    ((AbstractParticipant) _participant).addClientService(_provider.newClient(),
        _transport, _protocol, _transport.createAddress(_address));

    return parent();
  }
}
