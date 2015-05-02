package org.jactr.eclipse.runtime.ui.preferences;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.eclipse.ui.generic.prefs.LabelFieldEditor;

public class LogPreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(LogPreferencePage.class);

  public LogPreferencePage()
  {
    super(FieldEditorPreferencePage.GRID);
  }

  @Override
  protected void createFieldEditors()
  {
    addField(new LabelFieldEditor("Logging", getFieldEditorParent()));

    IntegerFieldEditor iField = new IntegerFieldEditor(
        RuntimePreferences.RUNTIME_DATA_WINDOW,
        "Seconds of data to retain (memory intensive)",
        getFieldEditorParent());
    iField.setTextLimit(5);
    iField.setValidRange(1, 600);
    iField.setEmptyStringAllowed(false);
    addField(iField);

    FontFieldEditor fField = new FontFieldEditor("log.font", "Log Font",
        getFieldEditorParent());
    addField(fField);

  }

  public void init(IWorkbench workbench)
  {
    setPreferenceStore(RuntimePlugin.getDefault().getPreferenceStore());
  }

}
