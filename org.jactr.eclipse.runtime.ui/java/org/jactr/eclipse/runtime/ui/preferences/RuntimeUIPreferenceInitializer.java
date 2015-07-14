package org.jactr.eclipse.runtime.ui.preferences;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;

public class RuntimeUIPreferenceInitializer extends
    AbstractPreferenceInitializer
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RuntimeUIPreferenceInitializer.class);

  @Override
  public void initializeDefaultPreferences()
  {
    IPreferenceStore prefs = RuntimePlugin.getDefault().getPreferenceStore();
    // Preferences prefs = UIPlugin.getDefault().getPluginPreferences();
    prefs.setDefault(RuntimePreferences.TRANSLATE_TIME, false);
  }

}
