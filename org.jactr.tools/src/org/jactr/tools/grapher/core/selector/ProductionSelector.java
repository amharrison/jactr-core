package org.jactr.tools.grapher.core.selector;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.production.IProduction;

public class ProductionSelector extends AbstractNameSelector<IProduction>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ProductionSelector.class);
  
  
  public ProductionSelector(String regex)
  {
    super(regex);
  }

  @Override
  protected String getName(IProduction element)
  {
    return element.getSymbolicProduction().getName();
  }


  public void add(ISelector selector)
  {

  }

}
