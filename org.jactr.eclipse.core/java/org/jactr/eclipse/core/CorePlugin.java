package org.jactr.eclipse.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.jactr.eclipse.core.parser.ProjectSensitiveParserImportDelegate;
import org.jactr.io.parser.IParserImportDelegate;
import org.jactr.io.parser.ParserImportDelegateFactory;
import org.jactr.io.parser.ParserImportDelegateFactory.IParserImportDelegateFactoryImpl;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class CorePlugin extends Plugin
{

  static private transient final Log LOGGER    = LogFactory
                                                   .getLog(CorePlugin.class);

  static public final String         PLUGIN_ID = "org.jactr.eclipse.core";

  // The shared instance.
  private static CorePlugin          plugin;

  // Resource bundle.
  private ResourceBundle             resourceBundle;

  private BundleContext              _bundleContext;

  /**
   * The constructor.
   */
  public CorePlugin()
  {
    super();
    plugin = this;
    try
    {
      resourceBundle = ResourceBundle
          .getBundle("org.jactr.eclipse.core.CorePluginResources");
    }
    catch (MissingResourceException x)
    {
      resourceBundle = null;
    }
  }

  public BundleContext getBundleContext()
  {
    return _bundleContext;
  }

  /**
   * This method is called upon plug-in activation
   */
  @Override
  public void start(BundleContext context) throws Exception
  {
    super.start(context);
    _bundleContext = context;

    /*
     * we also explicitly activate org.jactr.launching which will handle the
     * basic extension registrations
     */
    org.jactr.launching.Activator.getDefault();

    /*
     * we also need to set the default import delegate
     */

    ParserImportDelegateFactory
        .setFactoryImpl(new IParserImportDelegateFactoryImpl() {

          public IParserImportDelegate createDelegate(Object... params)
          {
            return new ProjectSensitiveParserImportDelegate();
          }
        });
  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop(BundleContext context) throws Exception
  {
    super.stop(context);
  }

  /**
   * Returns the shared instance.
   */
  public static CorePlugin getDefault()
  {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not
   * found.
   */
  public static String getResourceString(String key)
  {
    ResourceBundle bundle = CorePlugin.getDefault().getResourceBundle();
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

  static public void log(IStatus status)
  {
    getDefault().getLog().log(status);
  }

  static public void log(int level, int code, String message, Throwable thrown)
  {
    Status status = new Status(level, "org.jactr.eclipse.core.CorePlugin",
        code, message, thrown);
    log(status);
  }

  static public void debug(String message, Throwable thrown)
  {
    if (thrown instanceof CoreException)
    {
      CoreException ce = (CoreException) thrown;
      log(ce.getStatus());
    }
    else
      log(IStatus.INFO, 0, message, thrown);
    if (LOGGER.isDebugEnabled()) LOGGER.debug(message, thrown);
  }

  static public void debug(String message)
  {
    debug(message, null);
  }

  static public void warn(String message, Throwable thrown)
  {
    if (thrown instanceof CoreException)
    {
      CoreException ce = (CoreException) thrown;
      log(ce.getStatus());
    }
    else
      log(IStatus.WARNING, 0, message, thrown);
    if (LOGGER.isWarnEnabled()) LOGGER.warn(message, thrown);
  }

  static public void warn(String message)
  {
    warn(message, null);
  }

  static public void error(String message, Throwable thrown)
  {
    if (thrown instanceof CoreException)
    {
      CoreException ce = (CoreException) thrown;
      log(ce.getStatus());
    }
    else
      log(IStatus.ERROR, 0, message, thrown);

    if (LOGGER.isErrorEnabled()) LOGGER.error(message, thrown);
  }

  static public void error(String message)
  {
    error(message, null);
  }
}