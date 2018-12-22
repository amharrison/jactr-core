package org.commonreality.modalities.motor;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;

public class TranslateCommand extends MovementCommand
{
  /**
   * 
   */
  private static final long serialVersionUID = 993759307275979260L;
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(TranslateCommand.class);


  public TranslateCommand(IIdentifier identifier)
  {
    super(identifier);
  }
  
  public TranslateCommand(IIdentifier identifier, IIdentifier efferentId)
  {
    super(identifier, efferentId);
  }
  
  public void translate(double[] origin, double[] target, double[] rate)
  {
    setProperty(MOVEMENT_ORIGIN, origin);
    setProperty(MOVEMENT_RATE, rate);
    setProperty(MOVEMENT_TARGET, target);
  }
}
