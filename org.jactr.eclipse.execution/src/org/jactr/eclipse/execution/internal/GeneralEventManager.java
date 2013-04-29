package org.jactr.eclipse.execution.internal;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executor;

public class GeneralEventManager<L, E>
{

  private final INotifier<L, E>         _notifier;

  private Collection<Pair>              _listeners;

  private ThreadLocal<Collection<Pair>> _recycledContainer;

  public GeneralEventManager(INotifier<L, E> notifier)
  {
    if (notifier == null)
      throw new NullPointerException("INotifier cannot be null");
    _notifier = notifier;
    _recycledContainer = new ThreadLocal<Collection<Pair>>();
  }

  synchronized public boolean hasListeners()
  {
    return _listeners != null && _listeners.size() > 0;
  }

  synchronized public void clear()
  {
    if (_listeners != null) _listeners.clear();
    _listeners = null;
    _recycledContainer.remove();
  }

  public void addListener(L listener)
  {
    addListener(listener, null);
  }

  synchronized public void addListener(L listener, Executor executor)
  {
    if (_listeners == null) _listeners = new ArrayList<Pair>();

    _listeners.add(new Pair(listener, executor));
  }

  synchronized public void removeListener(L listener)
  {
    Iterator<Pair> itr = _listeners.iterator();
    while (itr.hasNext())
    {
      Pair pair = itr.next();
      if (pair._listener == listener)
      {
        itr.remove();
        break;
      }
    }
  }

  private Collection<Pair> getListenerPairs()
  {
    Collection<Pair> container = _recycledContainer.get();
    if (container == null)
    {
      container = new ArrayList<Pair>();
      _recycledContainer.set(container);
    }
    synchronized (this)
    {
      if (_listeners != null) container.addAll(_listeners);
    }

    return container;
  }

  public void notify(final E event)
  {
    for (Pair pair : getListenerPairs())
    {
      Executor executor = pair._executor;
      final L listener = pair._listener;

      if (executor == null)
        _notifier.notify(listener, event);
      else
          executor.execute(new Runnable() {
            public void run()
            {
              _notifier.notify(listener, event);
            }
          });
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
