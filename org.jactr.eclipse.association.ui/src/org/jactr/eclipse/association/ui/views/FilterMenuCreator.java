package org.jactr.eclipse.association.ui.views;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.jactr.eclipse.association.ui.filter.IFilterProvider;

final class FilterMenuCreator implements IMenuCreator
{
  /**
   * 
   */
  private final Consumer<ViewerFilter[]>              _filterConsumer;

  private final Supplier<Collection<IFilterProvider>> _availableFilterProvider;

  /**
   * @param associationViewer
   */
  FilterMenuCreator(Supplier<Collection<IFilterProvider>> filterProviders,
      Consumer<ViewerFilter[]> filterConsumer)
  {
    _filterConsumer = filterConsumer;
    _availableFilterProvider = filterProviders;
  }

  private Menu _menu;

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

        // String mapperName = getAssociationMapper().getClass().getName();
        // for (MenuItem item : menu.getItems())
        // {
        // AssociationMapperDescriptor descriptor =
        // (AssociationMapperDescriptor) item
        // .getData();
        //
        // if (descriptor.getClassName().equals(mapperName))
        // item.setSelection(true);
        // else
        // item.setSelection(false);
        // }
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

    for (IFilterProvider provider : _availableFilterProvider.get())
    {
      MenuItem item = new MenuItem(menu, SWT.PUSH);
      item.setData(provider);
      item.setText(provider.getLabel());
      item.addSelectionListener(new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e)
        {
          ViewerFilter[] newFilters = ((IFilterProvider) e.widget.getData())
              .getFilters();
          if (newFilters != null && newFilters.length != 0)
            _filterConsumer.accept(newFilters);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e)
        {
          widgetSelected(e);
        }

      });
    }

    // Collection<AssociationMapperDescriptor> descriptors =
    // AssociationMapperRegistry
    // .getRegistry().getAllDescriptors();
    //
    // for (AssociationMapperDescriptor descriptor : descriptors)
    // {
    // MenuItem item = new MenuItem(menu, SWT.CHECK);
    // item.setData(descriptor);
    // item.setText(descriptor.getName());
    //
    // item.addSelectionListener(new SelectionListener() {
    //
    // public void widgetDefaultSelected(SelectionEvent e)
    // {
    //
    // }
    //
    // public void widgetSelected(SelectionEvent e)
    // {
    // setAssociationMapper((AssociationMapperDescriptor) ((MenuItem)
    // e.widget)
    // .getData());
    // }
    // });
    // }

  }
}