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
package org.commonreality.net.message.request.connect;

import java.io.Serializable;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.identifier.impl.BasicIdentifier;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.credentials.ICredentials;
import org.commonreality.net.message.impl.BaseMessage;
import org.commonreality.participant.addressing.IAddressingInformation;

/**
 * @author developer
 */
public class ConnectionRequest extends BaseMessage implements
    IConnectionRequest, Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = -4913297099999481574L;



  private ICredentials           _credentials;

  private IAddressingInformation _addressInfo;

  public ConnectionRequest(String requestedName, IIdentifier.Type type,
      ICredentials credentials, IAddressingInformation address)
  {
    /*
     * we don't know our identifier yet
     */
    super(new BasicIdentifier(requestedName, type, null));
    _credentials = credentials;
    _addressInfo = address;
  }

  public IMessage copy()
  {
    return new ConnectionRequest(getSource().getName(), getSource().getType(),
        getCredentials(), getAddressingInformation());
  }

  /**
   * @see org.commonreality.net.message.request.IRequest#acknowledgementRequired()
   */
  public boolean acknowledgementRequired()
  {
    return true;
  }

  public ICredentials getCredentials()
  {
    return _credentials;
  }

  public IAddressingInformation getAddressingInformation()
  {
    return _addressInfo;
  }
}
