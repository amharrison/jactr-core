package org.jactr.eclipse.ui.wizards.pages;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ToolsExplanationWizardPage extends WizardPage
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ToolsExplanationWizardPage.class);

  private Text                       _description;

  private String                     _extendedDescription;

  public ToolsExplanationWizardPage(String name, String title, String message,
      String details)
  {
    super(name);
    setTitle(title);
    setMessage(message);
    _extendedDescription = details;
  }

  @Override
  public void createControl(Composite parent)
  {
    Composite descriptionGroup = new Composite(parent, SWT.BORDER);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    descriptionGroup.setLayout(layout);
    descriptionGroup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false,
        true,
        2, 2)); // two wide
    _description = new Text(descriptionGroup, SWT.WRAP | SWT.READ_ONLY
        | SWT.MULTI | SWT.VERTICAL);
    _description.setLayoutData(new GridData(GridData.FILL_BOTH));
    _description.setText(_extendedDescription);

    setControl(descriptionGroup);
  }

}
