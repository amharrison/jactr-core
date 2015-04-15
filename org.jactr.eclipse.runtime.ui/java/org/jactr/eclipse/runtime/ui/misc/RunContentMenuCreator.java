package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */


import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.session.control.ISessionController2;

public class RunContentMenuCreator implements IMenuCreator
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RunContentMenuCreator.class);

  private final ILabelProvider       _labelProvider;

  private final ISessionController2  _controller;

  private Menu                       _root;

  private volatile IAction           _lastRun;

  private Collection<ITimeBasedAction> _allActions;

  public RunContentMenuCreator(ISessionController2 controller2,
      ILabelProvider provider)
  {
    _controller = controller2;
    _labelProvider = provider;
    _allActions = new ArrayList<ITimeBasedAction>();
  }

  public void dispose()
  {
    if (_root != null) _root.dispose();
    _root = null;
  }

  public ISessionController2 getController()
  {
    return _controller;
  }

  public Menu getMenu(Control parent)
  {
    // dispose();
    if (_root == null) _root = buildMenu(new Menu(parent));
    return _root;
  }

  public Menu getMenu(Menu parent)
  {
    // dispose();

    if (_root == null) _root = buildMenu(new Menu(parent));
    return _root;
  }

  public IAction getLastRun()
  {
    return _lastRun;
  }

  public void setLastRun(IAction action)
  {
    _lastRun = action;
  }

  public void refresh()
  {
    for (ITimeBasedAction action : _allActions)
      action.update(_controller.getCurrentTime());
  }

  protected Menu buildMenu(Menu menu)
  {
    /*
     * we create two items: Run to.., Run for..
     */

    buildSubMenu(menu, "Run for ...", _controller.getRunForContentProvider(),
        false);
    buildSubMenu(menu, "Skip to ...", _controller.getRunToContentProvider(),
        true);

    menu.addMenuListener(new MenuListener() {

      public void menuHidden(MenuEvent e)
      {

      }

      public void menuShown(MenuEvent e)
      {
        _root.setEnabled(_controller.isRunning());
      }

    });

    return menu;
  }

  protected void buildSubMenu(Menu menu, String headerText,
      ITreeContentProvider provider, boolean isRunTo)
  {
    MenuItem header = new MenuItem(menu, SWT.CASCADE);
    header.setText(headerText);
    Menu subMenu = new Menu(header);
    header.setMenu(subMenu);

    if (provider != null)
    for (Object root : provider.getElements(null))
      populateSubMenus(subMenu, root, provider, isRunTo);

    if (isRunTo)
    {/*
      * add the default runTo(user specified)
      */

      IAction runUntilUser = new Action("Specific Time", SWT.PUSH) {
        @Override
        public void run()
        {
          /*
           * simple dialog
           */
          InputDialog dialog = new InputDialog(Display.getCurrent()
              .getActiveShell(), "Skip to", "Enter time to jump to",
              String.format("%.2f", _controller.getCurrentTime()),
              new IInputValidator() {

                public String isValid(String newText)
                {
                  String rtn = null;
                  try
                  {
                    double until = Double.parseDouble(newText);
                    if (until <= _controller.getCurrentTime())
                      rtn = String.format(
                          "Time must be less than current time %.2f",
                          _controller.getCurrentTime());
                    else
                      rtn = null;
                  }
                  catch (Exception e)
                  {
                    return "Input must be in seconds";
                  }

                  return rtn;
                }

              });

          if (dialog.open() == Window.OK)
          {
            String runUntil = dialog.getValue();
            try
            {
              double runUntilTime = Double.parseDouble(runUntil);
              if (_controller.canRunTo(runUntilTime))
                _controller.runTo(runUntilTime);
            }
            catch (Exception e)
            {
              RuntimePlugin.error(
                  String.format("Failed to run to %s", runUntil), e);
            }
          }
        }
      };

      new ActionContributionItem(runUntilUser).fill(subMenu, -1);
    }

  }

  protected void populateSubMenus(Menu subMenu, final Object node,
      ITreeContentProvider provider, final boolean isRunTo)
  {
    if (provider.hasChildren(node))
    {
      /*
       * this is a submenu mark
       */
      MenuItem header = new MenuItem(subMenu, SWT.CASCADE);
      header.setText(node.toString());
      Menu newSubMenu = new Menu(header);
      header.setMenu(newSubMenu);
      for (Object child : provider.getChildren(node))
        populateSubMenus(newSubMenu, child, provider, isRunTo);
    }
    else
    {
      /**
       * lone item, this is a runner.
       */
      ITimeBasedAction action = new TimeBasedAction(this, node, isRunTo);

      action.setText(String.format("%s %s", isRunTo ? "Skip to " : "Run for ",
          _labelProvider.getText(node)));

      _allActions.add(action);

      ActionContributionItem aci = new ActionContributionItem(action);
      aci.fill(subMenu, -1);
    }
  }
}
