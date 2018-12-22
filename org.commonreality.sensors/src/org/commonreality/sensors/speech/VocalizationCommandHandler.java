package org.commonreality.sensors.speech;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.modalities.vocal.VocalConstants;
import org.commonreality.modalities.vocal.VocalUtilities;
import org.commonreality.modalities.vocal.VocalizationCommand;
import org.commonreality.modalities.vocal.VocalizationCommandTemplate;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.delta.DeltaTracker;
import org.commonreality.object.identifier.ISensoryIdentifier;
import org.commonreality.sensors.handlers.AbstractCommandHandlerDelegate;
import org.commonreality.sensors.handlers.EfferentCommandHandler;
import org.commonreality.sensors.handlers.ICommandTimingEquation;

/**
 * this generalized vocalization command handler can be used by any sensor that
 * wants to avoid vocal abilities. A few things to note: any agent that can
 * speak must have an {@link IEfferentObject} associated with it (by the sensor)
 * that has {@link VocalConstants#CAN_VOCALIZE} set to true. This
 * {@link IEfferentObject} must also have a {@link VocalizationCommandTemplate}
 * as a member of the collection {@link IEfferentObject#COMMAND_TEMPLATES}. The
 * sensor must also be able to track the vocalizations (recommend extending
 * {@link #start(DeltaTracker)}) and then set the command as completed upon
 * termination {@link #completed(IEfferentCommand, Object)} or
 * {@link #aborted(IEfferentCommand, Object)} <br>
 * This class will then handle the commands on the provided executor. And when a
 * vocalization is actually executed it will be delegated to {@link ISpeaker}
 * 
 * @author harrison
 */
public class VocalizationCommandHandler extends AbstractCommandHandlerDelegate
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(VocalizationCommandHandler.class);

  private ISpeaker                   _speaker;
  private ICommandTimingEquation _equation;

  public VocalizationCommandHandler(ICommandTimingEquation duration, ISpeaker speaker)
  {
    setSpeaker(speaker);
    _equation = duration;
  }
  
  public void setSpeaker(ISpeaker speaker)
  {
    _speaker = speaker;
  }
  
  public ISpeaker getSpeaker()
  {
    return _speaker;
  }

  /**
   * only accept vocalization commands where the source matched the provided
   * efferent
   */
  @Override
  public Object shouldAccept(IEfferentCommand command, IAgentObject agent, EfferentCommandHandler handler)
  {
    if (!(command instanceof VocalizationCommand)) return "Not a vocalization";

    ISensoryIdentifier efferent = command.getEfferentIdentifier();

    IEfferentObject mouth = handler.getSensor().getEfferentObjectManager()
        .get(efferent);

    if (!VocalUtilities.canVocalize(mouth))
    {
      String msg = efferent + " cannot vocalize.";
      if (LOGGER.isWarnEnabled()) LOGGER.warn(msg);
      return msg;
    }

    return super.shouldAccept(command, agent, handler);
  }



  @Override
  public Object shouldStart(IEfferentCommand command, IAgentObject agent, EfferentCommandHandler handler)
  {
    if (!(command instanceof VocalizationCommand)) return "Not vocalization";

    return Boolean.TRUE;
  }


  public void abort(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler parent)
  {
    //Noop
    
  }

  public ICommandTimingEquation getTimingEquation(IEfferentCommand command,
      IAgentObject agent, EfferentCommandHandler handler)
  {
    return _equation;
  }

  public void start(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler parent)
  {
    VocalizationCommand vocalCom = (VocalizationCommand) command;

    getSpeaker().speak(agent, vocalCom);
  }

  public boolean isInterestedIn(IEfferentCommand command)
  {
   return command instanceof VocalizationCommand;
  }

  public void aborted(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler parent)
  {
    // TODO Auto-generated method stub

  }

  public void rejected(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler parent)
  {
    // TODO Auto-generated method stub

  }

  public void started(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler parent)
  {
    // TODO Auto-generated method stub

  }
}
