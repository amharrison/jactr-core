package org.jactr.core.module.retrieval;

import org.jactr.core.chunk.IChunk;

/**
 * result for a retrieval
 * 
 * @author harrison
 */
public class SearchResult
{
  final private IChunk _chunk;

  final private double _retrievalTime;

  public SearchResult(IChunk chunk, double retrievalTime)
  {
    _chunk = chunk;
    _retrievalTime = retrievalTime;
  }

  public double getRetrievalTime()
  {
    return _retrievalTime;
  }

  public IChunk getChunk()
  {
    return _chunk;
  }
}
