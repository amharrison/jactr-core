package org.jactr.eclipse.ui.preferences;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.internal.ui.text.PreferencesAdapter;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.bundles.descriptors.UnitCompilerDescriptor;
import org.jactr.eclipse.core.bundles.registry.UnitCompilerRegistry;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.io.compiler.IReportableUnitCompiler;

public class UnitCompilersPreferencePage extends PreferencePage implements
    IWorkbenchPreferencePage
{

  private Collection<UnitCompilerControl> _compilers = new ArrayList<UnitCompilerControl>();

  private PreferencesAdapter              _adapter   = new PreferencesAdapter(
                                                         CorePlugin
                                                             .getDefault()
                                                             .getPluginPreferences());

  @Override
  protected Control createContents(Composite parent)
  {
    Composite entryTable = new Composite(parent, SWT.VERTICAL);
    GridLayout layout = new GridLayout();
    entryTable.setLayout(layout);

    for (UnitCompilerDescriptor desc : UnitCompilerRegistry.getRegistry()
        .getAllDescriptors())
      if (!desc.isInWorkspace())
      {
        UnitCompilerControl control = new UnitCompilerControl(desc);
        _compilers.add(control);
        
        control.createControl(entryTable, _adapter);
        
        control.load();
      }

    return entryTable;
  }

  @Override
  protected void performDefaults()
  {
    for (UnitCompilerControl control : _compilers)
      control.loadDefaults();
  }

  @Override
  public boolean performOk()
  {
    for (UnitCompilerControl control : _compilers)
      control.store();
    return super.performOk();
  }

  public void init(IWorkbench workbench)
  {
    setPreferenceStore(_adapter);
  }

  private class UnitCompilerControl
  {
    private UnitCompilerDescriptor _descriptor;

    private BooleanFieldEditor     _enabledField;

    private ComboFieldEditor       _levelField;

    public UnitCompilerControl(UnitCompilerDescriptor descriptor)
    {
      _descriptor = descriptor;
    }

    public UnitCompilerDescriptor getDescriptor()
    {
      return _descriptor;
    }

    public Control createControl(Composite parent, IPreferenceStore store)
    {
      Composite group = new Composite(parent, SWT.BORDER);
      GridLayout layout = new GridLayout(2, false);
      group.setLayout(layout);

      Composite enable = new Composite(group, SWT.NONE);
      GridData gd = new GridData();
      enable.setLayoutData(gd);

      _enabledField = new BooleanFieldEditor(_descriptor.getClassName()
          + ".enabled", _descriptor.getName(), enable);
      _enabledField.setPreferenceStore(store);

      store.setDefault(_descriptor.getClassName() + ".enabled", _descriptor
          .isDefaultEnabled());

      IReportableUnitCompiler.Level[] levelValues = IReportableUnitCompiler.Level
          .values();
      String[][] levels = new String[levelValues.length][2];
      for (int i = 0; i < levelValues.length; i++)
      {
        levels[i][0] = levelValues[i].toString();
        levels[i][1] = levelValues[i].name();
      }

      final Composite comboParent = new Composite(group, SWT.NONE);
      gd = new GridData();
      gd.horizontalAlignment = SWT.LEFT;
      comboParent.setLayoutData(gd);

      _levelField = new ComboFieldEditor(_descriptor.getClassName() + ".level",
          "Report Level", levels, comboParent);
      _levelField.setPreferenceStore(store);
      store.setDefault(_descriptor.getClassName() + ".level", _descriptor
          .getReportLevel().name());

      _enabledField.setPropertyChangeListener(new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event)
        {
          _levelField.setEnabled(_enabledField.getBooleanValue(), comboParent);
        }
      });

      return group;
    }

    public void load()
    {
      _levelField.load();
      _enabledField.load();
    }

    public void loadDefaults()
    {
      _levelField.loadDefault();
      _enabledField.loadDefault();
    }

    public void store()
    {
      _levelField.store();
      _enabledField.store();
    }
  }
}
