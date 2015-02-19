package org.jactr.eclipse.runtime.ui.probe;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.probe3.ModelProbeRuntimeListener;
import org.jactr.eclipse.runtime.trace.IRuntimeTraceListener;
import org.jactr.eclipse.runtime.ui.probe.components.DataProbeProvider;
import org.osgi.framework.BundleContext;

public class ProbeUIPlugin extends AbstractUIPlugin
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProbeUIPlugin.class);

  public ProbeUIPlugin()
  {
  }

  @Override
  public void start(BundleContext context) throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Installing provider"));

    // install our provider for the probe runtime data.
    for (IRuntimeTraceListener listener : RuntimePlugin.getDefault()
        .getRuntimeTraceManager().getListeners(null))
      if (listener instanceof ModelProbeRuntimeListener)
      {
        ((ModelProbeRuntimeListener) listener)
            .setProbeProvider(new DataProbeProvider());
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Installed provider"));
      }

    super.start(context);
  }
}
