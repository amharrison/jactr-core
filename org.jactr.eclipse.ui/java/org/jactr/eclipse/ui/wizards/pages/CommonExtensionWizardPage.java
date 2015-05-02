package org.jactr.eclipse.ui.wizards.pages;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.jactr.eclipse.core.bundles.descriptors.CommonExtensionDescriptor;
import org.jactr.eclipse.core.bundles.meta.ManifestTools;

public class CommonExtensionWizardPage extends WizardPage
{
  /**
   * Logger definition
   */
  static private final transient Log                                      LOGGER        = LogFactory
                                                                                            .getLog(CommonExtensionWizardPage.class);

  private Text                                                            _description;

  private CheckboxTableViewer                                             _descriptorList;

  private final Supplier<Collection<? extends CommonExtensionDescriptor>> _descriptorProvider;

  private final ILabelProvider                                            _labelProvider;

  private final IContentProvider                                          _contentProvider;

  private Set<CommonExtensionDescriptor>                                  _checkedItems = new HashSet<CommonExtensionDescriptor>();

  public CommonExtensionWizardPage(
      Supplier<Collection<? extends CommonExtensionDescriptor>> descriptorProvider,
      ILabelProvider labelProvider, String name, String title, String desc)
  {
    super(name);
    setTitle(title);
    setMessage(desc);
    _descriptorProvider = descriptorProvider;
    _labelProvider = labelProvider;
    _contentProvider = new ArrayContentProvider();
  }

  @Override
  public void createControl(Composite parent)
  {
    Composite container = new Composite(parent, SWT.BORDER);
    GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    container.setLayout(layout);

    Composite listGroup = new Composite(container, SWT.BORDER);
    listGroup.setLayout(new GridLayout());
    listGroup
        .setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 2));

    _descriptorList = CheckboxTableViewer.newCheckList(listGroup, SWT.BORDER);
    _descriptorList
        .addSelectionChangedListener(new ISelectionChangedListener() {

          public void selectionChanged(SelectionChangedEvent e)
          {
            descriptorSelected(getSelectedDescriptor());
          }
        });
    // _descriptorList.setComparator(new ViewerComparator(
    // new Comparator<CommonExtensionDescriptor>() {
    //
    // @Override
    // public int compare(CommonExtensionDescriptor o1,
    // CommonExtensionDescriptor o2)
    // {
    // /*
    // * comparing by name
    // */
    // String name1 = o1.getName();
    // String name2 = o2.getName();
    // return name1.compareTo(name2);
    // }
    // }));
    _descriptorList.setComparator(new ViewerComparator(
        new Comparator<String>() {

          @Override
          public int compare(String o1, String o2)
          {
            /*
             * comparing by name
             */
            return o1.compareTo(o2);
          }
        }));

    _descriptorList.addCheckStateListener(new ICheckStateListener() {

      public void checkStateChanged(CheckStateChangedEvent event)
      {
        if (event.getChecked())
          _checkedItems.add((CommonExtensionDescriptor) event.getElement());
        else
          _checkedItems.remove(event.getElement());
      }
    });

    // content and label providers..
    _descriptorList.setContentProvider(_contentProvider);
    _descriptorList.setLabelProvider(_labelProvider);

    GridData gd = new GridData(GridData.FILL_BOTH);
    _descriptorList.getTable().setLayoutData(gd);

    Composite descriptionGroup = new Composite(container, SWT.BORDER);
    descriptionGroup.setLayout(new GridLayout());
    descriptionGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
        2, 2)); // two wide
    _description = new Text(descriptionGroup, SWT.WRAP | SWT.READ_ONLY
        | SWT.MULTI | SWT.VERTICAL);
    _description.setLayoutData(new GridData(GridData.FILL_BOTH));
    _description.setText("Description");

    setDescriptorData();

    setControl(container);
  }

  protected void descriptorSelected(CommonExtensionDescriptor selectedDescriptor)
  {
    // update the display
    String description = "";
    if (selectedDescriptor != null)
      description = selectedDescriptor.getDescription();
    if (description == null) description = "";
    _description.setText(description);
  }

  private CommonExtensionDescriptor getSelectedDescriptor()
  {
    Object item = ((IStructuredSelection) _descriptorList.getSelection())
        .getFirstElement();

    if (item instanceof CommonExtensionDescriptor)
      return (CommonExtensionDescriptor) item;

    return null;
  }

  protected void setDescriptorData()
  {
    _descriptorList.setInput(_descriptorProvider.get());
  }

  /**
   * called at the during finish and it will verify that required deps are met
   * 
   * @param project
   */
  public void ensureDependencies(IProject project)
  {
    IPluginModelBase base = ManifestTools.getModelBase(project);
    if (base == null)
    {
      LOGGER.error("Null plugin model? ");
      return;
    }
    boolean shouldSave = false;
    for (Object obj : _checkedItems)
    {
      shouldSave = true;
      CommonExtensionDescriptor desc = (CommonExtensionDescriptor) obj;
      String dependency = desc.getContributor();
      // make sure it's in there
      Collection<String> singlton = Collections.singleton(dependency);
      ManifestTools.addEclipseBuddies(base, singlton);
      try
      {
        ManifestTools.addPluginReferences(base, singlton);
      }
      catch (Exception e)
      {
        LOGGER.error(String.format("Could not add %s to %s as dependency? ",
            dependency, project), e);
      }
    }

    if (shouldSave) ManifestTools.save(base);
  }

  public Collection<CommonExtensionDescriptor> getSelectedDescriptors()
  {
    return _checkedItems;
  }

}
