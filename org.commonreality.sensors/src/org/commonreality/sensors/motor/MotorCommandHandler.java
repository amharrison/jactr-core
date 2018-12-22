package org.commonreality.sensors.motor;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.modalities.motor.MotorUtilities;
import org.commonreality.modalities.motor.MovementCommand;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.identifier.ISensoryIdentifier;
import org.commonreality.sensors.handlers.AbstractCommandHandlerDelegate;
import org.commonreality.sensors.handlers.EfferentCommandHandler;
import org.commonreality.sensors.handlers.ICommandTimingEquation;

/**
 * @author harrison
 */
public class MotorCommandHandler extends AbstractCommandHandlerDelegate
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(MotorCommandHandler.class);

  private IActuator                  _actuator;

  private ICommandTimingEquation     _timingEquation;

  public void setTimingEquation(ICommandTimingEquation equation)
  {
    _timingEquation = equation;
  }

  public void setActuator(IActuator actuator)
  {
    _actuator = actuator;
  }

  public IActuator getActuator()
  {
    return _actuator;
  }

  /**
   * only accept motor commands where the source matched the provided efferent
   */
  @Override
  public Object shouldAccept(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler handler)
  {
    if (!(command instanceof MovementCommand)) return "Not a movement";

    ISensoryIdentifier efferent = command.getEfferentIdentifier();

    IEfferentObject motor = handler.getSensor().getEfferentObjectManager().get(efferent);

    if (!MotorUtilities.isMotor(motor))
    {
      String msg = efferent + " is not a motor.";
      if (LOGGER.isWarnEnabled()) LOGGER.warn(msg);
      return msg;
    }

    return super.shouldAccept(command, agent, handler);
  }

  @Override
  public Object shouldStart(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler handler)
  {
    if (!(command instanceof MovementCommand)) return "Not a movement";

    return Boolean.TRUE;
  }

  public void start(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler parent)
  {
    getActuator().start(agent, (MovementCommand) command, parent);
  }

  public void abort(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler parent)
  {
    getActuator().abort(agent, (MovementCommand) command, parent);
  }

  public ICommandTimingEquation getTimingEquation(IEfferentCommand command,
      IAgentObject agent, EfferentCommandHandler handler)
  {
    return _timingEquation;
  }

  public boolean isInterestedIn(IEfferentCommand command)
  {
    return command instanceof MovementCommand;
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
