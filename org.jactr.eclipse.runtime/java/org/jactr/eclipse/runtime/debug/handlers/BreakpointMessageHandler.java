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
import org.jactr.eclipse.runtime.debug.elements.ACTRStackFrame;
import org.jactr.eclipse.runtime.debug.elements.ACTRThread;
import org.jactr.eclipse.runtime.debug.util.Utilities;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.tools.async.message.event.data.BreakpointReachedEvent;

public class BreakpointMessageHandler extends
    org.jactr.tools.async.shadow.handlers.BreakpointMessageHandler
{

  ACTRDebugTarget _target;

  public BreakpointMessageHandler(ACTRDebugTarget target)
  {
    super();
    _target = target;
  }

  @Override
  public void accept(ISessionInfo session, BreakpointReachedEvent message)
  {
    super.accept(session, message);

    /*
     * the super will set the breakpoint data for the shadow controller but we
     * do need to notify
     */
    String modelName = message.getModelName();
    ACTRThread thread = _target.getThread(modelName);
    String breakpointName = ASTSupport.getName(message.getAST());

    int lineNumber = Utilities.extractLineNumber(_target.getLaunch(),
        modelName, breakpointName, JACTRBuilder.PRODUCTION);

    ACTRStackFrame frame = new ACTRStackFrame(thread, message.getSimulationTime(),
        breakpointName, lineNumber);

    /*
     * this will fire the event notifying the debugger
     */
    thread.addStackFrame(frame, true);
  }
}
