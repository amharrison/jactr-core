package org.jactr.eclipse.runtime.ui.looper;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.impl.Session2SessionAdapter;
import org.jactr.eclipse.runtime.trace.IRuntimeTraceListener;
import org.jactr.eclipse.runtime.trace.impl.RuntimeTraceDataManager;
import org.jactr.tools.tracer.transformer.ITransformedEvent;
import org.jactr.tools.tracer.transformer.procedural.TransformedProceduralEvent;

public class RuntimeLoopDetector extends
    RuntimeTraceDataManager<RecentProductionModelData> implements
    IRuntimeTraceListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RuntimeLoopDetector.class);

  private LoopNotifier               _notifier;

  public RuntimeLoopDetector()
  {
    _notifier = new LoopNotifier();
  }

  @Override
  protected RecentProductionModelData createRuntimeTraceData(
      ACTRSession session, String commonName, String modelName)
  {
    return new RecentProductionModelData(session, modelName, _notifier);
  }

  @Override
  protected void disposeRuntimeTraceData(ACTRSession session, String modelName,
      RecentProductionModelData data)
  {
    data.dispose();
  }

  @Override
  protected void process(ACTRSession session, String modelName,
      RecentProductionModelData data, ITransformedEvent event)
  {
    data.process(session, (TransformedProceduralEvent) event);
  }

  @Override
  public void sessionClosed(ACTRSession session, boolean normal)
  {
    _notifier.clear(session);
  }

  @Override
  public void sessionDestroyed(ACTRSession session)
  {

  }

  @Override
  public void sessionOpened(ACTRSession session)
  {
    // so that we only do this for debug
    if (session.isDebugSession()) _notifier.add(session);
  }

  public void eventFired(ITransformedEvent traceEvent, ISession session)
  {
    Session2SessionAdapter nsw = (Session2SessionAdapter) session;
    process(traceEvent, (ACTRSession) nsw.getOldSession());
  }

  public boolean isInterestedIn(ITransformedEvent traceEvent,
      ISession session)
  {
    Session2SessionAdapter nsw = (Session2SessionAdapter) session;
    return nsw.getOldSession().isDebugSession()
        && traceEvent instanceof TransformedProceduralEvent;
  }

}
