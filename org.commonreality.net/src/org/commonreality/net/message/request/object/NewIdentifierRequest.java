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
import java.util.List;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.impl.BaseMessage;

/**
 * @author developer
 */
public class NewIdentifierRequest extends BaseMessage implements
    INewIdentifierRequest
{
  /**
   * 
   */
  private static final long serialVersionUID = -8070860415778236981L;


  private List<IIdentifier> _requestedIdentifiers = new ArrayList<IIdentifier>(
                                                      10);

  public NewIdentifierRequest(IIdentifier source,
      Collection< ? extends IIdentifier> names)
  {
    super(source);
    _requestedIdentifiers = new ArrayList<IIdentifier>(names);
  }

  /**
   * @see org.commonreality.net.message.request.object.INewIdentifierRequest#getRequestedNames()
   */
  public Collection<IIdentifier> getIdentifiers()
  {
    return Collections.unmodifiableCollection(_requestedIdentifiers);
  }

  /**
   * @see org.commonreality.net.message.request.IRequest#acknowledgementRequired()
   */
  public boolean acknowledgementRequired()
  {
    return true;
  }

  public IMessage copy()
  {
    return new NewIdentifierRequest(getSource(), getIdentifiers());
  }
}
