/*
 * Created on Feb 23, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
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

import java.io.Serializable;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IMessage;

/**
 * @author developer
 */
public abstract class BaseMessage implements IMessage, Serializable
{
  
  private static final long serialVersionUID = -1650032913350241488L;

  static transient private long LAST_ID;

  private long                  _messageId;

  private IIdentifier           _source;

  public BaseMessage(IIdentifier source)
  {
    _source = source;
    synchronized (BaseMessage.class)
    {
      _messageId = ++LAST_ID;
    }
  }

  /**
   * @see org.commonreality.net.message.IMessage#getMessageId()
   */
  public long getMessageId()
  {
    return _messageId;
  }

  /**
   * @see org.commonreality.net.message.IMessage#getSource()
   */
  public IIdentifier getSource()
  {
    return _source;
  }

  @Override
  public String toString()
  {
    return "{" + getClass().getSimpleName() + ":" + getSource() + "}";
  }

}
