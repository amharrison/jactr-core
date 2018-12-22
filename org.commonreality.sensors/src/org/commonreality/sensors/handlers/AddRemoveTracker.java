package org.commonreality.sensors.handlers;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.command.object.IObjectCommand;
import org.commonreality.net.message.request.object.ObjectCommandRequest;
import org.commonreality.net.message.request.object.ObjectDataRequest;
import org.commonreality.object.ISensoryObject;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.delta.FullObjectDelta;
import org.commonreality.object.delta.IObjectDelta;
import org.commonreality.sensors.ISensor;
import org.commonreality.time.impl.BasicClock;

/**
 * utility class that tracks the time to live for {@link ISimulationObject}s,
 * removing them after they have expired.
 * 
 * @author harrison
 */
public class AddRemoveTracker
{
  /**
   * Logger definition
   */
  static private final transient Log                     LOGGER = LogFactory
                                                                    .getLog(AddRemoveTracker.class);

  private TreeMap<Double, Collection<ISimulationObject>> _removeObjects;

  private TreeMap<Double, Collection<ISimulationObject>> _addObjects;

  public AddRemoveTracker()
  {
    _removeObjects = new TreeMap<Double, Collection<ISimulationObject>>();
    _addObjects = new TreeMap<Double, Collection<ISimulationObject>>();
  }

  protected Collection<ISimulationObject> getObjectsCollection(double expirationTime,
      boolean create, SortedMap<Double, Collection<ISimulationObject>> map)
  {
    synchronized (map)
    {
      Collection<ISimulationObject> rtn = map.get(expirationTime);
      if (rtn == null && create)
      {
        rtn = new ArrayList<ISimulationObject>();
        map.put(expirationTime, rtn);
      }
      return rtn;
    }
  }

  protected Collection<ISimulationObject> getElapsedObjects(
      double expirationTime,
      SortedMap<Double, Collection<ISimulationObject>> map)
  {
    Collection<ISimulationObject> rtn = Collections.emptyList();
    synchronized (map)
    {
      Set<Double> keySet = new TreeSet<Double>(map.headMap(expirationTime)
          .keySet());

      if (map.containsKey(expirationTime)) keySet.add(expirationTime);

      if (keySet.size() != 0)
      {
        rtn = new ArrayList<ISimulationObject>();
        for (Double key : keySet)
          rtn.addAll(map.remove(key));
      }
    }

    return rtn;
  }

  protected double getNextElapseTime(
      SortedMap<Double, Collection<ISimulationObject>> map)
  {
    Double smallestKey = Double.NaN;
    synchronized (map)
    {
      if (map.size() != 0) smallestKey = map.firstKey();
    }
    return smallestKey;
  }

  public void add(ISimulationObject object, double addTime, double removeTime)
  {
    Collection<ISimulationObject> collection = null;

    addTime = BasicClock.constrainPrecision(addTime);
    removeTime = BasicClock.constrainPrecision(removeTime);

    synchronized (_removeObjects)
    {
      collection = getObjectsCollection(removeTime, true, _removeObjects);
      collection.add(object);
    }

    synchronized (_addObjects)
    {
      collection = getObjectsCollection(addTime, true, _addObjects);
      collection.add(object);
    }
  }

  /**
   * returns the next time that something will expire
   * 
   * @param currentTime
   * @param sensor
   * @return
   */
  public double update(double currentTime, ISensor sensor)
  {
    handleAdditions(currentTime, sensor);
    handleRemovals(currentTime, sensor);

    double nextAdd = getNextElapseTime(_addObjects);
    double nextRemove = getNextElapseTime(_removeObjects);

    if (Double.isNaN(nextAdd)) return nextRemove;

    if (!Double.isNaN(nextRemove)) return Math.min(nextAdd, nextRemove);

    return nextRemove; // nan
  }

  protected void handleAdditions(double currentTime, ISensor sensor)
  {
    Collection<ISimulationObject> expired = getElapsedObjects(currentTime,
        _addObjects);
    IIdentifier sid = sensor.getIdentifier();

    if (expired.size() != 0)
    {
      Collection<IIdentifier> toAdd = new ArrayList<IIdentifier>();
      Collection<IObjectDelta> deltas = new ArrayList<IObjectDelta>();

      IIdentifier lastDest = null;
      IIdentifier.Type lastType = null;

      for (ISimulationObject object : expired)
      {
        IIdentifier dest = getDestination(object);

        if ((!dest.equals(lastDest) || !dest.getType().equals(lastType))
            && toAdd.size() != 0)
        {
          // send what we've collected so far
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("Sending addition of " + toAdd + " to " + lastDest);

          sensor.send(new ObjectDataRequest(sid, lastDest, deltas));
          sensor.send(new ObjectCommandRequest(sid, lastDest,
              IObjectCommand.Type.ADDED, toAdd));
          toAdd.clear();
        }

        toAdd.add(object.getIdentifier());
        deltas.add(new FullObjectDelta(object));

        lastDest = dest;
      }

      if (toAdd.size() != 0)
      {
        /*
         * and send what's left
         */
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Sending addition of " + toAdd + " to " + lastDest);

        sensor.send(new ObjectDataRequest(sid, lastDest, deltas));
        sensor.send(new ObjectCommandRequest(sid, lastDest,
            IObjectCommand.Type.ADDED, toAdd));
      }
    }
  }

  protected void handleRemovals(double currentTime, ISensor sensor)
  {
    Collection<ISimulationObject> expired = getElapsedObjects(currentTime,
        _removeObjects);
    IIdentifier sid = sensor.getIdentifier();

    if (expired.size() != 0)
    {
      Collection<IIdentifier> toRemove = new ArrayList<IIdentifier>();
      IIdentifier lastDest = null;
      IIdentifier.Type lastType = null;

      for (ISimulationObject object : expired)
      {
        IIdentifier dest = getDestination(object);

        if ((!dest.equals(lastDest) || !dest.getType().equals(lastType))
            && toRemove.size() != 0)
        {
          // send what we've collected so far
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("Sending removal of " + toRemove + " to " + lastDest);

          sensor.send(new ObjectCommandRequest(sid, lastDest,
              IObjectCommand.Type.REMOVED, toRemove));
          toRemove.clear();
        }

        toRemove.add(object.getIdentifier());
        lastDest = dest;
      }

      if (toRemove.size() != 0)
      {
        /*
         * and send what's left
         */
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Sending removal of " + toRemove + " to " + lastDest);

        sensor.send(new ObjectCommandRequest(sid, lastDest,
            IObjectCommand.Type.REMOVED, toRemove));
      }
    }
  }

  protected IIdentifier getDestination(ISimulationObject object)
  {
    if (object instanceof ISensoryObject)
      return ((ISensoryObject) object).getIdentifier().getAgent();

    return IIdentifier.ALL;
  }
}
