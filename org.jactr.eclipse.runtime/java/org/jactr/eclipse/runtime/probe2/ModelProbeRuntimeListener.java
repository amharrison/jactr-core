package org.jactr.eclipse.runtime.probe2;

/*
 * default logging
 */
import java.util.Collections;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.data.LiveSessionData;
import org.jactr.eclipse.runtime.trace.IRuntimeTraceListener;
import org.jactr.tools.grapher.core.message.ProbeContainerUpdate;
import org.jactr.tools.grapher.core.message.StringTableMessage;
import org.jactr.tools.tracer.transformer.ITransformedEvent;

public class ModelProbeRuntimeListener implements IRuntimeTraceListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ModelProbeRuntimeListener.class);

  private WeakHashMap<ISession, StringTable> _stringTables = new WeakHashMap<ISession, StringTable>();

  public boolean isInterestedIn(ITransformedEvent traceEvent,
      ISession session)
  {
    return traceEvent instanceof StringTableMessage
        || traceEvent instanceof ProbeContainerUpdate;
  }

  public void eventFired(ITransformedEvent traceEvent, ISession session)
  {
    /*
     * ensure we have the data stream
     */

    StringTable st = _stringTables.get(session);
    if (st == null)
    {
      st = new StringTable();
      _stringTables.put(session, st);
    }

    ISessionData sessionData = session.getData(
        traceEvent.getModelName());

    IModelProbeSessionDataStream lsds = (IModelProbeSessionDataStream) sessionData
        .getDataStream("probe");

    if (lsds == null)
    {
      lsds = new ModelProbeSessionDataStream(sessionData, RuntimePlugin
          .getDefault().getPreferenceStore()
          .getInt(RuntimePreferences.PROBE_RUNTIME_DATA_WINDOW));

      ((LiveSessionData) sessionData).setStreamData("probe", lsds);
    }

    ModelProbeData mpd = lsds.getRoot();
    if (mpd == null)
    {
      mpd = new ModelProbeData(traceEvent.getModelName(),RuntimePlugin
          .getDefault().getPreferenceStore()
          .getInt(RuntimePreferences.PROBE_RUNTIME_DATA_WINDOW), st);
      
      ((ModelProbeSessionDataStream) lsds).append(mpd);
    }

    mpd.process(traceEvent);

    if (traceEvent instanceof ProbeContainerUpdate)
      ((ModelProbeSessionDataStream) lsds).fireChange(Collections
          .singleton(mpd));
  }

}
