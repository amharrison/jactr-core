package org.jactr.eclipse.runtime.session.data;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.trace.IRuntimeTraceListener;
import org.jactr.tools.tracer.transformer.ITransformedEvent;

public class RuntimeTraceListener implements IRuntimeTraceListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RuntimeTraceListener.class);

  public boolean isInterestedIn(ITransformedEvent traceEvent,
      ISession session)
  {
    /*
     * we just use this to harvest the times
     */
    String modelName = traceEvent.getModelName();
    ISessionData data = session.getData(modelName);

    if (data == null)
    {
      data = new LiveSessionData(session, modelName);
      session.addData(modelName, data);
    }

    if (data instanceof LiveSessionData)
      ((LiveSessionData) data).timeUpdate(traceEvent.getSimulationTime());

    return false;
  }

  public void eventFired(ITransformedEvent traceEvent, ISession session)
  {
    // noop
  }

}
