package org.jactr.eclipse.core.bundles.descriptors;

/*
 * default logging
 */
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;

public class RuntimeTracerDescriptor extends CommonExtensionDescriptor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RuntimeTracerDescriptor.class);

  private Map<String, String>        _parameters;
  
  public RuntimeTracerDescriptor(String contributor,
      String name, String className, String description,
      Map<String, String> parameters)
  {
    super("org.jactr.tools.tracers", contributor, name, className, description);
    _parameters = new TreeMap<String, String>(parameters);
  }

  public RuntimeTracerDescriptor(IConfigurationElement descriptor)
  {
    super(descriptor);
    _parameters = new TreeMap<String, String>();
  }

  public Map<String, String> getParameters()
  {
    if (getConfigurationElement() != null)
      return getMapOfValues("parameter", "name", "value");
    return _parameters;
  }
}
