package org.jactr.tools.grapher.core.selector;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.utils.parameter.IParameterized;

public class ClassNamedParameterSelector extends
    AbstractNameSelector<IParameterized>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ClassNamedParameterSelector.class);

  public ClassNamedParameterSelector(String regex)
  {
    super(regex);
  }

  @Override
  protected String getName(IParameterized element)
  {
    return element.getClass().getName();
  }

  public void add(ISelector selector)
  {

  }

}
