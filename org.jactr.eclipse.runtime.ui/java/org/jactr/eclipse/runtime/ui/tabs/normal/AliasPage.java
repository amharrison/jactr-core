/*
 * Created on Jul 16, 2004 Copyright (C) 2001-4, Anthony Harrison anh23@pitt.edu
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * @author harrison TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Generation - Code and
 *         Comments
 */
public class AliasPage
{

  private Map<String, TextEditor>         fAliases;
  private ACTRMainTab fTab;
  private Composite   fParent;

  private class TextEditor
  {

    private AliasPage _self;
    private Text      text;
    private String    _alias;

    public TextEditor(AliasPage self, String alias)
    {
      _self = self;
      _alias = alias;
    }

    public void detach()
    {
      Composite parent = text.getParent();
      text.dispose();
      parent.layout(true);
      parent.redraw();
    }

    public String getAlias()
    {
      return text.getText();
    }

    public void create(Composite parent)
    {
      TableWrapData td = new TableWrapData();
      text = fTab.getToolkit().createText(parent, ""); //$NON-NLS-1$
      td = new TableWrapData(TableWrapData.FILL_GRAB);
      //gd.widthHint = 100;
      text.setLayoutData(td);
      text.addFocusListener(new FocusListener() {

        public void focusGained(FocusEvent e)
        {
          text.selectAll();
          fTab.setActiveAlias(_self, text.getText());
        }

        public void focusLost(FocusEvent e)
        {
          text.clearSelection();
          fTab.setActiveAlias(_self, null);
        }
      });
      
      text.addModifyListener(new ModifyListener() {

        public void modifyText(ModifyEvent e)
        {
          if (!text.getText().equals(_alias))
          {
            fTab.setDirty(true);
            fTab.updateLaunchConfigurationDialog();
          }
        }
      });
      
    }

    public void update()
    {
      text.setText(_alias);
    }

    public void initialize()
    {
      update();
    }
  }

  public AliasPage(ACTRMainTab tab, String defaultName)
  {
    this.fTab = tab;
    fAliases = new HashMap<String, TextEditor>();
    addAlias(defaultName);
  }

  public Collection<String> getAliases()
  {
    ArrayList<String> rtn = new ArrayList<String>();
    for(Map.Entry<String, TextEditor> entry : fAliases.entrySet())
    {
      TextEditor ted = entry.getValue();
      if (ted != null)
        rtn.add(ted.getAlias());
      else rtn.add(entry.getKey());
    }
    return rtn;
  }

  public void removeAllAliases()
  {
    //except self..
    Iterator<String> itr = getAliases().iterator();
    while (itr.hasNext())
      removeAlias(itr.next());
  }

  public int getNumberOfAliases()
  {
    return fAliases.size();
  }

  public void addAlias(String alias)
  {
    if (fParent != null)
    {
      boolean bordersNeeded = false;
      TextEditor editor = fAliases.get(alias);
      if (editor != null) return;
      editor = new TextEditor(this, alias);
      bordersNeeded = true;
      editor.create(fParent);
      editor.initialize();
      fAliases.put(alias, editor);
      if (bordersNeeded) fTab.getToolkit().paintBordersFor(fParent);

      fParent.layout(true);
      fParent.redraw();
    }
    else fAliases.put(alias, null);
  }

  public void removeAlias(String alias)
  {
    Iterator itr = fAliases.entrySet().iterator();
    while (itr.hasNext())
    {
      Map.Entry entry = (Map.Entry) itr.next();
      TextEditor editor = (TextEditor) entry.getValue();
      if (editor!=null && alias.equals(editor.getAlias())
      		|| editor==null && alias.equals(entry.getKey()))
      {
        itr.remove();
        if (editor != null) editor.detach();
        return;
      }
    }
  }

  public void createContents(Composite parent)
  {
    fParent = parent;
    TableWrapLayout layout = new TableWrapLayout();
    layout.numColumns = 1;
    parent.setLayout(layout);
    String[] aliases = fAliases.keySet().toArray(new String[0]);
    for (String alias : aliases)
      addAlias(alias);
  }

  public void dispose()
  {
    Iterator<TextEditor> itr = fAliases.values().iterator();
    while(itr.hasNext())
      {
       TextEditor editor = itr.next();
       if(editor!=null)
         editor.detach();
      }
  }

}