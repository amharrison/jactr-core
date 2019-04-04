package org.jactr.eclipse.association.ui.views;

import java.util.function.Consumer;

import org.eclipse.gef.layout.ILayoutAlgorithm;
import org.eclipse.gef.layout.algorithms.GridLayoutAlgorithm;
import org.eclipse.gef.layout.algorithms.RadialLayoutAlgorithm;
import org.eclipse.gef.layout.algorithms.SpringLayoutAlgorithm;
import org.eclipse.gef.layout.algorithms.TreeLayoutAlgorithm;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

final class LayoutMenuCreator implements IMenuCreator
{

  final private Consumer<Class<? extends ILayoutAlgorithm>> _consumer;
  /**
   * @param associationViewer
   */
  LayoutMenuCreator(Consumer<Class<? extends ILayoutAlgorithm>> clazzConsumer)
  {
    _consumer = clazzConsumer;
  }

  private Menu     _menu;

  private String[] _names      = new String[] { "Spring", "Radial", "Grid",
      "Tree" };

  private Class[]  _algorithms = new Class[] { SpringLayoutAlgorithm.class,
      RadialLayoutAlgorithm.class, GridLayoutAlgorithm.class,
      TreeLayoutAlgorithm.class };

  public void dispose()
  {
    if (_menu != null && !_menu.isDisposed()) _menu.dispose();
    _menu = null;
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
    for (int i = 0; i < _names.length; i++)
    {
      MenuItem item = new MenuItem(menu, SWT.PUSH);
      item.setText(_names[i]);
      item.setData(_algorithms[i]);
      item.addSelectionListener(new SelectionListener() {

        public void widgetDefaultSelected(SelectionEvent e)
        {

        }

        @SuppressWarnings("unchecked")
        public void widgetSelected(SelectionEvent e)
        {
          _consumer
              .accept((Class<? extends ILayoutAlgorithm>) ((MenuItem) e.widget)
                  .getData());
        }
      });
    }
  }
}