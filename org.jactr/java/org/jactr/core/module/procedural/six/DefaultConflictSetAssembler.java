package org.jactr.core.module.procedural.six;

import java.util.Set;

import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.module.procedural.IConflictSetAssembler;
import org.jactr.core.module.procedural.IProceduralModule;
import org.jactr.core.production.IProduction;
import org.jactr.core.utils.collections.FastSetFactory;
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
  static private final transient org.slf4j.Logger        LOGGER                       = LoggerFactory
                                                                                          .getLogger(DefaultConflictSetAssembler.class);

  private IProceduralModule                              _module;



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

  public Set<IProduction> getConflictSet(Set<IProduction> container)
  {
      Set<IProduction> candidates = FastSetFactory.newInstance();
      for (IActivationBuffer buffer : getProceduralModule().getModel()
          .getActivationBuffers())
      {
        candidates.clear();

        String bufferName = buffer.getName().toLowerCase();

        /*
         * first, the buffer ambiguous
         */
        getPossibleProductions(bufferName, null, candidates);

        if (candidates.size() != 0)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("%s yielded %s", buffer, candidates));
          container.addAll(candidates);
        }
        else if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format(
              "%s buffer yielded no ambiguous buffer productions", buffer));

        // get the source contents
        for (IChunk chunk : buffer.getSourceChunks())
        {
          candidates.clear();

          IChunkType chunkType = chunk.getSymbolicChunk().getChunkType();
          getPossibleProductions(buffer.getName(), chunkType, candidates);

          if (candidates.size() != 0)
          {
            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format("Chunktype : %s in %s yielded %s",
                  chunkType, buffer, candidates));

            container.addAll(candidates);
          }
          else if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format(
                "%s in %s buffer yielded no candidate productions", chunk,
                buffer.getName()));
        }

      }

      // and the completely ambiguous set
      candidates.clear();
      getPossibleProductions(null, null, candidates);

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Ambiguous productions %s", candidates));

      container.addAll(candidates);

      FastSetFactory.recycle(candidates);

      return container;

  }

  public Set<IProduction> getPossibleProductions(String bufferName,
      IChunkType chunkType, Set<IProduction> container)
  {
    return getProceduralModule().getProductionStorage()
        .getProductions(bufferName, chunkType, container);
  }

  public Set<IProduction> getPossibleProductions(String bufferName,
      Set<IProduction> container)
  {
    return getPossibleProductions(bufferName, null, container);
  }

  public Set<IProduction> getAmbiguousProductions(Set<IProduction> container)
  {
    return getPossibleProductions(null, null, container);
  }
}
