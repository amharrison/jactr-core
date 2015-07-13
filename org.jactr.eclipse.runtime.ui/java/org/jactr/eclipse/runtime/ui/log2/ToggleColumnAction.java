package org.jactr.eclipse.runtime.ui.log2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Widget;
import org.jactr.eclipse.runtime.ui.UIPlugin;

public class ToggleColumnAction extends Action implements IToggleColumnAction {

	public static final int TIME_COLUMN_WIDTH = 70;
	public static final String PREFERENCE_COLUMN_VISIBILITY_PREFIX = UIPlugin.PLUGIN_ID+".columnVisible";
	
	private final ModelLogView2 view;
	private final List<Table> tables = new ArrayList<>();
	private final List<TableColumn> columns = new ArrayList<>();
	private final String preferenceKey;
	private int lastWidth;
	
	public ToggleColumnAction(ModelLogView2 view, Table table, final TableColumn column) {
		super(column.getText(), IAction.AS_CHECK_BOX);
		super.setChecked(true);
		this.view = view;
		this.tables.add(table);
		this.columns.add(column);
		this.preferenceKey = PREFERENCE_COLUMN_VISIBILITY_PREFIX+"."+this.getClass().getName()+"."+column.getText();
    // this.lastWidth =
    // column.getText().equals("TIME")?TIME_COLUMN_WIDTH:column.getWidth();
    // I appreciate the frustration of resizing time all the time, but fixed is
    // worse if you ever run for long periods
    this.lastWidth = column.getWidth();
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
		if(value == null || value.equals(""))
      // initialize the preference
			UIPlugin.getDefault().getPreferenceStore().setValue(preferenceKey, "true");
    else if(value.equals("true")) {
			// fine
		} else if(value.equals("false")) {
			hideColumns();
			super.setChecked(false);
		}
    else
      throw new IllegalStateException("Invalid visibility preference for key="+preferenceKey+": "+value);
	}
	
	private void saveVisibilityPreference(boolean visibility) {
		UIPlugin.getDefault().getPreferenceStore().setValue(preferenceKey, visibility+"");
	}

	@Override
	public String getToolTipText() {
		return (isChecked()?"Hide":"Show")+" column "+getText();
	}
	
	/* (non-Javadoc)
	 * @see org.jactr.eclipse.runtime.ui.log2.IToggleColumnAction#getColumnText()
	 */
	@Override
	public String getColumnText() {
		return getText();
	}
	
	@Override
	public int getNumberOfColumns() {
		return columns.size();
	}
	
	@Override
	public void add(TableColumn column, Table table) {
		columns.add(column);
		if(!tables.contains(table)) tables.add(table);
		if(isChecked()) showColumn(column);
    else
      hideColumn(column);
	}
	
	@Override
	public void remove(TableColumn column) {
		columns.remove(column);
	}
	
	private <T extends Widget> void invokeForEachAndRemoveDisposed(List<T> list,
			Consumer<T> consumer){
		Iterator<T> iter = list.iterator();
		while(iter.hasNext()) {
			T element = iter.next();
			if(element.isDisposed()) {
				System.err.println("Removing disposed element="+element);
				iter.remove();
			}
      else
        consumer.accept(element);
		};
	}
	
	private void hideColumns() {
		invokeForEachAndRemoveDisposed(columns, this::hideColumn);
	}

	private void hideColumn(TableColumn column) {
		column.setWidth(0);
		column.setResizable(false);
	}

	private void showColumns() {
		invokeForEachAndRemoveDisposed(columns, this::showColumn);
	}

	private void showColumn(TableColumn column) {
    // column.setResizable(!column.getText().equals("TIME"));
    column.setResizable(true);
		column.setWidth(lastWidth);
	}

	@Override
	public void setChecked(boolean checked) {
		if(isChecked() && !checked) {
			// Hide column
			hideColumns();
			saveVisibilityPreference(false);
		} else if(!isChecked() && checked){
			// Show column
			showColumns();
			saveVisibilityPreference(true);
		}
		super.setChecked(checked);
		invokeForEachAndRemoveDisposed(tables, view::adjustColumnSizes);
	}
	
	@Override
  public String toString() {
		return "ToggleColumnAction(text="+getText()+" tables="+tables+" columns="+columns+")";
	}

}
