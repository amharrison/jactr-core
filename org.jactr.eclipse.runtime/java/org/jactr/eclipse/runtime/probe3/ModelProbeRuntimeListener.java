package org.jactr.eclipse.runtime.probe3;

/*
 * default logging
 */
import java.util.Collections;
import java.util.WeakHashMap;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.eclipse.runtime.probe2.StringTable;
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
  static private final transient Log         LOGGER        = LogFactory
                                                               .getLog(ModelProbeRuntimeListener.class);

  private WeakHashMap<ISession, StringTable> _stringTables = new WeakHashMap<ISession, StringTable>();

  private Function<String, IProbeData>       _probeDataProvider;

  public void setProbeProvider(Function<String, IProbeData> provider)
  {
    _probeDataProvider = provider;
  }

  public Function<String, IProbeData> getProbeProvider()
  {
    return _probeDataProvider;
  }

  public boolean isInterestedIn(ITransformedEvent traceEvent, ISession session)
  {
    return traceEvent instanceof StringTableMessage
        || traceEvent instanceof ProbeContainerUpdate;
  }

  public void eventFired(ITransformedEvent traceEvent, ISession session)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Got probe data [%s]", traceEvent));

    if (getProbeProvider() == null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("No provider found, ignoring probe data"));
      return; // we cant store the data.
    }

    /*
     * ensure we have the data stream
     */

    StringTable st = _stringTables.get(session);
    if (st == null)
    {
      st = new StringTable();
      _stringTables.put(session, st);
    }

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
    if (mpd == null && getProbeProvider() != null)
    {
      mpd = new ModelProbeData2(traceEvent.getModelName(), RuntimePlugin
          .getDefault().getPreferenceStore()
          .getInt(RuntimePreferences.PROBE_RUNTIME_DATA_WINDOW), st,
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
