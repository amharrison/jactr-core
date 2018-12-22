package org.commonreality.sensors.keyboard;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.ICompoundCommand;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.modalities.motor.TranslateCommand;
import org.commonreality.object.IAgentObject;
import org.commonreality.sensors.handlers.EfferentCommandHandler;
import org.commonreality.sensors.motor.MotorCommandHandler;

public class KeyboardMotorHandler extends MotorCommandHandler
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(KeyboardMotorHandler.class);

  public Object shouldAccept(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler handler)
  {
    Object rtn = super.shouldAccept(command, agent, handler);
    if (!Boolean.TRUE.equals(rtn)) return rtn;

    /*
     * we only accept translate, press & release or a compound of them..
     */
    if (command instanceof ICompoundCommand)
    {
      ICompoundCommand compound = (ICompoundCommand) command;
      for(IEfferentCommand component : compound.getComponents())
      {
        rtn = shouldAccept(component, agent, handler);
        if(!Boolean.TRUE.equals(rtn))
          return rtn;
      }
    }
    else if (!(command instanceof TranslateCommand
        || command instanceof PressCommand || command instanceof ReleaseCommand))
      return "Command must be translate, press, or release";
    
    
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Accepting motor command "+command);

    return Boolean.TRUE;
  }
}
