package org.jactr.eclipse.runtime.debug.handlers;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.session.IoSession;
import org.eclipse.debug.core.DebugEvent;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.tools.async.message.event.state.IRuntimeStateEvent;

public class RuntimeStateMessageHandler extends
    org.jactr.tools.async.shadow.handlers.RuntimeStateMessageHandler
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RuntimeStateMessageHandler.class);

  @Override
  public void handleMessage(IoSession session, IRuntimeStateEvent message)
      throws Exception
  {
    super.handleMessage(session, message);

    switch (message.getState())
    {
      case STARTED:
        ACTRSession actrSession = (ACTRSession) session
            .getAttribute("actrSession");
        /*
         * lets fire a BS break point message
         */
        if (actrSession != null && actrSession.isDebugSession())
          actrSession.getACTRDebugTarget().fireSuspendEvent(
              DebugEvent.BREAKPOINT);
        break;
    }
  }
}
