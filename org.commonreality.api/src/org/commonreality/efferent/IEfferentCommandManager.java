package org.commonreality.efferent;

/*
 * default logging
 */
import java.util.Collection;

import org.commonreality.efferent.event.IEfferentCommandListener;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.manager.IObjectManager;

public interface IEfferentCommandManager extends
    IObjectManager<IEfferentCommand, IEfferentCommandListener>
{

  public Collection<IEfferentCommand> getEfferentCommands(
      IEfferentCommand.ActualState actualState);

  public Collection<IEfferentCommand> getEfferentCommands(
      IEfferentCommand.RequestedState requestedState);
  
  public Collection<IIdentifier> getIdentifiersBySensor(IIdentifier sensorId);

  public Collection<IIdentifier> getIdentifiersByAgent(IIdentifier agentId);

}
