package org.jactr.eclipse.runtime.trace;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.trace.impl.GeneralEventManager;
import org.jactr.tools.tracer.transformer.ITransformedEvent;

public class RuntimeTraceManager
{
  static private final transient Log                        LOGGER = LogFactory
                                                                       .getLog(RuntimeTraceManager.class);

  private GeneralEventManager<IRuntimeTraceListener, Event> _eventManager;

  public RuntimeTraceManager()
  {
    _eventManager = new GeneralEventManager<IRuntimeTraceListener, Event>(
        new GeneralEventManager.INotifier<IRuntimeTraceListener, Event>() {

          public void notify(IRuntimeTraceListener listener, Event event)
          {
            try
            {
              if (listener.isInterestedIn(event.event, event.session))
                listener.eventFired(event.event, event.session);
            }
            catch (Exception e)
            {
              LOGGER.error(String.format(
                  "Failed to propogate event [%s] to listener [%s] ",
                  event.event.getClass().getName(), listener.getClass()
                      .getName()), e);
            }
          }
        });
  }

  public void clear()
  {
    _eventManager.clear();
  }

  public Collection<IRuntimeTraceListener> getListeners(
      Collection<IRuntimeTraceListener> container)
  {
    if (container == null) container = new ArrayList<IRuntimeTraceListener>();
    _eventManager.getListeners(container);
    return container;
  }

  public void addListener(IRuntimeTraceListener listener)
  {
    addListener(listener, null);
  }

  public void addListener(IRuntimeTraceListener listener, Executor executor)
  {
    _eventManager.addListener(listener, executor);
  }

  public void removeListener(final IRuntimeTraceListener listener)
  {
    _eventManager.removeListener(listener);
  }

  public void fireEvent(ITransformedEvent event, ISession session)
  {
    _eventManager.notify(new Event(event, session));
  }

  public void fireEvents(Collection<ITransformedEvent> events, ISession session)
  {
    for (ITransformedEvent event : events)
      _eventManager.notify(new Event(event, session));
    // RuntimePlugin.info(String.format("(bulk) fired %s %.4f", event.getClass()
    // .getSimpleName(), event.getSimulationTime()));
  }

  public void fireEvents(IProgressMonitor monitor,
      Collection<ITransformedEvent> events, ISession session)
  {
    for (ITransformedEvent event : events)
    {
      if (monitor.isCanceled()) return;

      try
      {
        _eventManager.notify(new Event(event, session));

        // RuntimePlugin.info(String.format("(monitored) fired %s %.4f", event
        // .getClass().getSimpleName(), event.getSimulationTime()));
      }
      catch (Exception e)
      {
        RuntimePlugin.error(String.format(
            "Failed to notify event %s, skipping", event.getClass()
                .getSimpleName()), e);
      }
      finally
      {
        monitor.worked(1);
      }
    }

  }

  private class Event
  {
    final public ITransformedEvent event;

    final public ISession          session;

    public Event(ITransformedEvent event, ISession session)
    {
      this.event = event;
      this.session = session;
    }
  }
}
