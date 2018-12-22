package org.commonreality.sensors.handlers;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.efferent.IEfferentCommandTemplate;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.identifier.ISensoryIdentifier;
import org.commonreality.sensors.ISensor;

public abstract class AbstractCommandHandlerDelegate implements ICommandHandlerDelegate
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractCommandHandlerDelegate.class);

  

 

  public boolean shouldAbort(IEfferentCommand command, IAgentObject agent, EfferentCommandHandler handler)
  {
    return Boolean.FALSE;
  }

  public Object shouldAccept(IEfferentCommand command, IAgentObject agent, EfferentCommandHandler handler)
  {
    /*
     * security check..
     */
    ISensoryIdentifier efferent = command.getEfferentIdentifier();

    IEfferentObject muscle = handler.getSensor().getEfferentObjectManager()
        .get(efferent);

    IIdentifier agentId = command.getIdentifier().getAgent();

    if (!efferent.getAgent().equals(agentId))
    {
      String msg = agentId + " attempted to act on someone else's IEfferentObject "
          + efferent.getAgent();
      if (LOGGER.isWarnEnabled()) LOGGER.warn(msg);

      return msg;
    }

    for (IEfferentCommandTemplate template : muscle.getCommandTemplates())
      if (template.isConsistent(command)) return Boolean.TRUE;

    return Boolean.FALSE;
  }

  public Object shouldStart(IEfferentCommand command, IAgentObject agent, EfferentCommandHandler handler)
  {
    return Boolean.FALSE;
  }

}
