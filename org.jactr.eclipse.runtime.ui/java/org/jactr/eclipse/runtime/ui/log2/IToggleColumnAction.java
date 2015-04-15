package org.jactr.eclipse.runtime.ui.log2;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public interface IToggleColumnAction extends IAction {

	public abstract String getColumnText();
	public int getNumberOfColumns();
	public void add(TableColumn column, Table table);
	public void remove(TableColumn column);
}