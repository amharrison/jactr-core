package org.jactr.eclipse.runtime.session.stream;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.trace.impl.GeneralEventManager;

/**
 * Abstract data stream that assumes temporally increasing insertion times. The
 * stream accepts some input I, creating
 * 
 * @author harrison
 * @param <I>
 * @param <T>
 */
public abstract class AbstractRollingSessionDataStream<I, T> extends
    AbstractSessionDataStream<T> implements ILiveSessionDataStream<T>
{
  /**
   * Logger definition
   */
  static private final transient Log                                               LOGGER    = LogFactory
                                                                                                 .getLog(AbstractRollingSessionDataStream.class);

  // protected final TreeMap<Double, Collection<T>> _data;

  // private final double _windowSize;

  // private final double _maxWindowSize;

  protected final List<TimedData<T>>                                               _data;

  private final int                                                                _hardCapacityLimit;

  private final int                                                                _softCapacityLimit;

  private T                                                                        _lastData;

  private TimedData<T>                                                             _lastTimedData;

  private double                                                                   _lastTime = Double.MIN_VALUE;

  protected final GeneralEventManager<ILiveSessionDataStreamListener<T>, Object[]> _eventManager;

  public AbstractRollingSessionDataStream(String streamName,
      ISessionData sessionData, int windowSize)
  {
    super(streamName, sessionData);
    // _windowSize = windowSize;
    // _maxWindowSize = _windowSize * 1.2;

    _softCapacityLimit = windowSize; // tmp assumption of 50ms
    _hardCapacityLimit = (int) (1.2 * windowSize);

    // _data = new TreeMap<Double, Collection<T>>();
    _data = new ArrayList<TimedData<T>>(_hardCapacityLimit + 1); // so we don't
                                                                 // allocate
                                                                 // anymore

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
      _lastTime = Double.MIN_VALUE;
      _lastTimedData = null;
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
      return ranged(startTime, endTime, true).count();
    }
  }

  /**
   * assuming within a synch
   * 
   * @param startTime
   * @param endTime
   * @return
   */
  private Stream<TimedData<T>> ranged(double startTime, double endTime,
      boolean inclusive)
  {
    if (inclusive)
      return _data.stream().filter(
          (td) -> td.getTime() >= startTime && td.getTime() <= endTime);

    return _data.stream().filter(
        (td) -> td.getTime() >= startTime && td.getTime() < endTime);
  }

  /**
   * returns all the data from startTime to endTime (inclusive)
   */
  public Collection<T> getData(double startTime, double endTime,
      Collection<T> container)
  {
    if (container == null) container = new ArrayList<T>();
    final Collection<T> fContainer = container;

    synchronized (_data)
    {
      ranged(startTime, endTime, true).forEach(
          (td) -> fContainer.addAll(td.getData()));
    }
    return fContainer;
  }

  public Collection<T> getLatestData(double endTime, Collection<T> container)
  {
    if (container == null) container = new ArrayList<T>();
    synchronized (_data)
    {
      // greates that is less than or equal endTime
      ListIterator<TimedData<T>> itr = _data.listIterator(_data.size());
      while (itr.hasPrevious())
      {
        TimedData<T> td = itr.previous();
        double refTime = td.getTime();
        if (refTime <= endTime)
        {
          container.addAll(td.getData());
          break;
        }
      }

    }
    return container;
  }

  public double getStartTime()
  {
    synchronized (_data)
    {
      if (_data.size() > 0) return _data.get(0).getTime();
      return 0;
    }
  }

  public double getEndTime()
  {
    synchronized (_data)
    {
      return _lastTime;
    }
  }

  public void append(Collection<I> dataToAdd)
  {
    if (dataToAdd.size() == 0) return;

    List<T> added = addData(dataToAdd);

    if (added.size() == 0) return;

    Collection<T> removed = removeExpiredData();

    Collection<T> modified = getModifiedData();

    _eventManager.notify(new Object[] { added, modified, removed });
  }

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
        sampleTime = getTime(data);
        TimedData<T> timedData = null;

        synchronized (_data)
        {
          if (sampleTime > _lastTime || _lastTimedData == null)
          {
            timedData = new TimedData<T>(sampleTime, data);
            _lastTimedData = timedData;
            _data.add(_lastTimedData);
          }
          else
            // assuming it's equal..
            _lastTimedData.add(data);

          _lastData = data;
          _lastTime = sampleTime;

          added.add(data);

          // Collection<T> container = _data.get(sampleTime);
          // if (container == null)
          // {
          // container = FastList.newInstance();
          // _data.put(sampleTime, container);
          // }
          //
          // container.add(data);
          // added.add(data);
        }
      }

    if (added.size() > 0) added(added);

    return added;
  }

  abstract protected Collection<T> toOutputData(I input);

  protected Collection<T> removeExpiredData()
  {
    Collection<T> removed = Collections.EMPTY_LIST;

    // synchronized (_data)
    // {
    // /*
    // * we want to cull from firstKey to (firstKey + lastSampleTime -
    // * _maxwindowSize)
    // */
    // double firstKey = _data.firstKey();
    // double delta = lastSampleTime - firstKey;
    // if (delta < _maxWindowSize) return removed;
    //
    // double endKey = firstKey + _maxWindowSize - _windowSize;
    // removed = new ArrayList<T>();
    //
    // Iterator<Map.Entry<Double, Collection<T>>> itr = _data
    // .headMap(endKey, false).entrySet().iterator();
    //
    // while (itr.hasNext())
    // {
    // /*
    // * we just visit each entry once to remove, as opposed to doing a while
    // * loop, as that will allow us to do partial removals as necessary.
    // */
    // Map.Entry<Double, Collection<T>> entry = itr.next();
    //
    // Collection<T> container = entry.getValue();
    //
    // removeSubset(entry.getKey(), endKey, container, removed);
    //
    // if (container.size() == 0)
    // {
    // itr.remove();
    // FastList.recycle((FastList<T>) container);
    // }
    // }
    //
    // if (LOGGER.isDebugEnabled())
    // LOGGER.debug(String.format(
    // "Window(%.2f) exceeded(%.2f), culled %d records %s", _windowSize,
    // lastSampleTime, removed.size(), removed));
    // }

    synchronized (_data)
    {
      if (_data.size() >= _hardCapacityLimit)
        while (_data.size() >= _softCapacityLimit)
        {
          if (removed.size() == 0)
            removed = new ArrayList<T>(_hardCapacityLimit - _softCapacityLimit);
          removed.addAll(_data.remove(0).getData());
        }
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

  private class TimedData<T>
  {
    double        _time;

    Collection<T> _data;

    public TimedData(double time, T data)
    {
      _time = time;
      _data = new ArrayList<T>(2); // bold assumption here.. not much colliding
                                   // data..
      _data.add(data);
    }

    public void add(T data)
    {
      _data.add(data);
    }

    public double getTime()
    {
      return _time;
    }

    public Collection<T> getData()
    {
      return _data;
    }
  }
}
