/*
 * Created on Mar 9, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.eclipse.ui.generic.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.jactr.eclipse.ui.UIPlugin;

public abstract class AbstractModelViewPart extends ViewPart
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER        = LogFactory
                                                       .getLog(AbstractModelViewPart.class);

  private CTabFolder                 _modelViewsFolder;

  private Set<Object>                _deferredAdds = new HashSet<Object>();

  public AbstractModelViewPart()
  {

  }

  @Override
  public void createPartControl(Composite parent)
  {
    createModelViewsFolder(parent);
    partControlCreated();
  }

  protected void createModelViewsFolder(Composite parent)
  {
    _modelViewsFolder = new CTabFolder(parent, SWT.SINGLE | SWT.BORDER);
    _modelViewsFolder.setMRUVisible(true);
    _modelViewsFolder.setSingle(false);
    _modelViewsFolder.setSimple(false);
  }

  protected void partControlCreated()
  {

  }

  @Override
  public void dispose()
  {
    super.dispose();
  }

  @Override
  public void setFocus()
  {
    if (!_modelViewsFolder.isDisposed())
    {
      CTabItem item = _modelViewsFolder.getSelection();
      if (item != null && !item.isDisposed()) item.getControl().setFocus();
    }
  }

  protected CTabFolder getTabFolder()
  {
    return _modelViewsFolder;
  }

  public CTabItem getSelectedTab()
  {
    if (_modelViewsFolder.isDisposed()) return null;

    CTabItem item = _modelViewsFolder.getSelection();
    return item;
  }

  public void setSelectedTab(CTabItem item)
  {
    _modelViewsFolder.setSelection(item);
  }

  public CTabItem getModelTab(String modelName)
  {
    for (CTabItem tab : _modelViewsFolder.getItems())
      if (tab.getText().equals(modelName)) return tab;
    return null;
  }

  protected boolean wasDeferred(Object modelData)
  {
    return _deferredAdds.contains(modelData);
  }

  protected void removeDeferred(Object modelData)
  {
    _deferredAdds.remove(modelData);
  }

  /**
   * create a new tab
   * 
   * @param modelName
   * @return
   */
  protected CTabItem addModelTab(String modelName, Composite modelComposite)
  {
    CTabItem item = new CTabItem(_modelViewsFolder, SWT.NONE);
    item.setText(modelName);
    item.setControl(modelComposite);

    item.addDisposeListener(new DisposeListener() {

      public void widgetDisposed(DisposeEvent e)
      {
        CTabItem item = (CTabItem) e.widget;
        Object modelData = item.getData();
        disposeModelComposite(item.getText(), modelData,
            (Composite) item.getControl());
        tabClosed(modelData);
        modelDataRemoved(modelData);
      }

    });

    setSelectedTab(item);
    return item;
  }

  private void removeModelTab(CTabItem item)
  {
    Object modelData = item.getData();
    disposeModelComposite(item.getText(), item.getData(),
        (Composite) item.getControl());
    item.dispose();
    modelDataRemoved(modelData);
  }

  protected void tabClosed(Object modelData)
  {

  }

  /**
   * add a new tab for modelName that will contain the composite returned by
   * {@link #createModelComposite(String, Object, Composite)} the modelData is
   * provided by the extending class, this will be passed on to the composite
   * creator.
   * 
   * @param modelName
   * @param modelData
   * @return the actual name of the tab, which will allow you to access the tab
   */
  public String addModelData(String modelName, Object modelData)
  {
    String rtnName = modelName;
    Composite modelComposite = createModelComposite(rtnName, modelData,
        _modelViewsFolder);

    if (modelComposite != null)
    {
      CTabItem tab = addModelTab(rtnName, modelComposite);
      tab.setData(modelData);
      if (wasDeferred(modelData)) removeDeferred(modelData);
      modelDataAdded(modelData);
    }
    else if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("(%s) Add of %s returned null, ignoring",
          getClass().getName(), modelName));

    return rtnName;
  }

  protected void modelDataAdded(Object modelData)
  {

  }

  protected void modelDataRemoved(Object modelData)
  {

  }

  protected void deferAdd(final String modelName, final Object modelData,
      final int tryAgainInMS)
  {
    _deferredAdds.add(modelData);

    final Runnable adder = new Runnable() {

      public void run()
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("(%s) Deferred add of %s",
              AbstractModelViewPart.this.getClass().getName(), modelName));
        addModelData(modelName, modelData);
      }

    };

    UIPlugin.getStandardDisplay().asyncExec(new Runnable() {

      public void run()
      {
        /*
         * to get around the SWT thread constraint for timed runs
         */
        UIPlugin.getStandardDisplay().timerExec(tryAgainInMS, adder);
      }

    });
  }

  abstract protected Composite createModelComposite(String modelName,
      Object modelData, Composite parent);

  abstract protected void disposeModelComposite(String modelName,
      Object modelData, Composite content);

  public void removeModelData(Object modelData)
  {
    for (CTabItem tab : _modelViewsFolder.getItems())
      if (!tab.isDisposed() && modelData == tab.getData()) removeModelTab(tab);
  }

  public Collection<Object> getModelData()
  {
    Collection<Object> rtn = new ArrayList<Object>();
    for (CTabItem tab : _modelViewsFolder.getItems())
      rtn.add(tab.getData());
    return rtn;
  }

  public CTabItem[] getItems()
  {
    return _modelViewsFolder.getItems();
  }

}
