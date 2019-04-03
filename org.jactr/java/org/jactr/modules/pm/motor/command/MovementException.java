package org.jactr.modules.pm.motor.command;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class MovementException extends RuntimeException
{
  /**
   * 
   */
  private static final long          serialVersionUID = -438072006595003276L;

  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(MovementException.class);

  final transient private IMovement  _movement;

  public MovementException(String message, IMovement movement)
  {
    super(message);
    _movement = movement;
  }

}
