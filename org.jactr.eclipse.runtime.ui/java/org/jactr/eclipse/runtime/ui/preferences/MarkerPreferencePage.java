package org.jactr.eclipse.runtime.ui.preferences;

/*
 * default logging
 */
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jactr.eclipse.runtime.ui.UIPlugin;
import org.jactr.eclipse.runtime.ui.marker.MarkerUI;
import org.jactr.eclipse.ui.generic.prefs.LabelFieldEditor;

public class MarkerPreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(MarkerPreferencePage.class);

  public MarkerPreferencePage()
  {
    super(FieldEditorPreferencePage.GRID);
  }

  public void init(IWorkbench workbench)
  {
    setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
  }

  @Override
  protected void createFieldEditors()
  {
    /*
     * snag all the encountered types of markers
     */
    Set<String> allTypes = MarkerUI.getStoredMarkerTypes();

    /*
     * this is fine for now, but the future will want to see these in a list w/
     * a remove button
     */
    addField(new LabelFieldEditor("Knwon marker types:", getFieldEditorParent()));
    for (String type : allTypes)
    {
      String pref = type + ".markerColor";
      ColorFieldEditor cfe = new ColorFieldEditor(pref, type,
          getFieldEditorParent());
      addField(cfe);
    }

  }

}
