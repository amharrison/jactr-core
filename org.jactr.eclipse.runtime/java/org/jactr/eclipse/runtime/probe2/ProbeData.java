package org.jactr.eclipse.runtime.probe2;

/*
 * default logging
 */
import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.DoubleList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProbeData
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProbeData.class);

  private final String               _probeName;

  private ArrayDoubleList            _data;

  private int                        _maximumCapacity;

  private int                        _shiftCapacity;

  public ProbeData(String probeName, int normalCapacity, int maxCapacity,
      int fillTo)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("new PD %s, %d, %d, %d", probeName,
          normalCapacity, maxCapacity, fillTo));

    _probeName = probeName;
    _maximumCapacity = maxCapacity;
    _shiftCapacity = normalCapacity;
    _data = new ArrayDoubleList(maxCapacity);

    for (int i = 0; i < fillTo; i++)
      _data.add(Double.NaN);
  }

  public String getName()
  {
    return _probeName;
  }

  synchronized public void append(double value)
  {
    _data.add(value);
  }

  synchronized public void shift()
  {
    DoubleList view = _data.subList(0, _maximumCapacity - _shiftCapacity);
    view.clear();
  }

  synchronized public double[] getData(double[] container)
  {
    if (container == null) container = new double[_data.size()];

    return _data.toArray(container);
  }
}
