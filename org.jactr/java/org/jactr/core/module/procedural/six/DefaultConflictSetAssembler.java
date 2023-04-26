package org.jactr.core.module.procedural.six;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.module.procedural.IConflictSetAssembler;
import org.jactr.core.module.procedural.IProceduralModule;
import org.jactr.core.production.IProduction;
import org.jactr.core.production.condition.ChunkTypeCondition;
import org.jactr.core.production.condition.ICondition;
import org.slf4j.LoggerFactory;

/**
 * monitors the procedural module for new productions. All productions are
 * sorted by the most specific characteristic used for conflict set assembly.
 * Typically this is the chunktype of one of the conditions. If there are no
 * chunktype matches, it drops down to the buffer (for queries), and finally if
 * that doesn't work, the production is always considered for matching. <br/>
 * <br/>
 * full indexing (of all conditions) is not strictly necessary since all
 * conditions must match for firing, but performing full indexing can be useful
 * if subsets of productions can change.
 * 
 * @author harrison
 */
public class DefaultConflictSetAssembler implements IConflictSetAssembler
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(DefaultConflictSetAssembler.class);

  private IProceduralModule                       _module;

  public DefaultConflictSetAssembler(boolean useFullIndexing)
  {
  }

  public void setProceduralModule(IProceduralModule module)
  {
    _module = module;
  }

  public IProceduralModule getProceduralModule()
  {
    return _module;
  }

  protected Map<String, Collection<IChunkType>> bufferContentMap()
  {
    Map<String, Collection<IChunkType>> rtn = Maps.mutable.empty();
    Collection<IChunk> source = new ArrayList<>(2);

    for (IActivationBuffer buffer : getProceduralModule().getModel()
        .getActivationBuffers())
    {
      source.clear();
      buffer.getSourceChunks(source);
      source.forEach((c) -> {
        rtn.computeIfAbsent(buffer.getName(), (key) -> {
          return new ArrayList<>();
        }).add(c.getSymbolicChunk().getChunkType());
      });
    }

    return rtn;
  }

  protected Map<IProduction, Boolean> createMapForPredicate()
  {
    return Maps.mutable.empty();
  }

  protected Predicate<IProduction> getSelectionPredicate(
      Map<String, Collection<IChunkType>> bufferContents)
  {
    final Map<IProduction, Boolean> alreadyTested = createMapForPredicate();

    return (prod) -> {

      return alreadyTested.computeIfAbsent(prod, p -> {
        for (ICondition condition : p.getSymbolicProduction().getConditions())
          if (condition instanceof ChunkTypeCondition)
          {
            ChunkTypeCondition ctc = (ChunkTypeCondition) condition;
            Collection<IChunkType> currentTypes = bufferContents
                .getOrDefault(ctc.getBufferName(), Collections.emptyList());
            boolean matches = currentTypes.stream().anyMatch(ct -> {
              return ct.isA(ctc.getChunkType());
            });

            if (!matches) return Boolean.FALSE;
          }
        return Boolean.TRUE;
      });
    };

  }

  protected Set<IProduction> getConflictSetForBuffer(IActivationBuffer buffer,
      Predicate<IProduction> selector, Set<IProduction> container)
  {
    Set<IProduction> candidates = Sets.mutable.empty();
    String bufferName = buffer.getName().toLowerCase();

    /*
     * first, the buffer ambiguous
     */
    getPossibleProductions(bufferName, null, candidates, selector);

    if (candidates.size() != 0)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("%s yielded %s", buffer, candidates));
      container.addAll(candidates);
    }
    else if (LOGGER.isDebugEnabled()) LOGGER.debug(String
        .format("%s buffer yielded no ambiguous buffer productions", buffer));

    // get the source contents
    for (IChunk chunk : buffer.getSourceChunks())
    {
      candidates.clear();

      IChunkType chunkType = chunk.getSymbolicChunk().getChunkType();
      getPossibleProductions(buffer.getName(), chunkType, candidates, selector);

      if (candidates.size() != 0)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Chunktype : %s in %s yielded %s",
              chunkType, buffer, candidates));

        container.addAll(candidates);
      }
      else if (LOGGER.isDebugEnabled()) LOGGER.debug(
          String.format("%s in %s buffer yielded no candidate productions",
              chunk, buffer.getName()));
    }
    return container;
  }

  public Set<IProduction> getConflictSet(Set<IProduction> container)
  {
    Map<String, Collection<IChunkType>> chunkTypesInBuffers = bufferContentMap();
    Predicate<IProduction> selector = getSelectionPredicate(
        chunkTypesInBuffers);
    for (IActivationBuffer buffer : getProceduralModule().getModel()
        .getActivationBuffers())
    {
      Set<IProduction> candidates = getConflictSetForBuffer(buffer, selector,
          Sets.mutable.empty());
      container.addAll(candidates);
    }

    // and the completely ambiguous set
    container.addAll(
        getPossibleProductions(null, null, Sets.mutable.empty(), selector));

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("%d candidates", container.size()));

    return container;

  }

  protected Set<IProduction> getPossibleProductions(String bufferName,
      IChunkType chunkType, Set<IProduction> container,
      Predicate<IProduction> selector)
  {
    return getProceduralModule().getProductionStorage()
        .getProductions(bufferName, chunkType, container, selector);
  }

  protected Set<IProduction> getPossibleProductions(String bufferName,
      Set<IProduction> container, Predicate<IProduction> selector)
  {
    return getPossibleProductions(bufferName, null, container, selector);
  }

  protected Set<IProduction> getAmbiguousProductions(Set<IProduction> container,
      Predicate<IProduction> selector)
  {
    return getPossibleProductions(null, null, container, selector);
  }
}
