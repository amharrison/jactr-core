package org.jactr.eclipse.ui.generic.dialog;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jactr.eclipse.ui.UIPlugin;

/**
 * general filterable dialog.
 * 
 * @author harrison
 */
public class ListSelectionDialog extends TitleAreaDialog
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ListSelectionDialog.class);

  private ILabelProvider             _labelProvider;

  private IStructuredContentProvider _contentProvider;

  private Object                     _input;

  private CheckboxTreeViewer         _viewer;

  private String                     _title;

  private String                     _message;

  private Collection<Object>         _checkedItems;

  public ListSelectionDialog(Shell parentShell, String title, String message,
      Object input, IStructuredContentProvider contentProvider,
      ILabelProvider labelProvider)
  {
    super(parentShell);
    _title = title;
    _message = message;
    _input = input;
    _contentProvider = contentProvider;
    _labelProvider = labelProvider;
  }

  @Override
  public void create()
  {
    super.create();
    setTitle(_title);
    setMessage(_message, IMessageProvider.INFORMATION);
  }

  @Override
  protected boolean isResizable()
  {
    return true;
  }

  protected CheckboxTreeViewer getViewer()
  {
    return _viewer;
  }

  @Override
  protected Control createDialogArea(Composite parent)
  {
    Composite area = (Composite) super.createDialogArea(parent);
    Composite container = new Composite(area, SWT.NONE);
    container.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout layout = new GridLayout(1, false);
    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    container.setLayout(layout);

    _viewer = new CheckboxTreeViewer(container);
    _viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
    _viewer.setContentProvider(_contentProvider);
    _viewer.setLabelProvider(_labelProvider);
    _viewer.setComparator(new ViewerComparator());
    _viewer.setInput(_input); // pass a non-null that will be ignored

    restoreState();

    return area;
  }

  public Object[] getCheckedItems()
  {
    return _checkedItems.toArray();
  }

  @Override
  protected void okPressed()
  {
    _checkedItems = new ArrayList();
    for (Object checked : _viewer.getCheckedElements())
      _checkedItems.add(checked);

    saveState();

    super.okPressed();
  }

  private void restoreState()
  {
    String checked = UIPlugin.getDefault().getPreferenceStore()
        .getString(getClass().getName() + ".checked");

    FastList<String> checkedNames = FastList.newInstance();

    for (String item : checked.split(","))
      checkedNames.add(item);

    for (Object element : _contentProvider.getElements(_input))
    {
      String label = _labelProvider.getText(element);
      _viewer.setChecked(element, checkedNames.contains(label));
    }
    FastList.recycle(checkedNames);
  }

  private void saveState()
  {
    FastList<String> checkedNames = FastList.newInstance();

    for (Object checked : _checkedItems)
      checkedNames.add(_labelProvider.getText(checked));

    String all = checkedNames.stream().collect(Collectors.joining(","));
    UIPlugin.getDefault().getPreferenceStore()
        .setValue(getClass().getName() + ".checked", all);

    FastList.recycle(checkedNames);
  }

}
