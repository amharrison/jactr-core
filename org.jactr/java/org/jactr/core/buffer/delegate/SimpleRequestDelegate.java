package org.jactr.core.buffer.delegate;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.production.request.IRequest;

/**
 * a simple request delegate that just fires and forgets. <br/>
 * <br/>
 * This is often used when simply wrapping an API call.
 * 
 * @author harrison
 */
public abstract class SimpleRequestDelegate implements IRequestDelegate
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(SimpleRequestDelegate.class);

  final private IChunkType           _chunkType;

  public SimpleRequestDelegate(IChunkType chunkType)
  {
    _chunkType = chunkType;
  }

  protected IChunkType getChunkType()
  {
    return _chunkType;
  }

  public boolean willAccept(IRequest request)
  {
    if (request instanceof ChunkTypeRequest)
    {
      IChunkType rCT = ((ChunkTypeRequest) request).getChunkType();
      return rCT != null && rCT.isA(_chunkType);
    }
    return false;
  }

}
