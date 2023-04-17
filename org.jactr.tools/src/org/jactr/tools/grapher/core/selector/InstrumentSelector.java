package org.jactr.tools.grapher.core.selector;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class InstrumentSelector extends ClassNamedParameterSelector
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(InstrumentSelector.class);

  public InstrumentSelector(String regex)
  {
    super(regex);
  }

}
