package org.jactr.tools.grapher.core.selector;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class BufferSelector extends ClassNamedParameterSelector
{

  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(BufferSelector.class);

  public BufferSelector(String regex)
  {
    super(regex);
  }
}
