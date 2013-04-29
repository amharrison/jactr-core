package org.jactr.eclipse.runtime.trace;

/*
 * default logging
 */
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.tools.tracer.transformer.ITransformedEvent;

public interface IRuntimeTraceListener
{

  public boolean isInterestedIn(ITransformedEvent traceEvent, ISession session);
  
  public void eventFired(ITransformedEvent traceEvent, ISession session);
  
}
