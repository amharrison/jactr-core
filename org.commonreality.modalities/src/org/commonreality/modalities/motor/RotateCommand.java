package org.commonreality.modalities.motor;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;

public class RotateCommand extends MovementCommand
{
  /**
   * 
   */
  private static final long serialVersionUID = -8873033692950206435L;
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RotateCommand.class);

  public RotateCommand(IIdentifier identifier)
  {
    super(identifier);
  }
  
  public RotateCommand(IIdentifier identifier, IIdentifier efferentId)
  {
    super(identifier, efferentId);
  }

  public void rotate(double[] target, double[] rate)
  {
    setProperty(MOVEMENT_RATE, rate);
    setProperty(MOVEMENT_TARGET, target);
  }
}
