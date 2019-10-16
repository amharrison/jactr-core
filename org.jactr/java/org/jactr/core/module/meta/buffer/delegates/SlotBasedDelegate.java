package org.jactr.core.module.meta.buffer.delegates;

import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.delegate.IRequestDelegate;
import org.jactr.core.buffer.meta.IMetaBuffer;
import org.jactr.core.production.request.IRequest;
import org.jactr.core.production.request.SlotBasedRequest;
import org.jactr.core.slot.ISlot;

public class SlotBasedDelegate implements IRequestDelegate
{

  public SlotBasedDelegate()
  {

  }

  @Override
  public boolean request(IRequest request, IActivationBuffer buffer,
      double requestTime)
  {
    SlotBasedRequest ctr = (SlotBasedRequest) request;
    IMetaBuffer buff = (IMetaBuffer) buffer;

    SlotBasedRequest existing = (SlotBasedRequest) buff.getContents();
    /*
     * if there are no contents, set them to this request, otherwise append the
     * slots to the existing
     */
    if (existing == null)
      buff.setContents(ctr);
    else
      for (ISlot slot : ctr.getSlots())
        existing.addSlot(slot);

    return true;
  }

  @Override
  public boolean willAccept(IRequest request)
  {
    return request instanceof SlotBasedRequest;
  }

  @Override
  public void clear()
  {

  }

}
