package org.jactr.eclipse.ui;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jactr.eclipse.core.builder.LaunchConfigurationCleaner;
import org.jactr.eclipse.ui.preferences.UIPreferences;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class UIPlugin extends AbstractUIPlugin
{

  /**
   * Logger definition
   */

  static private final transient Log      LOGGER = LogFactory
                                                     .getLog(UIPlugin.class);

  static public final String              ID     = UIPlugin.class.getName();

  // The shared instance.
  private static UIPlugin                 plugin;

  // Resource bundle.
  private ResourceBundle                  resourceBundle;

  /** The template store. */
  private TemplateStore                   fStore;

  /** The context type registry. */
  private ContributionContextTypeRegistry fRegistry;
  
  private IResourceChangeListener         _resourceListener = new IResourceChangeListener() {

                                                              public void resourceChanged(
                                                                  IResourceChangeEvent event)
                                                              {
                                                                IResource resource = event
                                                                    .getResource();
                                                                /*
                                                                 * a manifest
                                                                 * has changed,
                                                                 * we should
                                                                 * probably do a
                                                                 * clean on the
                                                                 * containing
                                                                 * project.
                                                                 */
                                                                if (resource != null
                                                                    && resource
                                                                    .getFileExtension()
                                                                    .equalsIgnoreCase(
                                                                        "MF"))
                                                                  LaunchConfigurationCleaner
                                                                      .clean(resource
                                                                          .getProject());

                                                              }
                                                            };
  /**
   * The constructor.
   */
  public UIPlugin()
  {
    super();
    plugin = this;
    try
    {
      resourceBundle = ResourceBundle
          .getBundle("org.jactr.eclipse.ui.UIPluginResources");
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
    ResourcesPlugin.getWorkspace().addResourceChangeListener(_resourceListener,
        IResourceChangeEvent.POST_CHANGE);
  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop(BundleContext context) throws Exception
  {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(
        _resourceListener);
    super.stop(context);
  }

  /**
   * Returns the shared instance.
   */
  public static UIPlugin getDefault()
  {
    return plugin;
  }

  public static void log(Throwable thrown)
  {
    log("Unknown cause", thrown);
  }

  public static void log(String message)
  {
    log(message, null);
  }

  public static void log(String message, Throwable thrown)
  {
    if (thrown != null)
      log(IStatus.ERROR, message, thrown);
    else
      log(IStatus.OK, message, thrown);
  }

  public static void log(int code, String message, Throwable thrown)
  {
    log(new Status(code, "org.jactr.eclipse.ui.UIPlugin", 0, message, thrown));
  }

  public static void log(IStatus status)
  {
    getDefault().getLog().log(status);
  }

  public static Display getStandardDisplay()
  {
    Display display = Display.getCurrent();
    if (display == null) display = Display.getDefault();
    return display;
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

  /**
   * Returns this plug-in's template store.
   * 
   * @return the template store of this plug-in instance
   */
  public TemplateStore getTemplateStore()
  {
    if (fStore == null)
    {
      fStore = new ContributionTemplateStore(getContextTypeRegistry(),
          getPreferenceStore(), UIPreferences.CUSTOM_TEMPLATES_PREF);
      try
      {
        fStore.load();
      }
      catch (IOException e)
      {
        log(new Status(IStatus.ERROR,
            "org.jactr.eclipse.ui.UIPlugin", IStatus.OK, "", e)); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    return fStore;
  }

  /**
   * Returns this plug-in's context type registry.
   * 
   * @return the context type registry for this plug-in instance
   */
  public ContributionContextTypeRegistry getContextTypeRegistry()
  {
    if (fRegistry == null) // create an configure the contexts available in the template editor
    fRegistry = new ContributionContextTypeRegistry();
//      fRegistry.addContextType(ACTRContextType.JACTR_CONTEXT_TYPE);
//      fRegistry.addContextType(LispContextType.LISP_CONTEXT_TYPE);
    return fRegistry;
  }

}