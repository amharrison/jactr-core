/*
 * Created on May 12, 2007 Copyright (C) 2001-2007, Anthony Harrison
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
import org.commonreality.net.message.impl.BaseAcknowledgementMessage;

/**
 * @author developer
 */
public class NewIdentifierAcknowledgement extends BaseAcknowledgementMessage
    implements INewIdentifierAcknowledgement
{
  /**
   * 
   */
  private static final long serialVersionUID = -923750298169160900L;


  private Collection<IIdentifier> _identifiers;

  /**
   * @param source
   */
  public NewIdentifierAcknowledgement(IIdentifier source, long requestId,
      Collection< ? extends IIdentifier> identifiers)
  {
    super(source, requestId);
    _identifiers = Collections
        .unmodifiableCollection(new ArrayList<IIdentifier>(identifiers));
  }

  /**
   * @see org.commonreality.net.message.request.object.INewIdentifierAcknowledgement#getIdentifiers()
   */
  public Collection<IIdentifier> getIdentifiers()
  {
    return _identifiers;
  }

  @Override
  public IMessage copy()
  {
    return new NewIdentifierAcknowledgement(getSource(), getRequestMessageId(),
        getIdentifiers());
  }
}
