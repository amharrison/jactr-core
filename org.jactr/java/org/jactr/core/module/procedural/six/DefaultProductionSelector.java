package org.jactr.core.module.procedural.six;

/*
 * default logging
 */
import java.util.Collection;

 
import org.slf4j.LoggerFactory;
import org.jactr.core.module.procedural.IProceduralModule;
import org.jactr.core.module.procedural.IProductionSelector;
import org.jactr.core.production.IInstantiation;

/**
 * merely return the first one in the collection
 * 
 * @author harrison
 */
public class DefaultProductionSelector implements IProductionSelector
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(DefaultProductionSelector.class);

  private IProceduralModule          _module;

  public IInstantiation select(Collection<IInstantiation> instantiations)
  {
    if (instantiations.size() > 0) return instantiations.iterator().next();
    return null;
  }

  public void setProceduralModule(IProceduralModule module)
  {
    _module = module;
  }

  public IProceduralModule getProceduralModule()
  {
    return _module;
  }

}
