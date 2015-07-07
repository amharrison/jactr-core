package org.jactr.eclipse.runtime;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.jactr.core.concurrent.ExecutorServices;
import org.jactr.eclipse.runtime.marker.MarkerIndexSessionListener;
import org.jactr.eclipse.runtime.marker.MarkerRuntimeTraceListener;
import org.jactr.eclipse.runtime.production2.ConflictResolutionRuntimeTraceListener;
import org.jactr.eclipse.runtime.session.manager.ISessionManager;
import org.jactr.eclipse.runtime.session.manager.internal.SessionManager;
import org.jactr.eclipse.runtime.trace.RuntimeTraceManager;
import org.jactr.eclipse.runtime.visual.VisualTraceCenter;
import org.jactr.eclipse.ui.concurrent.SWTExecutor;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class RuntimePlugin extends Plugin
{

  static public final String    PLUGIN_ID = "org.jactr.eclipse.runtime.RuntimePlugin";

  // The shared instance.
  private static RuntimePlugin  plugin;

  // Resource bundle.
  private ResourceBundle        resourceBundle;

  /**
   * Storage for preferences.
   */
  private ScopedPreferenceStore preferenceStore;

  private RuntimeTraceManager   _runtimeTraceManager;

  private SessionManager        _sessionManager;

  /**
   * The constructor.
   */
  public RuntimePlugin()
  {
    super();
    try
    {
      resourceBundle = ResourceBundle
          .getBundle("org.jactr.eclipse.runtime.RuntimePluginResources");
    }
    catch (MissingResourceException x)
    {
      resourceBundle = null;
    }
  }

  /**
   * This method is called upon plug-in activation
   */
  @Override
  public void start(BundleContext context) throws Exception
  {
    super.start(context);

    _sessionManager = new SessionManager();
    _sessionManager.addListener(new MarkerIndexSessionListener(),
        ExecutorServices.INLINE_EXECUTOR);

    _runtimeTraceManager = new RuntimeTraceManager();

    plugin = this;

    /*
     * always listen for log
     */
    // _runtimeTraceManager.addListener(ModelLoggingCenter.get()
    // .getRuntimeListener());
    // _runtimeTraceManager.addListener(new BufferTraceListener());
    _runtimeTraceManager.addListener(VisualTraceCenter.get()
        .getRuntimeListener());
    // _runtimeTraceManager
    // .addListener(ProbeDataManager.get()
    // .getRuntimeListener());
    // _runtimeTraceManager.addListener(new ProductionTraceListener());

    _runtimeTraceManager
        .addListener(new org.jactr.eclipse.runtime.session.data.RuntimeTraceListener());
    _runtimeTraceManager
        .addListener(new org.jactr.eclipse.runtime.log2.LogRuntimeTraceListener());
    _runtimeTraceManager
        .addListener(new org.jactr.eclipse.runtime.buffer2.BufferRuntimeTraceListener());
    _runtimeTraceManager
        .addListener(new ConflictResolutionRuntimeTraceListener());
    // _runtimeTraceManager
    // .addListener(new
    // org.jactr.eclipse.runtime.probe2.ModelProbeRuntimeListener());

    // must be run on SWT thread for the probe data storage
    _runtimeTraceManager.addListener(
        new org.jactr.eclipse.runtime.probe3.ModelProbeRuntimeListener(),
        new SWTExecutor());

    _runtimeTraceManager.addListener(new MarkerRuntimeTraceListener());


  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop(BundleContext context) throws Exception
  {
    super.stop(context);
    if (preferenceStore != null) preferenceStore.save();
    _runtimeTraceManager.clear();
    _runtimeTraceManager = null;
    plugin = null;
  }

  public RuntimeTraceManager getRuntimeTraceManager()
  {
    return _runtimeTraceManager;
  }

  public ISessionManager getSessionManager()
  {
    return _sessionManager;
  }

  /**
   * Returns the shared instance.
   */
  public static RuntimePlugin getDefault()
  {
    return plugin;
  }

  static public void info(String message)
  {
    info(message, null);
  }

  static public void info(String message, Throwable thrown)
  {
    log(IStatus.INFO, message, thrown);
  }

  static public void warn(String message)
  {
    warn(message, null);
  }

  static public void warn(String message, Throwable thrown)
  {
    log(IStatus.WARNING, message, thrown);
  }

  static public void error(String message)
  {
    error(message, null);
  }

  static public void error(String message, Throwable thrown)
  {
    log(IStatus.ERROR, message, thrown);
  }

  static public void log(int severity, String message, Throwable thrown)
  {
    getDefault().getLog().log(new Status(severity, PLUGIN_ID, message, thrown));
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not
   * found.
   */
  public static String getResourceString(String key)
  {
    ResourceBundle bundle = RuntimePlugin.getDefault().getResourceBundle();
    try
    {
      return bundle != null ? bundle.getString(key) : key;
    }
    catch (MissingResourceException e)
    {
      return key;
    }
  }

  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle()
  {
    return resourceBundle;
  }

  public IPreferenceStore getPreferenceStore()
  {
    // Create the preference store lazily.
    if (preferenceStore == null)
      preferenceStore = new ScopedPreferenceStore(new InstanceScope(),
          getBundle().getSymbolicName());
    return preferenceStore;
  }
}
