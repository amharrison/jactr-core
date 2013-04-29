/*
 * Created on Jul 12, 2004 Copyright (C) 2001-4, Anthony Harrison anh23@pitt.edu
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with this library; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */

package org.jactr.eclipse.runtime.ui.tabs.normal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.jactr.eclipse.runtime.ui.tabs.CommonExtensionDescriptorTab;

/**
 * @author harrison TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Generation - Code and
 *         Comments
 */
public class GeneralParameterPage
{
  private final CommonExtensionDescriptorTab _parentTab;

  private final String                       fName;

  private Collection<PropertyEditor>         fDescriptors;

  private final Map<String, String>          fValues;

  private final FormToolkit                  fToolkit;

  private abstract class PropertyEditor
  {
    private final String key;

    private final String label;

    public PropertyEditor(String key, String label)
    {
      this.key = key;
      this.label = label;
    }

    public String getKey()
    {
      return key;
    }

    public String getLabel()
    {
      return label;
    }

    abstract void create(Composite parent);

    abstract void update();

    abstract void initialize();

    protected void valueModified(String value)
    {
      fValues.put(getKey(), value);
      _parentTab.dirty();
      // fTab.updateLaunchConfigurationDialog();
    }
  }

  private class TextEditor extends PropertyEditor
  {

    private Text text;

    public TextEditor(String key, String label)
    {
      super(key, label);
    }

    @Override
    public void create(Composite parent)
    {
      Label label = fToolkit.createLabel(parent, getLabel());
      TableWrapData td = new TableWrapData();
      td.valign = TableWrapData.MIDDLE;
      label.setLayoutData(td);
      text = fToolkit.createText(parent, ""); //$NON-NLS-1$
      td = new TableWrapData(TableWrapData.FILL_GRAB);
      // gd.widthHint = 100;
      text.setLayoutData(td);
    }

    @Override
    public void update()
    {
      String value = fValues.get(getKey());
      text.setText(value);
    }

    @Override
    public void initialize()
    {
      update();
      text.addModifyListener(new ModifyListener() {

        public void modifyText(ModifyEvent e)
        {
          valueModified(text.getText());
        }
      });
    }
  }

  public GeneralParameterPage(String name, Map<String, String> parameterMap,
      FormToolkit toolkit, CommonExtensionDescriptorTab tab)
  {
    this.fToolkit = toolkit;
    fName = name;
    fValues = new TreeMap<String, String>(parameterMap);
    _parentTab = tab;
  }

  public String getName()
  {
    return fName;
  }

  public void setParameter(String key, String value)
  {
    fValues.put(key, value);
  }

  public String getParameter(String key)
  {
    return fValues.get(key);
  }

  public Map<String, String> getParameterMap()
  {
    return Collections.unmodifiableMap(fValues);
  }

  public void createContents(Composite parent)
  {
    fDescriptors = new ArrayList<PropertyEditor>();
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 2;
    parent.setLayout(layout);
    boolean bordersNeeded = false;
    for (String key : fValues.keySet())
    {
      String baseValue = fValues.get(key);
      PropertyEditor editor;
      editor = new TextEditor(key, key);
      bordersNeeded = true;
      editor.create(parent);
      editor.initialize();
      fDescriptors.add(editor);
      if (bordersNeeded) fToolkit.paintBordersFor(parent);
    }
  }

  public void dispose()
  {
  }
}