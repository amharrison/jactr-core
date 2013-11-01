/*
 * Created on Jun 11, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.eclipse.runtime.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.eclipse.ui.generic.prefs.LabelFieldEditor;
import org.jactr.eclipse.ui.generic.prefs.SpacerFieldEditor;

public class RuntimePreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage
{

  public RuntimePreferencePage()
  {
    super(FieldEditorPreferencePage.GRID);
  }

  public void init(IWorkbench workbench)
  {
    setPreferenceStore(RuntimePlugin.getDefault().getPreferenceStore());
  }

  @Override
  protected void createFieldEditors()
  {
    addField(new LabelFieldEditor("Run/Debug", getFieldEditorParent()));

    BooleanFieldEditor bField = new BooleanFieldEditor(
        RuntimePreferences.VERIFY_RUN_PREF, "Verify launch configuration",
        getFieldEditorParent());
    addField(bField);

    IntegerFieldEditor iField = new IntegerFieldEditor(
        RuntimePreferences.NORMAL_START_WAIT_PREF,
        "Launch wait time (seconds)", getFieldEditorParent());
    iField.setTextLimit(2);
    iField.setValidRange(5, 60);
    iField.setEmptyStringAllowed(false);
    addField(iField);

    iField = new IntegerFieldEditor(RuntimePreferences.DEBUG_STACK_PREF,
        "Debug stack trace length (productions)", getFieldEditorParent());
    iField.setTextLimit(3);
    iField.setValidRange(5, 100);
    iField.setEmptyStringAllowed(false);
    addField(iField);

    iField = new IntegerFieldEditor(RuntimePreferences.PLAYBACK_RATE,
        "Playback messages per second", getFieldEditorParent());
    iField.setTextLimit(4);
    iField.setValidRange(10, 10000);
    iField.setEmptyStringAllowed(false);
    addField(iField);

    iField = new IntegerFieldEditor(RuntimePreferences.PLAYBACK_BLOCKSIZE,
        "Playback block size", getFieldEditorParent());
    iField.setTextLimit(4);
    iField.setValidRange(10, 1000);
    iField.setEmptyStringAllowed(false);
    addField(iField);

    addField(new SpacerFieldEditor(getFieldEditorParent()));
    addField(new LabelFieldEditor("Iterative Runs", getFieldEditorParent()));

    iField = new IntegerFieldEditor(
        RuntimePreferences.ITERATIVE_START_WAIT_PREF,
        "Iterative wait time (seconds)", getFieldEditorParent());
    iField.setTextLimit(2);
    iField.setValidRange(5, 60);
    addField(iField);

    bField = new BooleanFieldEditor(RuntimePreferences.ITERATIVE_BEEP_PREF,
        "Beep on complete", getFieldEditorParent());
    addField(bField);

    addField(new SpacerFieldEditor(getFieldEditorParent()));
    addField(new LabelFieldEditor("Perspective Switching",
        getFieldEditorParent()));

    bField = new BooleanFieldEditor(RuntimePreferences.DONT_ASK_RUN_SWITCH,
        "Don't ask to switch to jACT-R Run perspective", getFieldEditorParent());
    addField(bField);

    bField = new BooleanFieldEditor(RuntimePreferences.DONT_ASK_DEBUG_SWITCH,
        "Don't ask to switch to Debug perspective", getFieldEditorParent());
    addField(bField);
  }

}
