package org.jactr.io.parser;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class CanceledException extends RuntimeException
{
  /**
   * 
   */
  private static final long serialVersionUID = -7382746231986528250L;
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(CanceledException.class);

}
