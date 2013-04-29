package org.jactr.eclipse.ui.wizards.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.jactr.eclipse.core.bundles.descriptors.ModuleDescriptor;
import org.jactr.eclipse.core.bundles.registry.ModuleRegistry;
import org.jactr.eclipse.core.project.ACTRProjectUtils;
import org.jactr.eclipse.ui.messages.JACTRMessages;
import org.jactr.eclipse.ui.renders.ACTRProjectLabelProvider;
import org.jactr.io.parser.ModelParserFactory;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class NewModelWizardPage1 extends WizardPage
{

  /**
   * Logger definition
   */

  static private final transient Log    LOGGER = LogFactory
                                                   .getLog(NewModelWizardPage1.class);

  private Text                          _projectText;

  private Text                          _fileText;

  private Text                          _descText;

  private final ISelection              _selection;

  private IProject                      _project;

  private CheckboxTableViewer           _moduleViewer;

  private Collection<String>            _validExtensions;

  private Map<String, ModuleDescriptor> _moduleMap;

  /**
   * Constructor for SampleNewWizardPage.
   * 
   * @param pageName
   */
  public NewModelWizardPage1(ISelection selection)
  {
    super("wizardPage");
    setTitle("New ACT-R Model");
    setDescription("You can create a specifically named model and enable which modules it should use");
    this._selection = selection;
  }

  /**
   * @see IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent)
  {
    Composite container = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    // layout.numColumns = 3;
    layout.verticalSpacing = 9;
    Label label = new Label(container, SWT.NULL);
    label.setText("&Project:");

    Composite projCont = new Composite(container, SWT.NULL);
    layout = new GridLayout();
    layout.numColumns = 3;
    projCont.setLayout(layout);
    projCont.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    _projectText = new Text(projCont, SWT.BORDER | SWT.SINGLE);
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    _projectText.setLayoutData(gd);
    _projectText.addModifyListener(new ModifyListener() {

      public void modifyText(ModifyEvent e)
      {
        dialogChanged();
      }
    });

    Button button = new Button(projCont, SWT.PUSH);
    button.setText("Browse...");
    button.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e)
      {
        handleBrowse();
      }
    });
    label = new Label(container, SWT.NULL);
    label.setText("&File name:");

    _fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
    gd = new GridData(GridData.FILL_HORIZONTAL);
    _fileText.setLayoutData(gd);
    _fileText.addModifyListener(new ModifyListener() {

      public void modifyText(ModifyEvent e)
      {
        dialogChanged();
      }
    });

    // lets create a sash
    SashForm sashForm = new SashForm(container, SWT.HORIZONTAL);
    sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

    Composite extensionSide = new Composite(sashForm, SWT.NULL);
    layout = new GridLayout();
    layout.marginWidth = layout.marginHeight = 0;
    extensionSide.setLayout(layout);

    label = new Label(extensionSide, SWT.NULL);
    label.setText(JACTRMessages.getString(getClass().getName() + ".modules")); //$NON-NLS-1$

    _moduleViewer = CheckboxTableViewer.newCheckList(extensionSide, SWT.BORDER);
    _moduleViewer.setContentProvider(new ArrayContentProvider());
    _moduleViewer.setLabelProvider(new LabelProvider() {

      @Override
      public String getText(Object element)
      {
        return element.toString();
      }
    });

    // fPluginViewer.setSorter(new ListUtil.PluginSorter());
    _moduleViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(SelectionChangedEvent e)
      {
        moduleSelected(getSelectedModuleNames());
      }
    });

    _moduleViewer.addCheckStateListener(new ICheckStateListener() {

      public void checkStateChanged(CheckStateChangedEvent event)
      {

      }
    });

    gd = new GridData(GridData.FILL_BOTH);
    gd.widthHint = 125;
    gd.heightHint = 100;
    _moduleViewer.getTable().setLayoutData(gd);

    setModuleData(null);

    Composite descSide = new Composite(sashForm, SWT.NULL);
    layout = new GridLayout();
    layout.marginWidth = layout.marginHeight = 0;
    descSide.setLayout(layout);
    descSide.setLayoutData(new GridData(GridData.FILL_BOTH));
    // we'll put the description in here
    _descText = new Text(descSide, SWT.MULTI);
    _descText.setEditable(false);

    _descText.setLayoutData(new GridData(GridData.FILL_BOTH));
    initialize();
    dialogChanged();
    setControl(container);
  }

  protected void setModuleData(IProject project)
  {
    Map<String, ModuleDescriptor> map = new TreeMap<String, ModuleDescriptor>();

    if (project != null && project.isAccessible())
      for (ModuleDescriptor md : ModuleRegistry.getRegistry().getDescriptors(
          project, true))
        map.put(md.getName(), md);

    _moduleMap = map;

    _moduleViewer.setInput(_moduleMap.keySet().toArray(
        new String[_moduleMap.size()]));

    /*
     * set default enabled
     */
    for (ModuleDescriptor descriptor : _moduleMap.values())
      if (descriptor.isEnabledByDefault())
        _moduleViewer.setChecked(descriptor.getName(), true);

  }

  public String getSelectedModuleNames()
  {
    Object item = ((IStructuredSelection) _moduleViewer.getSelection())
        .getFirstElement();
    if (item instanceof String) return ((String) item);
    return null;
  }

  protected void moduleSelected(String moduleName)
  {
    if (moduleName != null)
      _descText.setText(_moduleMap.get(moduleName).getDescription());
    else
      _descText.setText("");
  }

  public Collection<ModuleDescriptor> getSelectedModules()
  {
    ArrayList<ModuleDescriptor> modules = new ArrayList<ModuleDescriptor>();
    for (Object moduleName : _moduleViewer.getCheckedElements())
      modules.add(_moduleMap.get(moduleName));

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("returning selected modules : " + modules);

    return modules;
  }

  /**
   * Tests if the current workbench selection is a suitable container to use.
   */

  @SuppressWarnings("unchecked")
  private void initialize()
  {
    try
    {
      _validExtensions = ModelParserFactory.getValidExtensions();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    if (_selection != null && _selection.isEmpty() == false
        && _selection instanceof IStructuredSelection)
    {
      IStructuredSelection ssel = (IStructuredSelection) _selection;
      if (ssel.size() > 1) return;
      Object obj = ssel.getFirstElement();
      if (obj instanceof IResource) setProject(((IResource) obj).getProject());
    }
    _fileText.setText("new-model.jactr");
  }

  /**
   * Uses the standard container selection dialog to choose the new value for
   * the container field.
   */
  private void handleBrowse()
  {
    setProject(chooseACTRProject());
  }

  protected void setProject(IProject project)
  {
    if (!ACTRProjectUtils.isACTRProject(project))
    {
      updateStatus("Project must be an ACT-R Project");
      project = null;
    }
    _project = project;
    setModuleData(project);
    String projectName = "";
    if (project != null && project.isAccessible())
      projectName = project.getName();
    _projectText.setText(projectName);
  }

  private IProject chooseACTRProject()
  {
    IProject[] projects;

    projects = ACTRProjectUtils.getACTRProjects();

    // ILabelProvider labelProvider = new
    // JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
    ILabelProvider labelProvider = ACTRProjectLabelProvider.getInstance();
    ElementListSelectionDialog dialog = new ElementListSelectionDialog(
        getShell(), labelProvider);

    dialog.setTitle("Choose a project"); //$NON-NLS-1$
    dialog.setMessage("Select the ACTR Project"); //$NON-NLS-1$
    dialog.setElements(projects);

    if (_project != null)
    {
      dialog.setInitialSelections(new Object[] { _project });
    }
    if (dialog.open() == Window.OK)
    {
      return (IProject) dialog.getFirstResult();
    }
    return null;
  }

  /**
   * Ensures that both text fields are set.
   */
  private void dialogChanged()
  {
    String container = getProjectName();
    String fileName = getFileName();

    if (container.length() == 0)
    {
      updateStatus("Project must be specified");
      return;
    }
    if (fileName.length() == 0)
    {
      updateStatus("File name must be specified");
      return;
    }
    int dotLoc = fileName.lastIndexOf('.');
    if (dotLoc != -1)
    {
      String ext = fileName.substring(dotLoc + 1);
      boolean matched = false;
      for (String extension : _validExtensions)
        if (ext.equalsIgnoreCase(extension))
        {
          matched = true;
          break;
        }

      if (!matched)
      {
        updateStatus("File extension must be " + _validExtensions);
        return;
      }
    }
    updateStatus(null);
  }

  private void updateStatus(String message)
  {
    setErrorMessage(message);
    setPageComplete(message == null);
  }

  public String getProjectName()
  {
    return _projectText.getText();
  }

  public String getFileName()
  {
    return _fileText.getText();
  }
}