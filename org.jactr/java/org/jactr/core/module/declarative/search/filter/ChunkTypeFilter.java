package org.jactr.core.module.declarative.search.filter;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class ChunkTypeFilter implements IChunkFilter
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ChunkTypeFilter.class);

  private final IChunkType           _type;

  private final boolean              _isAStrict;

  public ChunkTypeFilter(IChunkType type, boolean isAStrict)
  {
    _type = type;
    _isAStrict = isAStrict;
  }

  @Override
  public boolean accept(IChunk chunk)
  {
    try
    {
      if (_type == null) return true;

      if (_isAStrict)
        return chunk.getSymbolicChunk().isAStrict(_type);
      else
        return chunk.getSymbolicChunk().isA(_type);
    }
    catch (Exception e)
    {
      LOGGER.error(e.getMessage(), e);

      return false;
    }
  }

}
