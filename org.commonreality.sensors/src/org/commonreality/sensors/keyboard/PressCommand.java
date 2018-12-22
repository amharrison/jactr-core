package org.commonreality.sensors.keyboard;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.agents.IAgent;
import org.commonreality.efferent.AbstractEfferentCommandTemplate;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.efferent.IEfferentCommandManager;
import org.commonreality.efferent.IEfferentCommandTemplate;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.motor.MotorConstants;
import org.commonreality.modalities.motor.MotorUtilities;
import org.commonreality.modalities.motor.MovementCommand;
import org.commonreality.modalities.vocal.VocalUtilities;
import org.commonreality.modalities.vocal.VocalizationCommand;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.manager.IRequestableObjectManager;

public class PressCommand extends MovementCommand
{
  /**
   * 
   */
  private static final long serialVersionUID = -2216262262751862703L;
  
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(PressCommand.class);
  
  
   

  public PressCommand(IIdentifier identifier)
  {
    super(identifier);
    setProperty(MOVEMENT_RATE, new double[]{0,0,20}); //0.05 sec
    setProperty(MOVEMENT_TARGET, new double[]{Double.NaN, Double.NaN,0});
  }
  
  public PressCommand(IIdentifier identifier, IIdentifier efferentId)
  {
    this(identifier);
    setEfferentIdentifier(efferentId);
  }
  
  public void press(double[] origin, double[] target, double[] rate)
  {
    setProperty(MOVEMENT_ORIGIN, origin);
    setProperty(MOVEMENT_RATE, rate); //0.05 sec
    setProperty(MOVEMENT_TARGET, target);
  }
  
}
