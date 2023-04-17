package org.jactr.tools.grapher.core.selector;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class ModuleSelector extends ClassNamedParameterSelector
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ModuleSelector.class);

  public ModuleSelector(String regex)
  {
    super(regex);
    // TODO Auto-generated constructor stub
  }

}
