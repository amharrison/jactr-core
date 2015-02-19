package org.jactr.eclipse.runtime.trace.impl;

/*
 * default logging
 */
import java.util.Collection;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.common.EventManager;

public class GeneralEventManager<L, E> extends EventManager
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(GeneralEventManager.class);

  private final INotifier<L, E>      _notifier;

  public GeneralEventManager(INotifier<L, E> notifier)
  {
    if (notifier == null)
      throw new NullPointerException("INotifier cannot be null");
    _notifier = notifier;
  }

  public void clear()
  {
    clearListeners();
  }

  public void addListener(L listener)
  {
    addListener(listener, null);
  }

  public void addListener(L listener, Executor executor)
  {
    addListenerObject(new Pair(listener, executor));
  }

  public void getListeners(Collection<L> container)
  {
    for (Object pair : getListeners())
    {
      L actual = ((Pair) pair)._listener;
      container.add(actual);
    }

  }

  public void removeListener(L listener)
  {
    for (Object pair : getListeners())
    {
      L actual = ((Pair) pair)._listener;
      if (actual == listener)
      {
        removeListenerObject(pair);
        break;
      }
    }
  }

  public void notify(final E event)
  {
    for (Object pair : getListeners())
    {
      Executor executor = ((Pair) pair)._executor;
      final L listener = ((Pair) pair)._listener;

      if (executor == null)
        _notifier.notify(listener, event);
      else
        try
        {
          executor.execute(new Runnable() {
            public void run()
            {
              _notifier.notify(listener, event);
            }
          });
        }
        catch (Exception e)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("Failed to dispatch event to " + listener, e);
        }
    }
  }

  static public interface INotifier<L, E>
  {
    public void notify(L listener, E event);
  }

  private class Pair
  {
    public Executor _executor;

    public L        _listener;

    public Pair(L listener, Executor executor)
    {
      _listener = listener;
      _executor = executor;
    }

    @Override
    public boolean equals(Object o)
    {
      return _listener.equals(o);
    }

    @Override
    public int hashCode()
    {
      return _listener.hashCode();
    }
  }
}
