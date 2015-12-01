package org.jactr.eclipse.runtime.probe3.extract;

/*
 * default logging
 */
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFolder;
import org.jactr.eclipse.runtime.probe3.IProbeData;

public class OutputProbeDataProvider implements Function<String, IProbeData>
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(OutputProbeDataProvider.class);

  private final IFolder              _outputDirectory;


  public OutputProbeDataProvider(IFolder output)
  {
    _outputDirectory = output;
  }

  public IProbeData apply(String t)
  {
    return new OutputProbeData(t, _outputDirectory);
  }


}
