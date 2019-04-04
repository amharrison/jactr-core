package org.jactr.eclipse.association.ui.views;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.jactr.eclipse.association.ui.mapper.IAssociationMapper;
import org.jactr.eclipse.association.ui.mapper.registry.AssociationMapperDescriptor;
import org.jactr.eclipse.association.ui.mapper.registry.AssociationMapperRegistry;

final class MapperMenuCreator implements IMenuCreator
{
  /**
   * 
   */

  final private Supplier<Class<? extends IAssociationMapper>> _selectionProvider;

  final private Consumer<AssociationMapperDescriptor>         _selectionConsumer;

  private Menu                                                _menu;

  /**
   * @param associationViewer
   */
  public MapperMenuCreator(
      Supplier<Class<? extends IAssociationMapper>> selectionProvider,
      Consumer<AssociationMapperDescriptor> selectionConsumer)
  {
    _selectionConsumer = selectionConsumer;
    _selectionProvider = selectionProvider;
  }

  public void dispose()
  {
    if (_menu != null && !_menu.isDisposed()) _menu.dispose();
    _menu = null;
  }

  protected void addMenuListener(Menu menu)
  {
    menu.addMenuListener(new MenuListener() {

      @Override
      public void menuHidden(MenuEvent e)
      {

      }

      @Override
      public void menuShown(MenuEvent e)
      {
        // TODO Auto-generated method stub
        Menu menu = (Menu) e.widget;
        String mapperName = _selectionProvider.get().getName();
        for (MenuItem item : menu.getItems())
        {
          AssociationMapperDescriptor descriptor = (AssociationMapperDescriptor) item
              .getData();

          if (descriptor.getClassName().equals(mapperName))
            item.setSelection(true);
          else
            item.setSelection(false);
        }
      }

    });
  }

  public Menu getMenu(Control parent)
  {
    if (_menu == null || _menu.getParent() != parent)
    {
      if (_menu != null) dispose();
      _menu = new Menu(parent);
      buildMenu(_menu);
    }
    return _menu;
  }

  public Menu getMenu(Menu parent)
  {
    if (_menu == null || _menu.getParentMenu() != parent)
    {
      if (_menu != null) dispose();
      _menu = new Menu(parent);
      buildMenu(_menu);
    }
    return _menu;
  }

  protected void buildMenu(Menu menu)
  {
    addMenuListener(_menu);

    Collection<AssociationMapperDescriptor> descriptors = AssociationMapperRegistry
        .getRegistry().getAllDescriptors();

    for (AssociationMapperDescriptor descriptor : descriptors)
    {
      MenuItem item = new MenuItem(menu, SWT.CHECK);
      item.setData(descriptor);
      item.setText(descriptor.getName());

      item.addSelectionListener(new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e)
        {

        }

        public void widgetSelected(SelectionEvent e)
        {
          _selectionConsumer.accept(
              (AssociationMapperDescriptor) ((MenuItem) e.widget).getData());
        }
      });
    }

  }
}