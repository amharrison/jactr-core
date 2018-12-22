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
import org.commonreality.net.message.impl.BaseMessage;

/**
 * @author developer
 */
public class ControlCommand extends BaseMessage implements IControlCommand
{


  /**
   * 
   */
  private static final long serialVersionUID = 143820624651406189L;

  private State            _state;

  private Object           _extraData;

  /**
   * @param source
   */
  public ControlCommand(IIdentifier source, State state, Object extra)
  {
    super(source);
    _state = state;
    _extraData = extra;
  }

  public ControlCommand(IIdentifier source, State state)
  {
    this(source, state, null);
  }

  public IMessage copy()
  {
    return new ControlCommand(getSource(), getState(), getData());
  }

  public State getState()
  {
    return _state;
  }

  /**
   * @see org.commonreality.net.message.command.control.IControlCommand#getData()
   */
  public Object getData()
  {
    return _extraData;
  }

  /**
   * common reality requires confirmation that this has been handled
   * 
   * @see org.commonreality.net.message.request.IRequest#acknowledgementRequired()
   */
  public boolean acknowledgementRequired()
  {
    return true;
  }
}
