package org.jactr.core.module.procedural.storage;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.module.procedural.IProceduralModule;
import org.jactr.core.production.IProduction;

public interface IProductionStorage
{

  void setProceduralModule(IProceduralModule module);

  IProceduralModule getProceduralModule();

  IProduction add(IProduction productionToAdd);

  void remove(IProduction productionToRemove);

  IProduction getProduction(String productionName);

  /**
   * return all productions currently stored
   * 
   * @param container
   * @return
   */
  Collection<IProduction> getProductions(Collection<IProduction> container);

  Set<IProduction> getProductions(String bufferName, IChunkType chunkType,
      Set<IProduction> container, Predicate<IProduction> selector);

}