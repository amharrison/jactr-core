package org.jactr.eclipse.ui.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.generic.prefs.LabelFieldEditor;
import org.jactr.eclipse.ui.generic.prefs.SpacerFieldEditor;

public class ACTREditorColorPreferencePage extends PreferencePage implements
    IWorkbenchPreferencePage
{

  private ColorFieldEditor    colorEditorKeyword;

  private ColorFieldEditor    colorEditorString;

  private ColorFieldEditor    colorEditorComment;

  private final FieldEditor[] _all = new FieldEditor[3];

  @Override
  protected Control createContents(Composite parent)
  {

    Composite entryTable = new Composite(parent, SWT.NULL);

    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.grabExcessHorizontalSpace = true;
    entryTable.setLayoutData(data);

    GridLayout layout = new GridLayout();
    entryTable.setLayout(layout);

    Composite colorComposite = new Composite(entryTable, SWT.NONE);

    colorComposite.setLayout(new GridLayout());

    colorComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    colorEditorKeyword = new ColorFieldEditor(UIPreferences.KEYWORD_COLOR_PREF,
        "Keyword Highlight Color", colorComposite);
    _all[0] = colorEditorKeyword;
    colorEditorComment = new ColorFieldEditor(UIPreferences.COMMENT_COLOR_PREF,
        "Comment Highlight Color", colorComposite);
    _all[1] = colorEditorComment;
    colorEditorString = new ColorFieldEditor(UIPreferences.STRING_COLOR_PREF,
        "String Highlight Color", colorComposite);
    _all[2] = colorEditorString;

    for (FieldEditor editor : _all)
    {
      editor.setPreferenceStore(getPreferenceStore());
      editor.load();
    }
    
    new SpacerFieldEditor(colorComposite);
    // add a little comment
    new LabelFieldEditor("(applies to new editors)", colorComposite);

    return entryTable;
  }

  public void init(IWorkbench workbench)
  {
    setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
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

  public IPreferenceStore getKeywordColor()
  {
    return colorEditorKeyword.getPreferenceStore();
  }

  public IPreferenceStore getStringColor()
  {
    return colorEditorString.getPreferenceStore();
  }

  public IPreferenceStore getCommentColor()
  {
    return colorEditorComment.getPreferenceStore();
  }

}
