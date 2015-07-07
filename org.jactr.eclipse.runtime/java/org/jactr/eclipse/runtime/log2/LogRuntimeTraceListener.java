package org.jactr.eclipse.runtime.log2;

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
import org.jactr.tools.tracer.transformer.ITransformedEvent;
import org.jactr.tools.tracer.transformer.logging.BulkLogEvent;

public class LogRuntimeTraceListener implements IRuntimeTraceListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(LogRuntimeTraceListener.class);
  
  private boolean _translateTime;
  
  public LogRuntimeTraceListener()
  {
    _translateTime = RuntimePlugin.getDefault().getPreferenceStore()
        .getBoolean(RuntimePreferences.TRANSLATE_TIME);
  }

  public boolean isInterestedIn(ITransformedEvent traceEvent, ISession session)
  {
    return traceEvent instanceof BulkLogEvent;
  }

  public void eventFired(ITransformedEvent traceEvent, ISession session)
  {
    BulkLogEvent ble = (BulkLogEvent) traceEvent;



    /*
     * route to the session
     */
    ISessionData sessionData = session.getData(ble.getModelName());
    LogSessionDataStream lsds = (LogSessionDataStream) sessionData
        .getDataStream("log");
    if (lsds == null)
    {
      lsds = new LogSessionDataStream(sessionData, RuntimePlugin.getDefault()
          .getPreferenceStore().getInt(RuntimePreferences.RUNTIME_DATA_WINDOW));
      ((LiveSessionData) sessionData).setStreamData("log", lsds);
    }

    LogData ld = lsds.getLastData();
    boolean isNew = false;

    if (ld == null || ld.getTime() < ble.getSimulationTime())
    {
      /*
       * create a new one
       */
      ld = new LogData(ble.getSimulationTime(), _translateTime, lsds);
      isNew = true;
    }

    if (isNew)
      lsds.append(ld);

      lsds.update(ble);
  }

}
