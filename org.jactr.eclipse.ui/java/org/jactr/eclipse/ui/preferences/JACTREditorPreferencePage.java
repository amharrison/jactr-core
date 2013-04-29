package org.jactr.eclipse.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jactr.eclipse.ui.UIPlugin;

public class JACTREditorPreferencePage extends PreferencePage implements
    IWorkbenchPreferencePage
{
  BooleanFieldEditor _completeStrings;

  BooleanFieldEditor _completeComments;

  FieldEditor[]      _all = new FieldEditor[4];

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

    _completeStrings = new BooleanFieldEditor(
        UIPreferences.ENABLE_STRING_COMPLETION, "Enable string completion",
        BooleanFieldEditor.SEPARATE_LABEL, reconcilerGroup);
    _all[0] = _completeStrings;
    _completeStrings.setPreferenceStore(getPreferenceStore());

    _completeComments = new BooleanFieldEditor(
        UIPreferences.ENABLE_COMMENT_COMPLETION, "Enable comment completion",
        BooleanFieldEditor.SEPARATE_LABEL, reconcilerGroup);
    _all[1] = _completeComments;
    _completeComments.setPreferenceStore(getPreferenceStore());

    BooleanFieldEditor bfe = new BooleanFieldEditor(
        UIPreferences.ENABLE_CARRET_COMPLETION,
        "Enable carret '<>' completion", BooleanFieldEditor.SEPARATE_LABEL,
        reconcilerGroup);
    _all[2] = bfe;
    bfe.setPreferenceStore(getPreferenceStore());

    bfe = new BooleanFieldEditor(
        UIPreferences.ENABLE_CLOSE_COMPLETION, "Enable close tag completion",
        BooleanFieldEditor.SEPARATE_LABEL, reconcilerGroup);
    _all[3] = bfe;
    bfe.setPreferenceStore(getPreferenceStore());

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
