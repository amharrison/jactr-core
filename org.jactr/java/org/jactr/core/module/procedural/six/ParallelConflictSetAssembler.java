package org.jactr.core.module.procedural.six;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.function.Predicate;

import org.eclipse.collections.impl.factory.Sets;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.concurrent.ExecutorServices;
import org.jactr.core.production.IProduction;
import org.slf4j.LoggerFactory;

public class ParallelConflictSetAssembler extends DefaultConflictSetAssembler
{
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(ParallelConflictSetAssembler.class);

  public ParallelConflictSetAssembler()
  {
    super(false);
  }

  @Override
  protected Map<IProduction, Boolean> createMapForPredicate()
  {
    return new ConcurrentHashMap<>();
  }

  @Override
  public Set<IProduction> getConflictSet(Set<IProduction> container)
  {
    Map<String, Collection<IChunkType>> chunkTypesInBuffers = bufferContentMap();
    Predicate<IProduction> selector = getSelectionPredicate(
        chunkTypesInBuffers);

    ExecutorCompletionService<Set<IProduction>> ecs = new ExecutorCompletionService<Set<IProduction>>(
        ExecutorServices.getExecutor(ExecutorServices.POOL));
    int submitted = 0;
    for (IActivationBuffer buffer : getProceduralModule().getModel()
        .getActivationBuffers())
    {
      ecs.submit(() -> {
        return getConflictSetForBuffer(buffer, selector, Sets.mutable.empty());
      });
      submitted++;
    }

    ecs.submit(() -> {
      return getPossibleProductions(null, null, Sets.mutable.empty(), selector);
    });
    submitted++;

    for (int i = 0; i < submitted; i++)
      try
      {
        container.addAll(ecs.take().get());
      }
      catch (Exception e)
      {
        LOGGER.error("Failed to collect productions in parallel: ", e);
      }

    // collect the results

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("%d candidates", container.size()));

    return container;
  }

}
