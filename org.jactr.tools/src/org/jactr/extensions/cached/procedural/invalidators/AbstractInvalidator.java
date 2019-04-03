package org.jactr.extensions.cached.procedural.invalidators;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.production.IProduction;
import org.jactr.extensions.cached.procedural.internal.InstantiationCache;

public abstract class AbstractInvalidator implements IInvalidator
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(AbstractInvalidator.class);

  
  private final InstantiationCache _cache;
  private final IProduction _production;
  
  public AbstractInvalidator(InstantiationCache cache, IProduction production)
  {
    _cache = cache;
    _production = production;
  }
  
  public void invalidate()
  {
    _cache.remove(_production);
  }

}
