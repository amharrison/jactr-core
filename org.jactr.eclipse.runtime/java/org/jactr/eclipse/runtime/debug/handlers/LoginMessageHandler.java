/*
 * Created on Mar 12, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.runtime.debug.handlers;

import org.commonreality.net.session.ISessionInfo;
import org.jactr.eclipse.runtime.debug.ACTRDebugTarget;
import org.jactr.tools.async.message.event.login.LoginAcknowledgedMessage;

public class LoginMessageHandler extends
    org.jactr.tools.async.shadow.handlers.LoginMessageHandler
{

  ACTRDebugTarget                    _target;

  public LoginMessageHandler(ACTRDebugTarget target)
  {
    super();
    _target = target;
  }

  @Override
  public void accept(ISessionInfo session, LoginAcknowledgedMessage message)
  {
    /*
     * we used to do something here..
     */
    super.accept(session, message);
    session.setAttribute("actrSession", _target.getACTRSession());
  }
}
