package org.jactr.eclipse.runtime.session.stream;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executor;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.trace.impl.GeneralEventManager;

public abstract class AbstractRollingSessionDataStream<I, T> extends
    AbstractSessionDataStream<I, T> implements ILiveSessionDataStream<T>
{
  /**
   * Logger definition
   */
  static private final transient Log                                               LOGGER = LogFactory
                                                                                              .getLog(AbstractRollingSessionDataStream.class);

  protected final TreeMap<Double, Collection<T>>                                   _data;

  private final double                                                             _windowSize;

  private final double                                                             _maxWindowSize;

  private T                                                                        _lastData;

  protected final GeneralEventManager<ILiveSessionDataStreamListener<T>, Object[]> _eventManager;

  public AbstractRollingSessionDataStream(String streamName,
      ISessionData sessionData, double windowSize)
  {
    super(streamName, sessionData);
    _windowSize = windowSize;
    _maxWindowSize = _windowSize * 1.2;

    _data = new TreeMap<Double, Collection<T>>();

    _eventManager = new GeneralEventManager<ILiveSessionDataStreamListener<T>, Object[]>(
        new GeneralEventManager.INotifier<ILiveSessionDataStreamListener<T>, Object[]>() {

          public void notify(ILiveSessionDataStreamListener<T> listener,
              Object[] event)
          {
            AbstractRollingSessionDataStream.this.notify(listener,
                (Collection) event[0], (Collection) event[1],
                (Collection) event[2]);
          }
        });
  }

  public void addListener(ILiveSessionDataStreamListener<T> listener,
      Executor executor)
  {
    _eventManager.addListener(listener, executor);
  }

  public void removeListener(ILiveSessionDataStreamListener<T> listener)
  {
    _eventManager.removeListener(listener);
  }

  public void clear()
  {
    synchronized (_data)
    {
      _data.clear();
      _lastData = null;
    }
  }

  protected void notify(ILiveSessionDataStreamListener<T> listener,
      Collection<T> added, Collection<T> modified, Collection<T> removed)
  {
    listener.dataChanged(this, added, modified, removed);
  }

  public long getAmountOfDataAvailable(double startTime, double endTime)
  {
    synchronized (_data)
    {
      return _data.subMap(startTime, true, endTime, true).size();
    }
  }

  /**
   * returns all the data from startTime to endTime (inclusive)
   */
  public Collection<T> getData(double startTime, double endTime,
      Collection<T> container)
  {
    if (container == null) container = new ArrayList<T>();
    synchronized (_data)
    {
      for (Collection<T> data : _data.subMap(startTime, true, endTime, true)
          .values())
        container.addAll(data);
    }
    return container;
  }

  public Collection<T> getLatestData(double endTime, Collection<T> container)
  {
    if (container == null) container = new ArrayList<T>();
    synchronized (_data)
    {
      Double key = _data.floorKey(endTime);
      if (key != null) container.addAll(_data.get(key));
    }
    return container;
  }

  public double getStartTime()
  {
    synchronized (_data)
    {
      if (_data.size() == 0) return 0;
      return _data.firstKey();
    }
  }

  public double getEndTime()
  {
    synchronized (_data)
    {
      if (_data.size() == 0) return 0;
      return _data.lastKey();
    }
  }

  public void append(Collection<I> dataToAdd)
  {
    if (dataToAdd.size() == 0) return;

    List<T> added = addData(dataToAdd);

    if (added.size() == 0) return;

    double lastSampleTime = getTime(added.get(added.size() - 1));

    Collection<T> removed = removeExpiredData(lastSampleTime);

    Collection<T> modified = getModifiedData();

    _eventManager.notify(new Object[] { added, modified, removed });
  }

  @Override
  public void append(I data)
  {
    append(Collections.singleton(data));
  }

  /**
   * return that data which we know has been modified. default returns empty
   * 
   * @return
   */
  protected Collection<T> getModifiedData()
  {
    return Collections.EMPTY_LIST;
  }

  /**
   * add the data and return the last sample time
   * 
   * @param toAdd
   * @return
   */
  protected List<T> addData(Collection<I> toAdd)
  {
    List<T> added = new ArrayList<T>(toAdd.size());
    double sampleTime = 0;

    for (I input : toAdd)
      for (T data : toOutputData(input))
      {
        _lastData = data;
        sampleTime = getTime(data);

        synchronized (_data)
        {
          Collection<T> container = _data.get(sampleTime);
          if (container == null)
          {
            container = FastList.newInstance();
            _data.put(sampleTime, container);
          }

          container.add(data);
          added.add(data);
        }
      }

    if (added.size() > 0) added(added);

    return added;
  }

  abstract protected Collection<T> toOutputData(I input);

  protected Collection<T> removeExpiredData(double lastSampleTime)
  {
    Collection<T> removed = Collections.EMPTY_LIST;

    synchronized (_data)
    {
      /*
       * we want to cull from firstKey to (firstKey + lastSampleTime -
       * _maxwindowSize)
       */
      double firstKey = _data.firstKey();
      double delta = lastSampleTime - firstKey;
      if (delta < _maxWindowSize) return removed;

      double endKey = firstKey + _maxWindowSize - _windowSize;
      removed = new ArrayList<T>();

      Iterator<Map.Entry<Double, Collection<T>>> itr = _data
          .headMap(endKey, false).entrySet().iterator();

      while (itr.hasNext())
      {
        /*
         * we just visit each entry once to remove, as opposed to doing a while
         * loop, as that will allow us to do partial removals as necessary.
         */
        Map.Entry<Double, Collection<T>> entry = itr.next();

        Collection<T> container = entry.getValue();

        removeSubset(entry.getKey(), endKey, container, removed);

        if (container.size() == 0)
        {
          itr.remove();
          FastList.recycle((FastList<T>) container);
        }
      }

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format(
            "Window(%.2f) exceeded(%.2f), culled %d records %s", _windowSize,
            lastSampleTime, removed.size(), removed));
    }

    if (removed.size() > 0) removed(removed);

    return removed;
  }

  /**
   * remove a subset (or all) of the T in data. Anything removed from data
   * should be added to removed for proper event notification. By default this
   * copies all of data into removed and clears data.
   * 
   * @param key
   * @param container
   * @return
   */
  protected void removeSubset(double timeOfData, double cullToTime,
      Collection<T> data, Collection<T> removed)
  {
    removed.addAll(data);
    data.clear();
  }

  protected void removed(Collection<T> removed)
  {

  }

  protected void added(Collection<T> added)
  {

  }

  public T getLastData()
  {
    return _lastData;
  }

  abstract protected double getTime(T data);

}
