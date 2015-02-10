package org.jactr.eclipse.ui.generic.dialog;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * general filterable dialog.
 * 
 * @author harrison
 */
public class NumericInputDialog extends TitleAreaDialog
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(NumericInputDialog.class);

  private String                     _title;

  private String                     _message;

  private Text                       _field;

  double                             _value;

  public NumericInputDialog(Shell parentShell, String title, String message,
      double defaultValue)
  {
    super(parentShell);
    _title = title;
    _message = message;
    _value = defaultValue;
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
    return false;
  }



  @Override
  protected Control createDialogArea(Composite parent)
  {
    Composite area = (Composite) super.createDialogArea(parent);
    Composite container = new Composite(area, SWT.NONE);
    container.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout layout = new GridLayout(2, false);
    container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    container.setLayout(layout);

    createField(container);

    return area;
  }

  private void createField(Composite container)
  {
    Label lbtFirstName = new Label(container, SWT.NONE);
    lbtFirstName.setText("Value");

    GridData dataFirstName = new GridData();
    dataFirstName.grabExcessHorizontalSpace = true;
    dataFirstName.horizontalAlignment = GridData.FILL;

    _field = new Text(container, SWT.BORDER);
    _field.setLayoutData(dataFirstName);
    _field.setText(Double.toString(_value));
  }

  public double getValue()
  {
    return _value;
  }

  @Override
  protected void okPressed()
  {
    try
    {
      _value = Double.parseDouble(_field.getText());
    }
    catch (Exception e)
    {
      _value = 0;
    }

    super.okPressed();
  }

}
