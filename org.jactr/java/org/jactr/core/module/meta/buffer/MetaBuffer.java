package org.jactr.core.module.meta.buffer;

import java.util.Collection;

import org.jactr.core.buffer.delegate.DefaultDelegatedRequestableBuffer6;
import org.jactr.core.buffer.meta.IMetaBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.module.meta.MetaModule;
import org.jactr.core.module.meta.buffer.delegates.AddChunkTypeDelegate;
import org.jactr.core.module.meta.buffer.delegates.SlotBasedDelegate;
import org.jactr.core.production.request.IRequest;

public class MetaBuffer extends DefaultDelegatedRequestableBuffer6
    implements IMetaBuffer
{
  
  private IRequest _request;

  public MetaBuffer(MetaModule module)
  {
    super("meta", module);

    addRequestDelegate(new AddChunkTypeDelegate());
    addRequestDelegate(new SlotBasedDelegate());
  }

  @Override
  public Object getContents()
  {
    return _request;
  }

  @Override
  public void setContents(Object object)
  {
    if(object==null)
      _request = null;
    if(object instanceof IRequest)
      _request = (IRequest) object;
  }

  @Override
  protected Collection<IChunk> clearInternal()
  {
    _request = null;
    return super.clearInternal();
  }
}
