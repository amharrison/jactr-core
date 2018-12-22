/*
 * Created on Feb 25, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
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
package org.commonreality.net.message.request.time;

import java.io.Serializable;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.impl.BaseMessage;

/**
 * @author developer
 */
public class RequestTime extends BaseMessage implements IRequestTime,
    Serializable
{

  /**
   * 
   */
  private static final long serialVersionUID = -9021398413809868437L;
  private double _time;

  /**
   * @param source
   */
  public RequestTime(IIdentifier source, double whatTimeShouldItBe)
  {
    super(source);
    _time = whatTimeShouldItBe;
  }

  /**
   * @see org.commonreality.net.message.request.time.IRequestTime#getTime()
   */
  public double getTime()
  {
    return _time;
  }

  /**
   * @see org.commonreality.net.message.request.IRequest#acknowledgementRequired()
   */
  public boolean acknowledgementRequired()
  {
    return false;
  }

  @Override
  public String toString()
  {
    return "{" + getClass().getSimpleName() + ":" + getSource() + ":" +
        getTime() + "}";
  }

  public IMessage copy()
  {
    return new RequestTime(getSource(), getTime());
  }
}
