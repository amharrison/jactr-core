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
package org.commonreality.net.message.command.control;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.impl.BaseAcknowledgementMessage;
import org.commonreality.participant.IParticipant;

/**
 * @author developer
 */
public class ControlAcknowledgement extends BaseAcknowledgementMessage
    implements IControlAcknowledgement
{


  /**
   * 
   */
  private static final long serialVersionUID = -3785635194352085659L;

  private Throwable        _thrown;

  private IParticipant.State _state;

  /**
   * @param source
   * @param requestedId
   */
  public ControlAcknowledgement(IIdentifier source, long requestedId,
      Throwable thrown)
  {
    super(source, requestedId);
    _thrown = thrown;
  }

  public ControlAcknowledgement(IIdentifier source, long requestedId,
      IParticipant.State state)
  {
    this(source, requestedId, (Throwable) null);
    _state = state;
  }

  @Override
  public IMessage copy()
  {
    if (_thrown != null)
      return new ControlAcknowledgement(getSource(), getRequestMessageId(),
          getException());
    return new ControlAcknowledgement(getSource(), getRequestMessageId(),
        getState());
  }

  /**
   * @see org.commonreality.net.message.command.control.IControlAcknowledgement#getException()
   */
  public Throwable getException()
  {
    return _thrown;
  }

  /**
   * @see org.commonreality.net.message.command.control.IControlAcknowledgement#getState()
   */
  public IParticipant.State getState()
  {
    return _state;
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append(":").append(getState());
    return sb.toString();
  }

}
