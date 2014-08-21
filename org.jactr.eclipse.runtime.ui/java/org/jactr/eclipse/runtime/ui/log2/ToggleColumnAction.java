package org.jactr.eclipse.runtime.ui.log2;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.jactr.eclipse.runtime.ui.UIPlugin;

public class ToggleColumnAction extends Action {

	public static final int TIME_COLUMN_WIDTH = 70;
	public static final String PREFERENCE_COLUMN_VISIBILITY_PREFIX = UIPlugin.PLUGIN_ID+".columnVisible";
	
	private final ModelLogView2 view;
	private final Table table;
	private final TableColumn column;
	private final String preferenceKey;
	private int lastWidth;
	
	public ToggleColumnAction(ModelLogView2 view, Table table, final TableColumn column) {
		super(column.getText(), IAction.AS_CHECK_BOX);
		super.setChecked(true);
		this.view = view;
		this.table = table;
		this.column = column;
		this.preferenceKey = PREFERENCE_COLUMN_VISIBILITY_PREFIX+"."+this.getClass().getName()+"."+column.getText();
		this.lastWidth = column.getText().equals("TIME")?TIME_COLUMN_WIDTH:column.getWidth();
		column.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				int width = column.getWidth();
				if(width != 0)
					lastWidth = width;
			}});
		loadVisibility();
	}
	
	private void loadVisibility() {
		String value = UIPlugin.getDefault().getPreferenceStore().getString(preferenceKey);
		if(value == null || value.equals("")) {
			// initialize the preference
			UIPlugin.getDefault().getPreferenceStore().setValue(preferenceKey, "true");
		} else if(value.equals("true")) {
			// fine
		} else if(value.equals("false")) {
			hideColumn();
			super.setChecked(false);
		} else {
			throw new IllegalStateException("Invalid visibility preference for key="+preferenceKey+": "+value);
		}
	}
	
	private void saveVisibilityPreference(boolean visibility) {
		UIPlugin.getDefault().getPreferenceStore().setValue(preferenceKey, visibility+"");
	}

	@Override
	public String getToolTipText() {
		return (isChecked()?"Hide":"Show")+" column "+getText();
	}
	
	private void hideColumn() {
		column.setWidth(0);
		column.setResizable(false);
	}

	@Override
	public void setChecked(boolean checked) {
		if(isChecked() && !checked) {
			// Hide column
			hideColumn();
			saveVisibilityPreference(false);
		} else if(!isChecked() && checked){
			// Show column
			column.setResizable(!column.getText().equals("TIME"));
			column.setWidth(lastWidth);
			saveVisibilityPreference(true);
		}
		super.setChecked(checked);
		view.adjustColumnSizes(table);
	}

}
