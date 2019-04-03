package org.jactr.core.module.declarative.search.filter;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.chunk.IChunk;

/**
 * allows all through
 * @author harrison
 *
 */
public class AcceptAllFilter implements IChunkFilter
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(AcceptAllFilter.class);

  public boolean accept(IChunk chunk)
  {
    return true;
  }

}
