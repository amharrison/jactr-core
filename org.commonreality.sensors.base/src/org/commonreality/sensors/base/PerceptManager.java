package org.commonreality.sensors.base;

import java.util.ArrayList;
/*
 * default logging
 */
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.ISensoryObject;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.delta.DeltaTracker;
import org.commonreality.object.delta.IObjectDelta;
import org.commonreality.object.identifier.ISensoryIdentifier;
import org.commonreality.object.manager.IAgentObjectManager;
import org.commonreality.object.manager.IObjectManager;
import org.commonreality.sensors.ISensor;

/**
 * the percept manager is tailored (primarily for afferent percepts) to create
 * percepts for arbitrary programmatic objects. Objects marked as dirty in the
 * PerceptManager are passed through the set of {@link IObjectCreator}s, to find
 * the set of possible percepts for the object. The {@link IObjectCreator} is
 * responsible for creating the {@link IObjectKey} which tracks the
 * percept-object linkage as well as the initial {@link ISimulationObject} (most
 * likely a {@link IAfferentObject}). <br/>
 * <br/>
 * The {@link ISimulationObject} and {@link IObjectKey} for that object is then
 * passed through the set of {@link IObjectProcessor}s which can add additional
 * properties to the percept. <br/>
 * <br/>
 * Once all of this is done, the percept manager signals the {@link BaseSensor}
 * will all the additions and modifications( {@link BaseSensor#add(Collection)}
 * and {@link BaseSensor#update(Collection)}. Removals are handled by flagging
 * {@link #flagForRemoval(Object)} the object, which then delegates the actual
 * clean up to the {@link IObjectCreator} and the {@link BaseSensor}
 * 
 * @author harrison
 */
public class PerceptManager
{
  /**
   * Logger definition
   */
  static private final transient Log       LOGGER = LogFactory
                                                      .getLog(PerceptManager.class);

  private Map<Object, Set<IObjectKey>>     _objectToKey;

  private Set<Object>                      _dirtyObjects;

  private Collection<IObjectCreator>       _creators;

  private Collection<IObjectProcessor>     _processors;

  /*
   * weak collection of objects that cannot be encoded..
   */
  private WeakHashMap<Object, Object>      _nullObjects;

  private final BaseSensor                 _sensor;

  private Map<IIdentifier, ISensoryObject> _objectsInLimbo;

  private final Set<Object>                _toBeDeleted;

  public PerceptManager(BaseSensor sensor)
  {
    _sensor = sensor;
    _objectToKey = new HashMap<Object, Set<IObjectKey>>();
    _dirtyObjects = new HashSet<Object>();
    _objectsInLimbo = new HashMap<IIdentifier, ISensoryObject>();
    _nullObjects = new WeakHashMap<Object, Object>();
    _creators = new ArrayList<IObjectCreator>();
    _processors = new ArrayList<IObjectProcessor>();
    _toBeDeleted = new HashSet<Object>();
  }

  public BaseSensor getSensor()
  {
    return _sensor;
  }

  public void reset()
  {
    for (IObjectCreator creator : _creators)
      creator.uninstalled(this);
    _creators.clear();

    for (IObjectProcessor processor : _processors)
      processor.uninstalled(this);
    _processors.clear();

    _objectsInLimbo.clear();
    _nullObjects.clear();
  }

  public void install(IObjectCreator creater)
  {
    _creators.add(creater);
    creater.installed(this);
  }

  public void install(IObjectProcessor processor)
  {
    _processors.add(processor);
    processor.installed(this);
  }

  /**
   * call when an object is new or has changed.
   * 
   * @param object
   */
  synchronized public void markAsDirty(Object object)
  {
    if (!_nullObjects.containsKey(object))
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("%s is dirty", object));
      _dirtyObjects.add(object);
    }
  }

  /**
   * flag this object as ready for removal
   * 
   * @param object
   */
  synchronized public void flagForRemoval(Object object)
  {
    if (!_nullObjects.containsKey(object))
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("%s flagged for removal", object));
      _toBeDeleted.add(object);
      // remove from object keys immediately?
    }
  }

  private Set<IObjectKey> getKeys(Object objec)
  {
    return _objectToKey.get(objec);
  }

  private void removeKeys(Object object)
  {
    _objectToKey.remove(object);
  }

  private Set<IObjectKey> createKeys(Object object)
  {
    Set<IObjectKey> rtn = new HashSet<>();
    IAgentObjectManager manager = _sensor.getAgentObjectManager();
    for (IObjectCreator creator : _creators)
      if (creator.handles(object))
        for (IIdentifier agent : _sensor.getInterfacedAgents())
        {
          IObjectKey key = creator.createKey(object);

          if (key != null)
          {
            ISensoryObject simObject = creator.createObject(key, object,
                _sensor, manager.get(agent));

            if (simObject != null)
            {
              ISensoryIdentifier id = simObject.getIdentifier();
              key.setIdentifier(id);
              /*
               * the object doesnt officially exist until CR replies, so we need
               * to hold on to this temporary version so we can set the
               * properties of it
               */
              _objectsInLimbo.put(id, simObject);

              if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format(
                    "Created new percept object for key %s (%s)", key, id));
            }
            /*
             * add the object to the simulation..
             */
            rtn.add(key);
          }
        }

    _objectToKey.put(object, rtn);

    return rtn;
  }

  protected IObjectManager getObjectManager(ISensor sensor)
  {
    return sensor.getAfferentObjectManager();
  }

  private void process(Collection<IObjectKey> keys)
  {
    List<IObjectDelta> updates = new ArrayList<>(keys.size());
    List<IMutableObject> newAdds = new ArrayList<>(keys.size());

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Processing keys  %s", keys));

    IObjectManager manager = getObjectManager(_sensor);
    for (IObjectKey objectKey : keys)
    {
      boolean isNewAdd = false;
      IIdentifier identifier = objectKey.getIdentifier();
      ISensoryObject simObject = (ISensoryObject) manager.get(identifier);

      if (simObject == null)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format(
              "CR has yet to acknowledge the new object %s (%s)", objectKey,
              identifier));

        // object hasn't been created fully, it's in limbo..
        simObject = _objectsInLimbo.remove(identifier);

        // this could still happen if CR is really freaking slow to reply
        // and we've marked the object as dirty multiple times now..
        // instead of ignoring it, we requeue
        if (simObject == null)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format(
                "No object for %s, requeueing until CR is heard from",
                objectKey));

          markAsDirty(objectKey.getObject());
          continue;
        }
        else
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("%s is a new add", objectKey));
          isNewAdd = true;
          // newAdds.add(simObject);
        }
      }

      DeltaTracker tracker = null;
      for (IObjectProcessor processor : _processors)
        if (processor.handles(objectKey))
        {
          if (tracker == null)
          {
            tracker = new DeltaTracker(simObject);
            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format("Created tracker for %s", objectKey));
          }

          processor.process(objectKey, tracker);

          if (LOGGER.isDebugEnabled())
            LOGGER
                .debug(String.format("%s processed %s", processor, objectKey));
        }

      if (tracker != null) if (!isNewAdd && tracker.hasChanged())
      {
        /*
         * we can send..
         */
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("%s actually changed", objectKey));

        updates.add(tracker.getDelta());
      }
      else if (isNewAdd) newAdds.add(tracker);
    }

    /*
     * let's send out some data..
     */

    if (newAdds.size() > 0)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Requesting addition of %d percepts",
            newAdds.size()));

      _sensor.add(newAdds);
    }

    if (updates.size() > 0)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Requesting update of %d percepts", updates
            .size()));

      _sensor.update(updates);
    }


  }
  
  private void remove(Collection<Object> container)
  {
    if (container.size() != 0)
    {
      List<ISensoryIdentifier> toBeRemoved = new ArrayList<>();

      for (Object object : container)
      {
        Set<IObjectKey> keys = getKeys(object);
        if (keys != null) for (IObjectKey key : keys)
          if (key.getCreator().canDelete(key))
          {
            toBeRemoved.add(key.getIdentifier());
            
            for (IObjectProcessor processor : _processors)
              if (processor.handles(key))
                processor.deleted(key);
                
            
            key.getCreator().deleteKey(key);
          }

        removeKeys(object);
      }

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Removing %s", toBeRemoved));

      _sensor.removeIdentifiers(toBeRemoved);
    }
  }

  public void processDirtyObjects()
  {
    List<Object> container = new ArrayList<>();

    synchronized (this)
    {
      container.addAll(_toBeDeleted);
      _toBeDeleted.clear();
    }

    remove(container);

    container.clear();

    synchronized (this)
    {
      // not thread safe
      container.addAll(_dirtyObjects);
      _dirtyObjects.clear();
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Processing %d dirty objects", container
          .size()));

    for (Object object : container)
    {
      /*
       * if we dont have a key, try to create one.
       */
      Set<IObjectKey> keys = getKeys(object);

      // possibly creating objects..
      if (keys == null)
      {
        keys = createKeys(object);

        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Created %d keys for %s", keys.size(),
              object));

      }
      else
        /*
         * let's check to be sure object hasn't changed. If so, replace
         */
        for(IObjectKey key : keys)
          if (key.isObjectImmutable() && key.getObject() != object)
            key.replaceObject(object);

      // and process the keys
      if (keys.size() > 0)
        process(keys);
      else
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format(
              "No keys created for %s, will ignore henceforth", object));

        _nullObjects.put(object, null);
      }
    }

  }
}
