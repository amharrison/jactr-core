package org.jactr.core.buffer.meta;

import org.jactr.core.buffer.IActivationBuffer;

/**
 * meta buffers are single element buffers that can contain things other than
 * chunks. The normal spreading activation behavior does not apply. Just because
 * it is bound in the production does not mean that the contents are encodable
 * in DM.
 * 
 * @author harrison
 */
public interface IMetaBuffer extends IActivationBuffer
{

  public Object getContents();

  public void setContents(Object object);

}
