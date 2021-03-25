package org.jactr.core.production.request;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Collections;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.slot.ISlot;
import org.slf4j.LoggerFactory;

public class ChunkRequest extends ChunkTypeRequest
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ChunkRequest.class);
  
  private IChunk _chunk;
  
  public ChunkRequest(IChunk chunk)
  {
    this(chunk, Collections.EMPTY_LIST);
    _chunk = chunk;
  }

  public ChunkRequest(IChunk chunk, Collection<? extends ISlot> slots)
  {
    super(chunk.getSymbolicChunk().getChunkType(), slots);
    _chunk = chunk;
  }
  
  @Override
  public ChunkRequest clone()
  {
    return new ChunkRequest(_chunk, _slots);
  }

  public IChunk getChunk()
  {
    return _chunk;
  }

  @Override
  public boolean matches(IChunk reference)
  {
    return _chunk.equalsSymbolic(reference) && super.matches(reference);
  }
}
