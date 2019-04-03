package org.jactr.tools.grapher.core.selector;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class ExtensionSelector extends ClassNamedParameterSelector
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ExtensionSelector.class);

  public ExtensionSelector(String regex)
  {
    super(regex);
    // TODO Auto-generated constructor stub
  }

}
