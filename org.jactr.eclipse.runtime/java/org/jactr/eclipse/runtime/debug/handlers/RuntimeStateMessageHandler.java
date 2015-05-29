package org.jactr.eclipse.runtime.debug.handlers;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.session.ISessionInfo;
import org.eclipse.debug.core.DebugEvent;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.tools.async.message.event.state.RuntimeStateEvent;

public class RuntimeStateMessageHandler extends
    org.jactr.tools.async.shadow.handlers.RuntimeStateMessageHandler
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RuntimeStateMessageHandler.class);

  // @Override
  // public void handleMessage(IoSession session, IRuntimeStateEvent message)
  // throws Exception
  // {
  // super.handleMessage(session, message);
  //
  // switch (message.getState())
  // {
  // case STARTED:
  // break;
  // }
  // }

  @Override
  public void accept(ISessionInfo session, RuntimeStateEvent message)
  {
    super.accept(session, message);

    /**
     * other than start, the actual state of the runtime is determined by all of
     * the ModelState messages that we get.
     */
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
    // case STOPPED : controller.stopped(); break;
    // case SUSPENDED : controller.suspended(); break;
    // case RESUMED : controller.resumed(); break;
    }
  }
}
