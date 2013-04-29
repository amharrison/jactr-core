package org.jactr.eclipse.runtime.marker;

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
import org.jactr.tools.marker.tracer.MarkerTransformedEvent;
import org.jactr.tools.tracer.transformer.ITransformedEvent;

public class MarkerRuntimeTraceListener implements IRuntimeTraceListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(MarkerRuntimeTraceListener.class);

  public boolean isInterestedIn(ITransformedEvent traceEvent, ISession session)
  {
    return traceEvent instanceof MarkerTransformedEvent;
  }

  public void eventFired(ITransformedEvent traceEvent, ISession session)
  {
    MarkerTransformedEvent mte = (MarkerTransformedEvent) traceEvent;

    /*
     * get or create the session data stream for markers
     */
    ISessionData sessionData = session.getData(mte.getModelName());
    MarkerSessionDataStream msds = (MarkerSessionDataStream) sessionData
        .getDataStream("marker");
    OpenMarkerSessionDataStream omsd = (OpenMarkerSessionDataStream) sessionData
        .getDataStream("openMarkers");

    if (msds == null)
    {
      msds = new MarkerSessionDataStream(sessionData, RuntimePlugin
          .getDefault().getPreferenceStore()
          .getInt(RuntimePreferences.RUNTIME_DATA_WINDOW));
      ((LiveSessionData) sessionData).setStreamData("marker", msds);
    }

    if (omsd == null)
    {
      omsd = new OpenMarkerSessionDataStream(sessionData, RuntimePlugin
          .getDefault().getPreferenceStore()
          .getInt(RuntimePreferences.RUNTIME_DATA_WINDOW), msds);
      ((LiveSessionData) sessionData).setStreamData("openMarkers", omsd);
    }

    msds.append(mte);
    omsd.append(mte);
  }

}
