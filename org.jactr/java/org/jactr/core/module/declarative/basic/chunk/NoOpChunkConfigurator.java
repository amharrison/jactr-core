package org.jactr.core.module.declarative.basic.chunk;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.chunk.IChunk;

public class NoOpChunkConfigurator implements IChunkConfigurator
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(NoOpChunkConfigurator.class);

  public void configure(IChunk chunk)
  {

  }

}
