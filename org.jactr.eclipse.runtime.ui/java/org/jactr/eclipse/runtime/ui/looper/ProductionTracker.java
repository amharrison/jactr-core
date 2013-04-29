package org.jactr.eclipse.runtime.ui.looper;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;
import org.jactr.eclipse.runtime.launching.session.AbstractSession;
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
  protected Object createRuntimeTraceData(ACTRSession session,
      String commonName, String modelName)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void disposeRuntimeTraceData(ACTRSession session, String modelName,
      Object data)
  {
    // TODO Auto-generated method stub

  }

  @Override
  protected void process(ACTRSession session, String modelName, Object data,
      ITransformedEvent event)
  {
    // TODO Auto-generated method stub

  }

  public void sessionClosed(AbstractSession session, boolean normal)
  {
    // TODO Auto-generated method stub

  }

  public void sessionDestroyed(AbstractSession session)
  {
    // TODO Auto-generated method stub

  }

  public void sessionOpened(AbstractSession session)
  {
    // TODO Auto-generated method stub

  }

}
