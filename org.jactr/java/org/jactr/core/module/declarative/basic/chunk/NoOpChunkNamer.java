package org.jactr.core.module.declarative.basic.chunk;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.chunk.IChunk;

public class NoOpChunkNamer implements IChunkNamer
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(NoOpChunkNamer.class);

  public String generateName(IChunk chunk)
  {
    return chunk.getSymbolicChunk().getName();
  }

}
