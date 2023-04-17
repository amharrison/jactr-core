package org.jactr.core.module.declarative.basic.type;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.chunktype.IChunkType;

public class NoOpChunkTypeConfigurator implements IChunkTypeConfigurator
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(NoOpChunkTypeConfigurator.class);

  public void configure(IChunkType chunk)
  {

  }

}
