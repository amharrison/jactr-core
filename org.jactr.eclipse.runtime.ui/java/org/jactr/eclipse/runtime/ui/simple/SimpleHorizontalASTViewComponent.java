package org.jactr.eclipse.runtime.ui.simple;

/*
 * default logging
 */
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javolution.util.FastList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.ui.content.ACTRLabelProvider;
import org.jactr.eclipse.ui.content.AbstractACTRContentProvider;
import org.jactr.io.antlr3.misc.ASTSupport;

public abstract class SimpleHorizontalASTViewComponent implements
    IOrientedComponent
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SimpleHorizontalASTViewComponent.class);

  static private final String        EDITOR           = "table.Editor";

  static private final String        TREEVIEW         = "table.TreeView";

  static private final String        COLUMN_INDEX     = "table.columnIndex";

  private Table                      _table;

  private TableItem                  _tableItem;

  private Map<String, TableColumn>   _tableColumns    = new TreeMap<String, TableColumn>();

  private ACTRLabelProvider          _labelProvider   = new ACTRLabelProvider();

  private ITreeContentProvider       _contentProvider = new AbstractACTRContentProvider() {

                                                        public void inputChanged(
                                                            Viewer viewer,
                                                            Object oldInput,
                                                            Object newInput)
                                                        {
                                                          if (newInput instanceof CommonTree)
                                                            setRoot((CommonTree) newInput);
                                                        }
                                                      };

  public void dispose()
  {
    _table.dispose();
    _tableColumns.clear();
  }

  public void createPartControl(Composite parent)
  {

    _table = new Table(parent, SWT.SINGLE | SWT.BORDER);
    _table.setHeaderVisible(true);

    _table.addListener(SWT.MeasureItem, new Listener() {

      public void handleEvent(Event event)
      {
        int index = event.index;
        if (index >= _table.getColumnCount()) return;
        TableColumn tc = _table.getColumns()[index];
        TreeViewer viewer = (TreeViewer) tc.getData(TREEVIEW);
        viewer.getTree().pack();
        Rectangle bounds = viewer.getTree().getBounds();
        event.width = bounds.width;
        event.height = bounds.height;
      }
    });

    /*
     * we only have a single item..
     */
    _tableItem = new TableItem(_table, SWT.NONE);

  }

  abstract protected void getAST(ISession session, String modelName,
      double time, boolean isPostConflictResolution,
      Collection<CommonTree> container);


  public void setData(ISession session, String modelName, double time,
      boolean isPostConflictResolution)
  {

    final FastList<CommonTree> trees = FastList.newInstance();
    getAST(session, modelName, time, isPostConflictResolution, trees);

    if (trees.size() == 0)
    {
      noAST();
      FastList.recycle(trees);
      return;
    }

    /*
     * match tableColumns and buffers.
     */
    Set<TableColumn> unprocessed = new HashSet<TableColumn>(
        _tableColumns.values());

    for (CommonTree tree : trees)
    {
      String treeName = ASTSupport.getName(tree);
      TableColumn column = _tableColumns.get(treeName);

      if (column == null)
      {
        column = new TableColumn(_table, SWT.NONE);
        column.setText(treeName);
        column.setMoveable(true);
        column.setResizable(false);
        column.setData(COLUMN_INDEX, _table.getColumnCount() - 1);
        _tableColumns.put(treeName, column);
      }

      unprocessed.remove(column);

      /*
       * now we get the table editor for the column
       */
      TableEditor editor = (TableEditor) column.getData(EDITOR);
      TreeViewer viewer = (TreeViewer) column.getData(TREEVIEW);
      if (editor == null)
      {
        editor = new TableEditor(_table);
        column.setData(EDITOR, editor);
      }

      if (viewer == null)
      {
        viewer = new TreeViewer(_table, SWT.SINGLE);

        viewer.setAutoExpandLevel(4);
        viewer.setContentProvider(_contentProvider);
        viewer.setLabelProvider(_labelProvider);

        column.setData(TREEVIEW, viewer);
      }

      viewer.setInput(tree);
      viewer.getTree().pack();
      Rectangle bounds = viewer.getTree().getBounds();

      int columnIndex = (Integer) column.getData(COLUMN_INDEX);
      column.setWidth(bounds.width);

      editor.minimumWidth = bounds.width;
      editor.minimumHeight = bounds.height;

      // _tableItem.setText(columnIndex, bufferName);

      editor.setEditor(viewer.getControl(), _tableItem, columnIndex);
    }

    /*
     * now we should zero those columns that we didn't process.
     */
    for (TableColumn column : unprocessed)
    {
      TreeViewer viewer = (TreeViewer) column.getData(TREEVIEW);
      viewer.setInput(null);
      column.pack();
    }

    // recylce
    FastList.recycle(trees);

    _table.pack(true);
    // _table.redraw();

  }

  public void noAST()
  {
    for (TableColumn column : _tableColumns.values())
    {
      // TableEditor editor = (TableEditor) column.getData(EDITOR);
      // if (editor.getEditor() != null) editor.getEditor().dispose();
      //
      // editor.setEditor(null);

      TreeViewer viewer = (TreeViewer) column.getData(TREEVIEW);
      viewer.setInput(null);
    }

    _table.pack(true);

  }

  public void setFocus()
  {
    _table.setFocus();

  }

}
