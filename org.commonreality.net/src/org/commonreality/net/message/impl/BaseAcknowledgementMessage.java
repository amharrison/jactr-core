/*
 * Created on Apr 15, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.net.message.impl;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IAcknowledgement;
import org.commonreality.net.message.IMessage;

/**
 * @author developer
 */
public class BaseAcknowledgementMessage extends BaseMessage implements
    IAcknowledgement
{

  /**
   * 
   */
  private static final long serialVersionUID = -1650032913350240488L;


  private long             _acknowledgingRequestId;

  /**
   * @param source
   */
  public BaseAcknowledgementMessage(IIdentifier source, long requestedId)
  {
    super(source);
    _acknowledgingRequestId = requestedId;
  }

  /**
   * @see org.commonreality.net.message.IAcknowledgement#getRequestMessageId()
   */
  public long getRequestMessageId()
  {
    return _acknowledgingRequestId;
  }

  /**
   * @see org.commonreality.net.message.IMessage#copy()
   */
  public IMessage copy()
  {
    return new BaseAcknowledgementMessage(getSource(), getRequestMessageId());
  }

}
