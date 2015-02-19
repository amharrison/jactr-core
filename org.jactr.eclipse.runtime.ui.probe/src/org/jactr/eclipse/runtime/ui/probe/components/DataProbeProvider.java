package org.jactr.eclipse.runtime.ui.probe.components;

/*
 * default logging
 */
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.probe3.IProbeData;

public class DataProbeProvider implements Function<String, IProbeData>
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(DataProbeProvider.class);


  public DataProbeProvider()
  {

  }

  public IProbeData apply(String t)
  {
    return new ProbeDataSourceProvider(t);
  }


}
