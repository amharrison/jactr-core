/*
 * Created on May 11, 2007 Copyright (C) 2001-2007, Anthony Harrison
 * anh23@pitt.edu (jactr.org) This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version. This library is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author developer
 */
public class EventDispatcher<L extends ICommonRealityListener, E extends ICommonRealityEvent<L>>
{
  /**
   * logger definition
   */
  static private final Log LOGGER = LogFactory.getLog(EventDispatcher.class);

  private ArrayList<Pair>  _listeners;

  private Collection<Pair> _cachedListeners;

  public EventDispatcher()
  {
    _listeners = new ArrayList<Pair>();
  }

  public boolean hasListeners()
  {
    synchronized (_listeners)
    {
      return !_listeners.isEmpty();
    }
  }

  public void addListener(L listener, Executor executor)
  {
    synchronized (_listeners)
    {
      _listeners.add(new Pair(listener, executor));
      _cachedListeners = null;
    }
  }

  public void removeListener(L listener)
  {
    synchronized (_listeners)
    {
      ArrayList<Pair> toRemove = new ArrayList<Pair>(2);
      for (Pair pair : _listeners)
        if (pair._listener == listener) toRemove.add(pair);
      _listeners.removeAll(toRemove);
      _cachedListeners = null;
    }
  }

  public Collection<L> getListeners()
  {
    ArrayList<L> listeners = new ArrayList<L>();
    for (Pair pair : getCachedListeners())
      listeners.add(pair._listener);
    return listeners;
  }

  protected Collection<Pair> getCachedListeners()
  {
    synchronized (_listeners)
    {
      if (_cachedListeners == null)
        _cachedListeners = new ArrayList<Pair>(_listeners);
      return _cachedListeners;
    }
  }

  public void fire(E event)
  {
    for (Pair pair : getCachedListeners())
      pair.fire(event);
  }

  private class Pair
  {
    public L        _listener;

    public Executor _executor;

    public Pair(L listener, Executor executor)
    {
      _listener = listener;
      _executor = executor;
    }

    public void fire(final E event)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Passing " + event + " to " + _listener + " on "
            + _executor);
      try
      {
        _executor.execute(new Runnable() {
          public void run()
          {
            try
            {
              event.fire(_listener);
            }
            catch (Exception e)
            {
              LOGGER.error("Uncaught exception on event dispatch of " + event
                  + " to " + _listener, e);
            }
          }
        });
      }
      catch (RejectedExecutionException ree)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER
              .debug("Event notification was rejected, likely we are shutting down");
      }
    }
  }
}
