package org.commonreality.sensors.keyboard;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.agents.IAgent;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.motor.MotorUtilities;
import org.commonreality.modalities.motor.MovementCommand;
import org.commonreality.modalities.motor.MovementCommandTemplate;
import org.commonreality.object.IEfferentObject;

public class ReleaseCommandTemplate extends MovementCommandTemplate<ReleaseCommand>
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ReleaseCommandTemplate.class);

  /**
   * 
   */
  private static final long serialVersionUID = 0L;
  
  public ReleaseCommandTemplate()
  {
    super("release","release");
  }


  public boolean isConsistent(IEfferentCommand command)
  {
    return command instanceof PressCommand;
  }

  @Override
  protected void configure(ReleaseCommand command, IAgent agent,
      IEfferentObject object)
  {
    command.setProperty(MovementCommand.MOVEMENT_ORIGIN, MotorUtilities.getPosition(object));
  }

  @Override
  protected ReleaseCommand create(IIdentifier commandId, IIdentifier muscleId)
  {
    return new ReleaseCommand(commandId, muscleId);
  }

}
