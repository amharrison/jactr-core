package org.jactr.core.module.meta.buffer.delegates;

import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.delegate.IRequestDelegate;
import org.jactr.core.buffer.meta.IMetaBuffer;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.production.request.IRequest;

public class AddChunkTypeDelegate implements IRequestDelegate
{

  public AddChunkTypeDelegate()
  {

  }

  @Override
  public boolean request(IRequest request, IActivationBuffer buffer,
      double requestTime)
  {
    ChunkTypeRequest ctr = (ChunkTypeRequest) request;
    IMetaBuffer buff = (IMetaBuffer) buffer;

    buff.setContents(ctr);

    return true;
  }

  @Override
  public boolean willAccept(IRequest request)
  {
    return request instanceof ChunkTypeRequest;
  }

  @Override
  public void clear()
  {

  }

}
