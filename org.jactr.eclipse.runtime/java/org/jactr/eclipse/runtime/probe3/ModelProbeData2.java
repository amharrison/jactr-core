package org.jactr.eclipse.runtime.probe3;

/*
 * default logging
 */
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.probe2.StringTable;
import org.jactr.tools.grapher.core.message.ProbeContainerUpdate;
import org.jactr.tools.grapher.core.message.StringTableMessage;
import org.jactr.tools.tracer.transformer.ITransformedEvent;

/**
 * top level model based container.
 * 
 * @author harrison
 */
public class ModelProbeData2
{
  /**
   * Logger definition
   */
  static private final transient Log   LOGGER          = LogFactory
                                                           .getLog(ModelProbeData2.class);

  private final String                 _modelName;

  private final StringTable            _stringTable;

  private final double                 _timeWindow;

  private int                          _maxCapacity    = 0;

  private int                          _normalCapacity = 0;

  private Map<String, IProbeData>      _probeData;

  private Function<String, IProbeData> _probeDataProvider;

  private boolean                      _firstSample    = true;

  public ModelProbeData2(String modelName, int timeWindow,
      StringTable sessionStringTable, Function<String, IProbeData> dataSupplier)
  {
    _modelName = modelName;
    _stringTable = sessionStringTable;
    _timeWindow = timeWindow;
    _probeData = new TreeMap<String, IProbeData>();
    _probeDataProvider = dataSupplier;
  }

  public void clear()
  {
    _probeData.clear();
  }

  public double getTimeWindow()
  {
    return _timeWindow;
  }

  public void process(ITransformedEvent event)
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

  void updateProbeData(ProbeContainerUpdate event)
  {
    double sampleTime = event.getSimulationTime();
    /*
     * if this is our first data, we need to calculate the capacity
     */
    if (_firstSample)
    {
      // double samplingTime = event.getWindowSize();
      _normalCapacity = (int) _timeWindow;
      _maxCapacity = (int) (1.5 * _normalCapacity);

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Capacity: %d / %d", _normalCapacity,
            _maxCapacity));

      _firstSample = false;
    }

    /*
     * for all the updated data..
     */
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

      IProbeData pd = _probeData.get(probeName);
      if (pd == null)
      {
        pd = _probeDataProvider.apply(probeName);
        pd.setBufferSize(_maxCapacity);
        pd.setClipWindow(_normalCapacity);
        _probeData.put(probeName, pd);
      }

      // must be on gui thread..
      // we shift the value..
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("%s.%s added %.4f @ %.3f", _modelName,
            probeName, value, sampleTime));
      pd.addSample(sampleTime, value);
    }

  }

  public Set<String> getProbeNames(Set<String> container)
  {
    if (container == null) container = new TreeSet<String>();
    container.addAll(_probeData.keySet());
    return container;
  }

  public IProbeData getProbeData(String probeName)
  {
    return _probeData.get(probeName);
  }
}
