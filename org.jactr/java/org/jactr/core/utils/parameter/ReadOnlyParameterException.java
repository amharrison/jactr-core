package org.jactr.core.utils.parameter;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class ReadOnlyParameterException extends ParameterException
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ReadOnlyParameterException.class);


  public ReadOnlyParameterException(String arg0)
  {
    super(String.format("%s is a ready only parameter", arg0));
  }



}
