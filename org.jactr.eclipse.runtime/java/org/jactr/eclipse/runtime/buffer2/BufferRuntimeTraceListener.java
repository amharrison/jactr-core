package org.jactr.eclipse.runtime.buffer2;

/*
 * default logging
 */
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
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
import org.jactr.tools.tracer.transformer.buffer.BulkBufferEvent;

public class BufferRuntimeTraceListener implements IRuntimeTraceListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(BufferRuntimeTraceListener.class);

  public boolean isInterestedIn(ITransformedEvent traceEvent,
      ISession session)
  {
    return traceEvent instanceof BulkBufferEvent;
  }

  public void eventFired(ITransformedEvent traceEvent, ISession session)
  {
    BulkBufferEvent ble = (BulkBufferEvent) traceEvent;

    /*
     * route to the session
     */
    ISessionData sessionData = session.getData(ble.getModelName());
    BufferSessionDataStream lsds = (BufferSessionDataStream) sessionData
        .getDataStream("buffer");
    if (lsds == null)
    {
      lsds = new BufferSessionDataStream(sessionData, RuntimePlugin
          .getDefault().getPreferenceStore()
          .getInt(RuntimePreferences.RUNTIME_DATA_WINDOW));
      ((LiveSessionData) sessionData).setStreamData("buffer", lsds);
    }

    BufferData ld = lsds.getLastData();
    boolean isNew = false;

    // append conflict resolution markers
    if (ld == null || ld.getTime() < ble.getSimulationTime())
    {
      /*
       * create a new one
       */
      if (ld != null)
        ld = new BufferData(ble.getSimulationTime(), ld);
      else
        ld = new BufferData(ble.getSimulationTime());

      isNew = true;
    }

    Map<String, CommonTree> buffers = ASTSupport.getMapOfTrees(ble.getAST(),
        JACTRBuilder.BUFFER);
    for (Map.Entry<String, CommonTree> buffer : buffers.entrySet())
      ld.setBufferContents(buffer.getKey(), buffer.getValue(),
          ble.isPostConflictResolution());

    if (isNew) lsds.append(ld);
  }

}
