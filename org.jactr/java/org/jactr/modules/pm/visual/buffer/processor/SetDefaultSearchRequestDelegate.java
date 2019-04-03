package org.jactr.modules.pm.visual.buffer.processor;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.delegate.SimpleRequestDelegate;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.production.request.IRequest;
import org.jactr.modules.pm.visual.IVisualModule;
import org.jactr.modules.pm.visual.buffer.IVisualLocationBuffer;

/**
 * sets the default search pattern for the visual location buffer
 * 
 * @author harrison
 */
public class SetDefaultSearchRequestDelegate extends SimpleRequestDelegate
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(SetDefaultSearchRequestDelegate.class);

  public SetDefaultSearchRequestDelegate(IChunkType chunkType)
  {
    super(chunkType);
  }

  public boolean request(IRequest request, IActivationBuffer buffer,
      double requestTime)
  {
    IVisualLocationBuffer vlb = ((IVisualModule) buffer.getModule())
        .getVisualLocationBuffer();

    ChunkTypeRequest ctr = (ChunkTypeRequest) request;
    ChunkTypeRequest pattern = new ChunkTypeRequest(
        ((IVisualModule) vlb.getModule()).getVisualLocationChunkType(),
        ctr.getConditionalSlots());

    vlb.setDefaultSearch(pattern);

    return true;
  }

  public void clear()
  {

  }

}
