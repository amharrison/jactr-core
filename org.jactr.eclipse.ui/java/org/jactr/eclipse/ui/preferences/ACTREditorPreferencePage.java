package org.jactr.eclipse.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jactr.eclipse.ui.UIPlugin;

public class ACTREditorPreferencePage extends PreferencePage implements
    IWorkbenchPreferencePage
{
  BooleanFieldEditor _reconcilerEnabled;

  BooleanFieldEditor _foldingEnabled;

  BooleanFieldEditor _hoverEnabled;

  BooleanFieldEditor _hyperlinkEnabled;

  BooleanFieldEditor _assistEnabled;

  BooleanFieldEditor _formatOnSaveEnabled;
  
  BooleanFieldEditor _autoTemplateEnabled;

  FieldEditor[]      _all = new FieldEditor[7];

  @Override
  protected Control createContents(Composite parent)
  {
    Composite entryTable = new Composite(parent, SWT.NULL);

    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.grabExcessHorizontalSpace = true;
    entryTable.setLayoutData(data);

    GridLayout layout = new GridLayout();
    entryTable.setLayout(layout);

    final Composite reconcilerGroup = new Composite(entryTable, SWT.NONE);
    reconcilerGroup.setLayout(new GridLayout());
    reconcilerGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    _reconcilerEnabled = new BooleanFieldEditor(
        UIPreferences.ENABLE_RECONCILER_PREF, "Enable continuous compilation",
        BooleanFieldEditor.SEPARATE_LABEL, reconcilerGroup);
    _all[0] = _reconcilerEnabled;
    _reconcilerEnabled.setPreferenceStore(getPreferenceStore());
    _reconcilerEnabled.setPropertyChangeListener(new IPropertyChangeListener() {

      public void propertyChange(PropertyChangeEvent event)
      {
        if (!_reconcilerEnabled.getBooleanValue())
        {
          IPreferenceStore pref = getPreferenceStore();
          pref.setValue(UIPreferences.ENABLE_ASSIST_PREF, false);
          pref.setValue(UIPreferences.ENABLE_FOLDING_PREF, false);
          pref.setValue(UIPreferences.ENABLE_HOVER_PREF, false);
          pref.setValue(UIPreferences.ENABLE_HYPERLINK_PREF, false);
        }

        _assistEnabled.setEnabled(_reconcilerEnabled.getBooleanValue(),
            reconcilerGroup);
        _foldingEnabled.setEnabled(_reconcilerEnabled.getBooleanValue(),
            reconcilerGroup);
        _hoverEnabled.setEnabled(_reconcilerEnabled.getBooleanValue(),
            reconcilerGroup);
        _hyperlinkEnabled.setEnabled(_reconcilerEnabled.getBooleanValue(),
            reconcilerGroup);
      }
    });

    _assistEnabled = new BooleanFieldEditor(UIPreferences.ENABLE_ASSIST_PREF,
        "Enable content assist", BooleanFieldEditor.SEPARATE_LABEL,
        reconcilerGroup);
    _all[1] = _assistEnabled;
    _assistEnabled.setPreferenceStore(getPreferenceStore());

    _foldingEnabled = new BooleanFieldEditor(UIPreferences.ENABLE_FOLDING_PREF,
        "Enable folding", BooleanFieldEditor.SEPARATE_LABEL, reconcilerGroup);
    _all[2] = _foldingEnabled;
    _foldingEnabled.setPreferenceStore(getPreferenceStore());

    _hoverEnabled = new BooleanFieldEditor(UIPreferences.ENABLE_HOVER_PREF,
        "Enable text hovers", BooleanFieldEditor.SEPARATE_LABEL,
        reconcilerGroup);
    _all[3] = _hoverEnabled;
    _hoverEnabled.setPreferenceStore(getPreferenceStore());

    _hyperlinkEnabled = new BooleanFieldEditor(
        UIPreferences.ENABLE_HYPERLINK_PREF, "Enable hyperlinks",
        BooleanFieldEditor.SEPARATE_LABEL, reconcilerGroup);
    _all[4] = _hyperlinkEnabled;
    _hyperlinkEnabled.setPreferenceStore(getPreferenceStore());
    
    _autoTemplateEnabled = new BooleanFieldEditor(
        UIPreferences.ENABLE_AUTO_ACTIVATE_PREF, "Auto-propose templates",
        BooleanFieldEditor.SEPARATE_LABEL, reconcilerGroup);
    _all[5] = _autoTemplateEnabled;
    _autoTemplateEnabled.setPreferenceStore(getPreferenceStore());

    _formatOnSaveEnabled = new BooleanFieldEditor(
        UIPreferences.ENABLE_FORMAT_PREF, "Format on save",
        BooleanFieldEditor.SEPARATE_LABEL, reconcilerGroup);
    _all[6] = _formatOnSaveEnabled;
    _formatOnSaveEnabled.setPreferenceStore(getPreferenceStore());

    for (FieldEditor editor : _all)
      editor.load();

    return entryTable;
  }

  @Override
  protected void performDefaults()
  {
    for (FieldEditor editor : _all)
      editor.loadDefault();
  }

  @Override
  public boolean performOk()
  {
    for (FieldEditor editor : _all)
      editor.store();
    return super.performOk();
  }

  public void init(IWorkbench workbench)
  {
    setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
  }

}
