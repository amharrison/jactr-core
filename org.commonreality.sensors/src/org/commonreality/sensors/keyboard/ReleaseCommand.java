package org.commonreality.sensors.keyboard;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.motor.MotorConstants;
import org.commonreality.modalities.motor.MotorUtilities;
import org.commonreality.modalities.motor.MovementCommand;

public class ReleaseCommand extends MovementCommand
{
  /**
   * 
   */
  private static final long          serialVersionUID = -2216262262751862703L;

  /**
   * Logger definition
   */
  static private final transient Log LOGGER           = LogFactory
                                                          .getLog(ReleaseCommand.class);

  
  
  public ReleaseCommand(IIdentifier identifier)
  {
    super(identifier);
    setProperty(MOVEMENT_RATE, new double[] { 0, 0, 20 }); // 0.05 sec
    setProperty(MOVEMENT_TARGET, new double[] { Double.NaN, Double.NaN, 1 });
  }
  
  public ReleaseCommand(IIdentifier identifier, IIdentifier efferentId)
  {
    this(identifier);
    setEfferentIdentifier(efferentId);
  }

  public void release(double[] origin, double[] target, double[] rate)
  {
    setProperty(MOVEMENT_ORIGIN, origin);
    setProperty(MOVEMENT_RATE, rate); // 0.05 sec
    setProperty(MOVEMENT_TARGET, target);
  }

}
