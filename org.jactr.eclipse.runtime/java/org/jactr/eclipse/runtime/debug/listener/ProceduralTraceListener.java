package org.jactr.eclipse.runtime.debug.listener;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.module.procedural.event.ProceduralModuleEvent;
import org.jactr.eclipse.runtime.debug.ACTRDebugTarget;
import org.jactr.eclipse.runtime.debug.elements.ACTRStackFrame;
import org.jactr.eclipse.runtime.debug.elements.ACTRThread;
import org.jactr.eclipse.runtime.debug.util.Utilities;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.trace.IRuntimeTraceListener;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.tools.tracer.transformer.ITransformedEvent;
import org.jactr.tools.tracer.transformer.procedural.TransformedProceduralEvent;

public class ProceduralTraceListener implements IRuntimeTraceListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProceduralTraceListener.class);

  
  ACTRDebugTarget                    _target;
  
  public ProceduralTraceListener(ACTRDebugTarget target)
  {
    _target = target;
  }
  
  public void eventFired(ITransformedEvent traceEvent, ISession session)
  {
    TransformedProceduralEvent tpme = (TransformedProceduralEvent) traceEvent;

    if (tpme.getType() == ProceduralModuleEvent.Type.PRODUCTION_FIRED)
    {
      /*
       * we track the stack trace as a list of production fires
       */
      String modelName = tpme.getModelName();
      ACTRThread thread = _target.getThread(modelName);
      if (thread == null)
      {
        LOGGER.warn("Could not find ACTRThread for model " + modelName);
        return;
      }

      String productionName = ASTSupport.getName(tpme.getAST());
      int lineNumber = Utilities.extractLineNumber(_target.getLaunch(),
          modelName, productionName, JACTRBuilder.PRODUCTION);

      ACTRStackFrame trace = new ACTRStackFrame(thread, tpme
          .getSimulationTime(), productionName, lineNumber);
      thread.addStackFrame(trace, false);
    }

  }

  public boolean isInterestedIn(ITransformedEvent traceEvent, ISession session)
  {
    return traceEvent instanceof TransformedProceduralEvent;
  }



}
