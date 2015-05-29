/*
 * Created on Mar 15, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
import org.jactr.eclipse.runtime.debug.elements.ACTRStackFrame;
import org.jactr.eclipse.runtime.debug.elements.ACTRThread;
import org.jactr.tools.async.message.event.state.ModelStateEvent;
import org.jactr.tools.async.shadow.handlers.ModelStateHandler;

public class ModelStateMessageHandler extends ModelStateHandler
{

  ACTRDebugTarget _target;

  public ModelStateMessageHandler(ACTRDebugTarget target)
  {
    _target = target;
  }

  @Override
  public void accept(ISessionInfo session, ModelStateEvent message)
  {
    /*
     * here is where we deal with the model theads
     */
    ACTRThread thread = null;
    switch (message.getState())
    {
      case STARTED:
        thread = _target.addThread(message.getModelName());
        ACTRStackFrame startStack = new ACTRStackFrame(thread, message
            .getSimulationTime(), "<started>", 0);
        thread.addStackFrame(startStack, false);
        break;
      case STOPPED:
        thread = _target.getThread(message.getModelName());
        ACTRStackFrame endStack = new ACTRStackFrame(thread, message
            .getSimulationTime(), "<terminated>", 0);
        thread.addStackFrame(endStack, false);
        thread.setTerminated(true);
        break;
      case SUSPENDED:
        _target.getThread(message.getModelName()).setSuspended(true, "");
        break;
      case RESUMED:
        _target.getThread(message.getModelName()).setSuspended(false, "");
        break;
    }
    
    super.accept(session, message);
  }

}
