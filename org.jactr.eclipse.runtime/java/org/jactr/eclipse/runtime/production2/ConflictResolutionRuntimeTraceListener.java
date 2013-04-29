package org.jactr.eclipse.runtime.production2;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.data.LiveSessionData;
import org.jactr.eclipse.runtime.trace.IRuntimeTraceListener;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.tools.tracer.transformer.ITransformedEvent;
import org.jactr.tools.tracer.transformer.procedural.TransformedProceduralEvent;

public class ConflictResolutionRuntimeTraceListener implements IRuntimeTraceListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ConflictResolutionRuntimeTraceListener.class);

  public boolean isInterestedIn(ITransformedEvent traceEvent,
      ISession session)
  {
    return traceEvent instanceof TransformedProceduralEvent;
  }

  public void eventFired(ITransformedEvent traceEvent, ISession session)
  {
    TransformedProceduralEvent ble = (TransformedProceduralEvent) traceEvent;

    /*
     * route to the session
     */
    ISessionData sessionData = session.getData(ble.getModelName());
    IConflictResolutionDataStream lsds = (IConflictResolutionDataStream) sessionData
        .getDataStream("conflict");
    if (lsds == null)
    {
      lsds = new ConflictResolutionSessionDataStream(sessionData, RuntimePlugin
          .getDefault().getPreferenceStore()
          .getInt(RuntimePreferences.RUNTIME_DATA_WINDOW));
      ((LiveSessionData) sessionData).setStreamData("conflict", lsds);
    }

    ConflictResolutionData data = new ConflictResolutionData(
        ble.getSimulationTime(), ASTSupport.getAllDescendantsWithType(
            ble.getAST(), JACTRBuilder.PRODUCTION));

    ((ConflictResolutionSessionDataStream) lsds).append(data);
  }

}
