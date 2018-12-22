package org.commonreality.modalities.motor;

/*
 * default logging
 */
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;

public class MotorConstants
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(MotorConstants.class);

  /**
   * general marker for all motor systems (ie. muscles and actuators)
   * {@link Boolean}
   */
  static public final String IS_MOTOR = "motor.isMotor";
  
  /**
   * since many motor systems have parent/child relations, this
   * marks the parent {@link IIdentifier}
   */
  static public final String PARENT_IDENTIFIER = "motor.parent";
  
  /**
   * children of this motor {@link Collection} of {@link IIdentifier}s
   */
  static public final String CHILD_IDENTIFIERS = "motor.children";
  
  /**
   * current position of the motor in the parent's coordinates, a primitive
   * double[] of unspecified length and format
   */
  static public String POSITION = "motor.position";
  
  /**
   * range of positions, again a double[] that is 2xPOSITION. Each pair
   * of doubles represents the minimum and maximum for that position.
   */
  static public String POSITION_RANGE = "motor.positionRange";
  
  /**
   * current movement rate double[] that is POSITION[]/s
   */
  static public String RATE = "motor.rate";
  
  /**
   * range of rates
   */
  static public String RATE_RANGE = "motor.rateRange";
  
  static public String NAME = "motor.name";
  
}
