package org.jactr.eclipse.runtime.probe2;

/*
 * default logging
 */
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.tools.grapher.core.message.ProbeContainerUpdate;
import org.jactr.tools.grapher.core.message.StringTableMessage;
import org.jactr.tools.tracer.transformer.ITransformedEvent;

/**
 * top level model based container.
 * 
 * @author harrison
 */
public class ModelProbeData
{
  /**
   * Logger definition
   */
  static private final transient Log  LOGGER           = LogFactory
                                                           .getLog(ModelProbeData.class);

  private final String                _modelName;

  private final StringTable           _stringTable;

  private final double                _timeWindow;

  private volatile double             _minimumValue    = 0;

  private volatile double             _maximumValue    = 0;

  private int                         _maxCapacity     = 0;

  private int                         _normalCapacity  = 0;

  private final Map<String, double[]> _probeData;

  private double[]                    _samplingTimes;

  private volatile int                _lastSampleIndex = -1;

  public ModelProbeData(String modelName, double timeWindow,
      StringTable sessionStringTable)
  {
    _modelName = modelName;
    _stringTable = sessionStringTable;
    _timeWindow = timeWindow;
    _probeData = new ConcurrentHashMap<String, double[]>();
  }

  public void clear()
  {
    _probeData.clear();
  }

  public double getTimeWindow()
  {
    return _timeWindow;
  }

  void process(ITransformedEvent event)
  {
    if (event instanceof StringTableMessage)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Got string table update : %s",
            ((StringTableMessage) event).getStringTable()));

      _stringTable.update((StringTableMessage) event);
    }
    else
      updateProbeData((ProbeContainerUpdate) event);
  }

  private int computeSampleIndex(ProbeContainerUpdate event)
  {
    double sampleTime = event.getSimulationTime();

    int sampleIndex = 0;

    /*
     * first time through, we need to figure out what the ideal capacity is for
     * the moment.
     */
    if (_samplingTimes == null)
    {
      double samplingTime = event.getWindowSize();
      _normalCapacity = (int) (_timeWindow / samplingTime);
      _maxCapacity = (int) (1.5 * _normalCapacity);

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Capacity: %d / %d", _normalCapacity,
            _maxCapacity));

      /*
       * labels
       */
      _samplingTimes = new double[_maxCapacity];
      for (int i = 0; i < _maxCapacity; i++)
        _samplingTimes[i] = sampleTime + samplingTime * i;

      sampleIndex = 0;
    }
    else
    {
      /*
       * compute sampleIndex
       */
      sampleIndex = (int) ((sampleTime - _samplingTimes[0]) / event
          .getWindowSize());

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("%.2f [%d] [%.2f, %.2f]", sampleTime,
            sampleIndex, _samplingTimes[0], _samplingTimes[1]));
    }

    /*
     * do we need to resize?
     */
    if (sampleIndex >= _maxCapacity)
    {
      /*
       * shift ourselves and trigger a shift for the probe data
       */
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Maximum capacity reached. Shifting");

      double windowSize = event.getWindowSize();
      int blockSize = _maxCapacity - _normalCapacity;
      System.arraycopy(_samplingTimes, blockSize, _samplingTimes, 0,
          _normalCapacity);

      for (int i = _normalCapacity; i < _maxCapacity; i++)
        _samplingTimes[i] = _samplingTimes[i - 1] + windowSize;

      for (double[] probeData : _probeData.values())
      {
        System.arraycopy(probeData, blockSize, probeData, 0, _normalCapacity);
        Arrays.fill(probeData, _normalCapacity, _maxCapacity, Double.NaN);
      }

      sampleIndex = _normalCapacity;
    }

    return sampleIndex;
  }

  void updateProbeData(ProbeContainerUpdate event)
  {
    int sampleIndex = computeSampleIndex(event);


    for (Map.Entry<Long, Number> entry : event.getData().entrySet())
    {
      String probeName = _stringTable.lookUp(entry.getKey());

      if (probeName == null)
      {
        if (LOGGER.isWarnEnabled())
          LOGGER.warn(String.format("Missing data from stringTable [%d]",
              entry.getKey()));
        continue;
      }

      double value = entry.getValue().doubleValue();

      _minimumValue = Math.min(value, _minimumValue);
      _maximumValue = Math.max(value, _maximumValue);

      double[] probeData = _probeData.get(probeName);
      if (probeData == null)
      {
        probeData = new double[_maxCapacity];
        Arrays.fill(probeData, Double.NaN);
        _probeData.put(probeName, probeData);
      }

      probeData[sampleIndex] = value;
    }

    _samplingTimes[sampleIndex] = event.getSimulationTime();
    _lastSampleIndex = sampleIndex;
  }

  public Set<String> getProbeNames(Set<String> container)
  {
    if (container == null) container = new TreeSet<String>();
    container.addAll(_probeData.keySet());
    return container;
  }

  public double getMaximumValue()
  {
    return _maximumValue;
  }

  public double getMinimumValue()
  {
    return _minimumValue;
  }

  public double getStartTime()
  {
    if (_samplingTimes == null) return 0;
    return _samplingTimes[0];
  }

  public double getEndTime()
  {
    if (_samplingTimes == null || _lastSampleIndex == -1) return _timeWindow;
    return _samplingTimes[_lastSampleIndex];
  }

  public double[] getSampleTimes(double[] container)
  {
    if (_samplingTimes == null) return new double[] { 0, 1 };
    return _samplingTimes;
  }

  public double[] getProbeData(String probeName, double[] container)
  {
    double[] pd = _probeData.get(probeName);
    return pd;
  }
}
