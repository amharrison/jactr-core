package org.jactr.eclipse.runtime.probe3.extract;

/*
 * default logging
 */
import java.util.Collections;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFolder;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.eclipse.runtime.probe2.StringTable;
import org.jactr.eclipse.runtime.probe3.IProbeData;
import org.jactr.eclipse.runtime.probe3.ModelProbeData2;
import org.jactr.eclipse.runtime.probe3.ModelProbeDataStream;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.data.LiveSessionData;
import org.jactr.eclipse.runtime.trace.IRuntimeTraceListener;
import org.jactr.tools.grapher.core.message.ProbeContainerUpdate;
import org.jactr.tools.grapher.core.message.StringTableMessage;
import org.jactr.tools.tracer.transformer.ITransformedEvent;

public class SessionSpecificModelProbeRuntimeListener implements
    IRuntimeTraceListener
{
  /**
   * Logger definition
   */
  static private final transient Log   LOGGER       = LogFactory
                                                        .getLog(SessionSpecificModelProbeRuntimeListener.class);

  private final ISession               _session;

  private StringTable                  _stringTable = new StringTable();

  private final Function<String, IProbeData> _probeDataProvider;

  public SessionSpecificModelProbeRuntimeListener(ISession session,
      IFolder output)
  {
    _session = session;
    _probeDataProvider = new OutputProbeDataProvider(output);
  }


  public boolean isInterestedIn(ITransformedEvent traceEvent, ISession session)
  {
    return _session == session
        && (traceEvent instanceof StringTableMessage || traceEvent instanceof ProbeContainerUpdate);
  }

  public void eventFired(ITransformedEvent traceEvent, ISession session)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Got probe data [%s]", traceEvent));

    ISessionData sessionData = session.getData(traceEvent.getModelName());

    ModelProbeDataStream lsds = (ModelProbeDataStream) sessionData
        .getDataStream("probe");

    if (lsds == null)
    {
      lsds = new ModelProbeDataStream(sessionData);

      ((LiveSessionData) sessionData).setStreamData("probe", lsds);

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Set stream data"));
    }

    ModelProbeData2 mpd = lsds.getRoot();
    if (mpd == null)
    {
      mpd = new ModelProbeData2(traceEvent.getModelName(), RuntimePlugin
          .getDefault().getPreferenceStore()
          .getInt(RuntimePreferences.PROBE_RUNTIME_DATA_WINDOW), _stringTable,
          _probeDataProvider);

      lsds.setRoot(mpd);

      if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Set root"));
    }

    mpd.process(traceEvent);

    // add/modify events need to be fired

    if (traceEvent instanceof ProbeContainerUpdate)
      lsds.fireChange(Collections.singleton(mpd));
  }

}
