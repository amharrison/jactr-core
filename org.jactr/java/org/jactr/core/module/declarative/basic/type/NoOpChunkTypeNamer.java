
package org.jactr.core.module.declarative.basic.type;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.chunktype.IChunkType;

public class NoOpChunkTypeNamer implements IChunkTypeNamer
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(NoOpChunkTypeNamer.class);

  public String generateName(IChunkType chunk)
  {
    return chunk.getSymbolicChunkType().getName();
  }

}
