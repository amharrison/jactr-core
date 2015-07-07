package org.jactr.eclipse.runtime.ui.preferences;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.eclipse.ui.generic.prefs.LabelFieldEditor;

public class ProbePreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProbePreferencePage.class);

  public ProbePreferencePage()
  {
    super(FieldEditorPreferencePage.GRID);
  }

  @Override
  protected void createFieldEditors()
  {
    addField(new LabelFieldEditor("Probes", getFieldEditorParent()));

    IntegerFieldEditor iField = new IntegerFieldEditor(
        RuntimePreferences.PROBE_RUNTIME_DATA_WINDOW,
        "Samples of data to retain (depends on probe sample rate, can be memory intensive)",
        getFieldEditorParent());
    iField.setTextLimit(9);
    iField.setValidRange(1, 1000000);
    iField.setEmptyStringAllowed(false);
    addField(iField);


  }

  public void init(IWorkbench workbench)
  {
    setPreferenceStore(RuntimePlugin.getDefault().getPreferenceStore());
  }

}
