package org.commonreality.sensors.base.impl;

/*
 * default logging
 */
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.sensors.base.IObjectProcessor;
import org.commonreality.sensors.base.PerceptManager;

public abstract class AbstractObjectProcessor implements IObjectProcessor<DefaultObjectKey>
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractObjectProcessor.class);

  public void configure(Map<String, String> options)
  {
    // noop
    
  }


  public void installed(PerceptManager manager)
  {
    // noop
    
  }


  public void uninstalled(PerceptManager manager)
  {
    // noop
    
  }

}
