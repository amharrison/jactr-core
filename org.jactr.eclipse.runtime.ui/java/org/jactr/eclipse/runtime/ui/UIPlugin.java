package org.jactr.eclipse.runtime.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.commonreality.executor.InlineExecutor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jactr.eclipse.core.builder.LaunchConfigurationCleaner;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.ui.marker.MarkerUI;
import org.jactr.eclipse.runtime.ui.misc.LayoutModifier;
import org.jactr.eclipse.runtime.ui.sync.SynchronizationSessionListener;
import org.jactr.eclipse.ui.concurrent.SWTExecutor;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class UIPlugin extends AbstractUIPlugin
{

	public static final String PLUGIN_ID = "org.jactr.eclipse.runtime.ui";
	
  // The shared instance.
  private static UIPlugin plugin;

  // Resource bundle.
  private ResourceBundle  resourceBundle;

  private ImageRegistry   _imageRegistry;

  /**
   * The constructor.
   */
  public UIPlugin()
  {
    super();
    plugin = this;
    _imageRegistry = new ImageRegistry();
    try
    {
      resourceBundle = ResourceBundle
          .getBundle("org.jactr.eclipse.runtime.ui.UiPluginResources");
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

    // RuntimePlugin.getDefault().getRuntimeTraceManager().addListener(
    // new RuntimeLoopDetector());

    RuntimePlugin.getDefault().getSessionManager()
        .addListener(new LayoutModifier(), new SWTExecutor());

    RuntimePlugin
        .getDefault()
        .getSessionManager()
        .addListener(new SynchronizationSessionListener(), InlineExecutor.get());

    initializeRegistry();

    /*
     * add a clean up mechanism
     */
    getWorkbench().addWorkbenchListener(new IWorkbenchListener() {

      public boolean preShutdown(IWorkbench workbench, boolean forced)
      {
        LaunchConfigurationCleaner.clean(null);
        return true;
      }

      public void postShutdown(IWorkbench workbench)
      {

      }
    });

    /*
     * prefetch this one
     */
    MarkerUI.getInstance();
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
  public static UIPlugin getDefault()
  {
    return plugin;
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not
   * found.
   */
  public static String getResourceString(String key)
  {
    ResourceBundle bundle = UIPlugin.getDefault().getResourceBundle();
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

  protected void initializeRegistry()
  {
    _imageRegistry.put(
        "preconflict",
        ImageDescriptor.createFromURL(UIPlugin.getDefault().getBundle()
            .getEntry("/icons/full/basic/preconflict.gif")));
    _imageRegistry.put(
        "postconflict",
        ImageDescriptor.createFromURL(UIPlugin.getDefault().getBundle()
            .getEntry("/icons/full/basic/postconflict.gif")));
  }

  public ImageDescriptor getImageDescriptor(String key)
  {
    return _imageRegistry.getDescriptor(key);
  }
}
