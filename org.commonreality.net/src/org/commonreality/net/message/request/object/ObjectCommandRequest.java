/*
 * Created on May 11, 2007 Copyright (C) 2001-2007, Anthony Harrison
 * anh23@pitt.edu (jactr.org) This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version. This library is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.net.message.request.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.command.object.IObjectCommand.Type;
import org.commonreality.net.message.impl.BaseMessage;

/**
 * @author developer
 */
public class ObjectCommandRequest extends BaseMessage implements
    IObjectCommandRequest
{
  /**
   * 
   */
  private static final long serialVersionUID = 8072903007883244439L;


  private IIdentifier             _destination;

  private Type                    _type;

  private Collection<IIdentifier> _identifiers;

  /**
   * @param source
   */
  public ObjectCommandRequest(IIdentifier source, IIdentifier destination, Type type,
      Collection<IIdentifier> identifiers)
  {
    super(source);
    _destination = destination;
    _type = type;
    _identifiers = new ArrayList<IIdentifier>(identifiers);
  }

  public IMessage copy()
  {
    return new ObjectCommandRequest(getSource(), getDestination(), getType(),
        getIdentifiers());
  }

  /**
   * @see org.commonreality.net.message.request.object.IObjectCommandRequest#getDestination()
   */
  public IIdentifier getDestination()
  {
    return _destination;
  }

  /**
   * @see org.commonreality.net.message.request.object.IObjectCommandRequest#getIdentifiers()
   */
  public Collection<IIdentifier> getIdentifiers()
  {
    return Collections.unmodifiableCollection(_identifiers);
  }

  /**
   * @see org.commonreality.net.message.request.object.IObjectCommandRequest#getType()
   */
  public Type getType()
  {
    return _type;
  }

  /**
   * @see org.commonreality.net.message.request.IRequest#acknowledgementRequired()
   */
  public boolean acknowledgementRequired()
  {
    return true;
  }

}
