package org.jactr.entry.iterative;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class TerminateIterativeRunException extends Exception
{
  /**
   * 
   */
  private static final long          serialVersionUID = 4941116438896123697L;

  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(TerminateIterativeRunException.class);

  public TerminateIterativeRunException(String message)
  {
    super(message);
  }

}
