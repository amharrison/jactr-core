package org.jactr.eclipse.runtime.visual;

import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.data.LiveSessionData;
import org.jactr.eclipse.runtime.trace.IRuntimeTraceListener;
import org.jactr.tools.tracer.transformer.ITransformedEvent;
import org.jactr.tools.tracer.transformer.visual.TransformedVisualEvent;

public class ModelVisiconRuntimeListener implements IRuntimeTraceListener
{

  public ModelVisiconRuntimeListener()
  {
  }

  @Override
  public boolean isInterestedIn(ITransformedEvent traceEvent, ISession session)
  {
    return traceEvent instanceof TransformedVisualEvent;
  }

  @Override
  public void eventFired(ITransformedEvent traceEvent, ISession session)
  {
    TransformedVisualEvent tve = (TransformedVisualEvent) traceEvent;
    ISessionData sessionData = session.getData(tve.getModelName());
    ModelVisiconSessionDataStream mvsds = (ModelVisiconSessionDataStream) sessionData
        .getDataStream("visicon");

    if (mvsds == null)
    {
      // create it
      mvsds = new ModelVisiconSessionDataStream(tve.getModelName(),
          sessionData);
      ((LiveSessionData) sessionData).setStreamData("visicon", mvsds);
    }

    VisualDescriptor root = mvsds.getRoot();
    if (root == null)
    {
      root = new VisualDescriptor(tve.getModelName(), session);
      mvsds.setRoot(root);
    }

    root.process(tve);
  }

}
