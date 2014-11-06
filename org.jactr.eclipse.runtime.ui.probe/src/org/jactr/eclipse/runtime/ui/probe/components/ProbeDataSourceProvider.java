package org.jactr.eclipse.runtime.ui.probe.components;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.visualization.xygraph.dataprovider.ClippedCircularBufferDataProvider;
import org.jactr.eclipse.runtime.probe3.IProbeData;

public class ProbeDataSourceProvider extends ClippedCircularBufferDataProvider
    implements IProbeData
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProbeDataSourceProvider.class);

  private final String               _probeName;

  public ProbeDataSourceProvider(String probeName)
  {
    super(true, 1000, 100);
    _probeName = probeName;
    setUpdateDelay(0);
  }

  @Override
  public String getName()
  {
    return _probeName;
  }

  @Override
  public void addSample(double time, double value)
  {
    setCurrentYData(value, (long) (time * 1000));
  }

  @Override
  public void setClipWindow(int window)
  {
    setClippingWindow(window);
  }

}
