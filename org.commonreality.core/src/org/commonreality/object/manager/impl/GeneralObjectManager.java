/*
 * Created on May 10, 2007 Copyright (C) 2001-2007, Anthony Harrison
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
package org.commonreality.object.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.event.EventDispatcher;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.delta.IObjectDelta;
import org.commonreality.object.delta.ObjectDelta;
import org.commonreality.object.manager.IMutableObjectManager;
import org.commonreality.object.manager.event.IObjectEvent;
import org.commonreality.object.manager.event.IObjectListener;
import org.commonreality.object.manager.event.ObjectEvent;

/**
 * Handles the adding, updating, and removal of a specific type of simulation
 * object - it also deals with the listeners. This class is generally hidden
 * from users as they are generally encapsulated within the ObjectManager
 * 
 * @author developer
 */
public class GeneralObjectManager<O extends ISimulationObject, L extends IObjectListener<O>>
    implements IMutableObjectManager<O, L>
{
  /**
   * logger definition
   */
  static private final Log                      LOGGER      = LogFactory
                                                                .getLog(GeneralObjectManager.class);

  private Map<IIdentifier, O>                   _objectMap;

  private EventDispatcher<L, ObjectEvent<O, L>> _dispatcher = new EventDispatcher<L, ObjectEvent<O, L>>();
  
  private ReentrantLock _lock = new ReentrantLock();
  private Condition _changed = _lock.newCondition();

  public GeneralObjectManager()
  {
    _objectMap = new HashMap<IIdentifier, O>();
  }

  /**
   * add a listener that will have its objectAdded,removed,updated methods fired
   * appropriately on the provided executor
   * 
   * @param listener
   * @param executor
   */
  public void addListener(L listener, Executor executor)
  {
    _dispatcher.addListener(listener, executor);
  }

  /**
   * remove said listener
   * 
   * @param listener
   */
  public void removeListener(L listener)
  {
    _dispatcher.removeListener(listener);
  }

  /**
   * are there any registered listeners?
   * 
   * @return
   */
  public boolean hasListeners()
  {
    return _dispatcher.hasListeners();
  }

  /**
   * return a collection containing all the installed listeners
   * 
   * @return
   */
  public Collection<L> getListeners()
  {
    return _dispatcher.getListeners();
  }

  /**
   * update the object that is described by this delta with this delta. and fire
   * an event
   * 
   * @param delta
   */
  @SuppressWarnings("unchecked")
  public boolean update(IObjectDelta delta)
  {
    IIdentifier id = delta.getIdentifier();
    if (!contains(id)) return false;
    O object = get(id);
    if (object == null) return false;

    if (object instanceof IMutableObject && delta instanceof ObjectDelta)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Updating " + object + " with " + delta);
      ((ObjectDelta) delta).apply((IMutableObject) object);
      fireUpdate(Collections.singleton(delta));
      return true;
    }
    return false;
  }

  /**
   * update all these obejcts
   * 
   * @param deltas
   */
  @SuppressWarnings("unchecked")
  public boolean update(Collection<IObjectDelta> deltas)
  {
    boolean updated = false;
    for (IObjectDelta delta : deltas)
    {
      IIdentifier id = delta.getIdentifier();
      if (contains(id))
      {
        O object = get(id);
        if (object instanceof IMutableObject && delta instanceof ObjectDelta)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("Updating " + object + " with " + delta);
          ((ObjectDelta) delta).apply((IMutableObject) object);
          updated = true;
        }
      }
    }

    if (updated) fireUpdate(deltas);
    return updated;
  }

  /**
   * add this single object and fire an event
   * 
   * @param object
   */
  public boolean add(O object)
  {
    IIdentifier id = object.getIdentifier();
    if (contains(id)) return false;
    synchronized (_objectMap)
    {
      _objectMap.put(id, object);
    }

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Added " + id);

    fire(IObjectEvent.Type.ADDED, Collections.singleton(object));
    return true;
  }

  /**
   * add all of the objects to the manager and fire an appropriate event
   * 
   * @param objects
   */
  public boolean add(Collection<O> objects)
  {
    boolean added = false;
    for (O object : objects)
    {
      IIdentifier id = object.getIdentifier();
      if (!contains(id)) synchronized (_objectMap)
      {
        _objectMap.put(id, object);
        added = true;
        if (LOGGER.isDebugEnabled()) LOGGER.debug("added " + id);
      }
    }

    if (added) fire(IObjectEvent.Type.ADDED, objects);
    return added;
  }

  /**
   * fire an event either added or removed
   * 
   * @param type
   * @param objects
   */
  final protected void fire(IObjectEvent.Type type, Collection<O> objects)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Firing " + type + " event on " + objects);
    if (_dispatcher.hasListeners())
      _dispatcher.fire(new ObjectEvent<O, L>(type, objects));
    
    _lock.lock();
    _changed.signalAll();
    _lock.unlock();
  }

  /**
   * fire an update event
   * 
   * @param deltas
   */
  final protected void fireUpdate(Collection<? extends IObjectDelta> deltas)
  {
    
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Firing update event for "+deltas);
    if (_dispatcher.hasListeners())
      _dispatcher.fire(new ObjectEvent<O, L>(deltas));
    
    _lock.lock();
    _changed.signalAll();
    _lock.unlock();
  }

  /**
   * remove the object that is keyed on id and fire an event
   * 
   * @param id
   */
  public boolean remove(IIdentifier id)
  {
    if (!contains(id)) return false;
    O removed = null;
    synchronized (_objectMap)
    {
      removed = _objectMap.remove(id);
    }

    if (removed != null)
      fire(IObjectEvent.Type.REMOVED, Collections.singleton(removed));
    return removed != null;
  }

  /**
   * remove all the objects that are keyed on identifier. this fire a bulk
   * remove event
   * 
   * @param identifiers
   */
  public boolean remove(Collection<IIdentifier> identifiers)
  {
    Collection<O> removed = new ArrayList<O>();
    for (IIdentifier id : identifiers)
      synchronized (_objectMap)
      {
        O old = _objectMap.remove(id);
        if (old != null) removed.add(old);
      }

    if (removed.size() != 0) fire(IObjectEvent.Type.REMOVED, removed);
    return removed.size() != 0;
  }

  /**
   * return the object O that is keyed on identifier
   * 
   * @param identifier
   * @return object or null
   */
  public O get(IIdentifier identifier)
  {
    synchronized (_objectMap)
    {
      return _objectMap.get(identifier);
    }
  }

  /**
   * return all the identifiers that have matched objects
   * 
   * @return
   */
  public Collection<IIdentifier> getIdentifiers()
  {
    synchronized (_objectMap)
    {
      return new ArrayList<IIdentifier>(_objectMap.keySet());
    }
  }

  /**
   * does this object manager have an object O that is keyed on this identifier
   * 
   * @param identifier
   * @return
   */
  public boolean contains(IIdentifier identifier)
  {
    return _objectMap.containsKey(identifier);
  }
  
  public void waitForChange() throws InterruptedException
  {
    try
    {
      _lock.lock();
      _changed.await();
    }
    finally
    {
      _lock.unlock();
    }
  }
}
