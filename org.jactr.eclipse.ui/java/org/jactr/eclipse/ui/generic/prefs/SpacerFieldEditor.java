/**
 * lifted directly from http://www.eclipse.org/articles/Article-Field-Editors/field_editors.html
 * 
 */
package org.jactr.eclipse.ui.generic.prefs;

import org.eclipse.swt.widgets.Composite;

/**
 * A field editor for adding space to a preference page.
 */
public class SpacerFieldEditor extends LabelFieldEditor {
	// Implemented as an empty label field editor.
	public SpacerFieldEditor(Composite parent) {
		super("", parent);
	}
}
