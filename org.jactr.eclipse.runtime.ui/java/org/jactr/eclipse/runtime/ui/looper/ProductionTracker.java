package org.jactr.eclipse.runtime.ui.looper;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;
import org.jactr.eclipse.runtime.trace.impl.RuntimeTraceDataManager;
import org.jactr.tools.tracer.transformer.ITransformedEvent;

public class ProductionTracker extends RuntimeTraceDataManager
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProductionTracker.class);

  @Override
  protected Object createRuntimeTraceData(ISession session,
      String commonName, String modelName)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void disposeRuntimeTraceData(ISession session, String modelName,
      Object data)
  {
    // TODO Auto-generated method stub

  }

  @Override
  protected void process(ISession session, String modelName, Object data,
      ITransformedEvent event)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void sessionClosed(ISession session, boolean normal)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void sessionDestroyed(ISession session)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void sessionOpened(ISession session)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void sessionClosed(ISession session)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void newSessionData(ISessionData sessionData)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void newSessionDataStream(ISessionData sessionData,
      ISessionDataStream sessionDataStream)
  {
    // TODO Auto-generated method stub

  }

}
