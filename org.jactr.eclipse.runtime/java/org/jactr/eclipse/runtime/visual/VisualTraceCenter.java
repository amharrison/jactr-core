package org.jactr.eclipse.runtime.visual;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;
import org.jactr.eclipse.runtime.trace.IRuntimeTraceListener;
import org.jactr.eclipse.runtime.trace.impl.GeneralEventManager;
import org.jactr.eclipse.runtime.trace.impl.RuntimeTraceDataManager;
import org.jactr.tools.tracer.transformer.ITransformedEvent;
import org.jactr.tools.tracer.transformer.visual.TransformedVisualEvent;

public class VisualTraceCenter extends
    RuntimeTraceDataManager<VisualDescriptor>
{
  static private final Log         LOGGER   = LogFactory
                                                .getLog(VisualTraceCenter.class);

  static private VisualTraceCenter _default = new VisualTraceCenter();

  static public VisualTraceCenter get()
  {
    return _default;
  }

  private final GeneralEventManager<IVisualTraceCenterListener, VisualTraceCenterEvent> _listenerList;

  private IRuntimeTraceListener                                                         _runtimeListener;

  protected VisualTraceCenter()
  {
    _listenerList = new GeneralEventManager<IVisualTraceCenterListener, VisualTraceCenterEvent>(
        new GeneralEventManager.INotifier<IVisualTraceCenterListener, VisualTraceCenterEvent>() {

          public void notify(IVisualTraceCenterListener listener,
              VisualTraceCenterEvent event)
          {
            if (event._isAdd)
              listener.modelAdded(event._modelName, event._descriptor);
            else
              listener.modelRemoved(event._modelName, event._descriptor);
          }
        });

    _runtimeListener = new IRuntimeTraceListener() {

      public void eventFired(ITransformedEvent traceEvent, ISession session)
      {

        VisualTraceCenter.get().process(traceEvent, session);
      }

      public boolean isInterestedIn(ITransformedEvent traceEvent,
          ISession session)
      {
        return traceEvent instanceof TransformedVisualEvent;
      }

    };
  }

  public IRuntimeTraceListener getRuntimeListener()
  {
    return _runtimeListener;
  }

  public void add(IVisualTraceCenterListener listener)
  {
    _listenerList.addListener(listener);
  }

  public void remove(IVisualTraceCenterListener listener)
  {
    _listenerList.removeListener(listener);
  }

  @Override
  protected VisualDescriptor createRuntimeTraceData(ISession session,
      String commonName, String modelName)
  {
    return new VisualDescriptor(commonName, modelName, session);
  }

  @Override
  protected void modelAdded(ISession session, VisualDescriptor data,
      String modelName)
  {
    super.modelAdded(session, data, modelName);
    _listenerList.notify(new VisualTraceCenterEvent(modelName, data, true));
  }

  @Override
  protected void disposeRuntimeTraceData(ISession session, String modelName,
      VisualDescriptor data)
  {
    // noop

  }

  @Override
  protected void modelRemoved(ISession session, VisualDescriptor data,
      String modelName)
  {
    super.modelRemoved(session, data, modelName);
    _listenerList.notify(new VisualTraceCenterEvent(modelName, data, false));
  }

  @Override
  protected void process(ISession session, String modelName,
      VisualDescriptor data, ITransformedEvent event)
  {
    try
    {
      data.process((TransformedVisualEvent) event);
    }
    catch (Exception e)
    {
      LOGGER.error("Failed to process visual event for " + modelName, e);
    }
  }

  private class VisualTraceCenterEvent
  {
    public boolean          _isAdd;

    public String           _modelName;

    public VisualDescriptor _descriptor;

    public VisualTraceCenterEvent(String modelName, VisualDescriptor data,
        boolean isAdd)
    {
      _isAdd = isAdd;
      _modelName = modelName;
      _descriptor = data;
    }
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
