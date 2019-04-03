package org.jactr.core.module.procedural.six;

/*
 * default logging
 */
import java.util.Comparator;

 
import org.slf4j.LoggerFactory;
import org.jactr.core.production.IProduction;

public class ProductionNameComparator implements Comparator<IProduction>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ProductionNameComparator.class);

  public int compare(IProduction o1, IProduction o2)
  {
    return o1.getSymbolicProduction().getName().compareTo(
        o2.getSymbolicProduction().getName());
  }

}
