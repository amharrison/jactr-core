package org.jactr.eclipse.association.ui.views;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.jactr.eclipse.association.ui.content.AssociationViewLabelProvider;
import org.jactr.eclipse.association.ui.content.AssociativeContentProvider;
import org.jactr.eclipse.association.ui.model.Association;
import org.jactr.eclipse.association.ui.model.ModelAssociations;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.io.antlr3.misc.ASTSupport;

public class AssociationViewer extends ViewPart implements
    IZoomableWorkbenchPart
{

  /**
   * The ID of the view as specified by the extension.
   */
  public static final String       VIEW_ID    = "org.jactr.eclipse.association.ui.views.AssociationViewer";

  private GraphViewer              _viewer;

  private int                      _nodeStyle = ZestStyles.NODES_FISHEYE
                                                  | ZestStyles.NODES_NO_LAYOUT_RESIZE;

  private Action                   _viewAll;

  private ZoomContributionViewItem _zoomAction;

  private ACTRModelEditor                  _lastEditor;

  private Class<? extends LayoutAlgorithm> _lastLayoutClass = RadialLayoutAlgorithm.class;

  private Comparator                       _layoutComparator = new Comparator() {

                                                               public int compare(
                                                                   Object arg0,
                                                                   Object arg1)
                                                               {
                                                                 if (arg0 instanceof CommonTree)
                                                                 {
                                                                   String name0 = ASTSupport
                                                                       .getName((CommonTree) arg0);
                                                                   String name1 = ASTSupport
                                                                       .getName((CommonTree) arg1);
                                                                   return name0
                                                                       .compareTo(name1);
                                                                 }
                                                                 if (arg0 instanceof Association)
                                                                 {
                                                                   Double value0 = ((Association) arg0)
                                                                       .getStrength();
                                                                   Double value1 = ((Association) arg1)
                                                                       .getStrength();
                                                                   return value0
                                                                       .compareTo(value1);
                                                                 }
                                                                 return 0;
                                                               }

                                                             };

  /**
   * The constructor.
   */
  public AssociationViewer()
  {
  }

  public void viewAll(final ACTRModelEditor editor)
  {
    Runnable runner = new Runnable() {
      public void run()
      {
        try
        {
          showBusy(true);
          _lastEditor = editor;

          CommonTree descriptor = editor.getCompilationUnit()
              .getModelDescriptor();
          ModelAssociations associations = null;
          if (descriptor != null)
            associations = new ModelAssociations(descriptor);
          else
            associations = new ModelAssociations();

          _viewer.setInput(associations);
          forceCurve(10);
        }
        finally
        {
          showBusy(false);
        }
      }
    };

    _viewer.getControl().getDisplay().asyncExec(runner);
  }

  public void view(ACTRModelEditor editor, CommonTree nearestChunk)
  {
    view(editor, nearestChunk, _lastLayoutClass);
  }

  public void view(final ACTRModelEditor editor, final CommonTree nearestChunk,
      Class<? extends LayoutAlgorithm> layoutClass)
  {
    final Class fLayoutClass = layoutClass == null ? RadialLayoutAlgorithm.class
        : layoutClass;
    Runnable runner = new Runnable() {
      public void run()
      {
        try
        {
          showBusy(true);
          _lastEditor = editor;

          CommonTree descriptor = editor.getCompilationUnit()
              .getModelDescriptor();
          ModelAssociations associations = null;
          if (descriptor != null && nearestChunk != null)
            associations = new ModelAssociations(descriptor, ASTSupport
                .getName(nearestChunk));
          else
            associations = new ModelAssociations();

          setLayoutAlgorithm(fLayoutClass);

          _viewer.setInput(associations);
          if (nearestChunk != null)
            forceCurve(30);
          else
            forceCurve(10);
        }
        catch (Exception e)
        {
          UIPlugin.log("Failed to focus on " + nearestChunk, e);
        }
        finally
        {
          showBusy(false);
        }
      }
    };

    _viewer.getControl().getDisplay().asyncExec(runner);
  }

  /**
   * I have no clue how to do this properly. But, I want to set the curve of the
   * links. You'd think I could do it right after setInput on the SWT thread,
   * but some other thread is modifying the collection, so we do this brute
   * force. Unfortunately the content/label providers don't allow setting of the
   * curve
   * 
   * @param depth
   */
  private void forceCurve(int depth)
  {
    List connections = new ArrayList();
    boolean completed = false;
    while (!completed)
      try
      {
        connections.clear();
        connections.addAll(_viewer.getGraphControl().getConnections());
        for (Object link : connections)
          ((GraphConnection) link).setCurveDepth(depth);
        completed = true;
      }
      catch (ConcurrentModificationException cme)
      {

      }
  }

  public AbstractZoomableViewer getZoomableViewer()
  {
    return _viewer;
  }

  /**
   * This is a callback that will allow us to create the viewer and initialize
   * it.
   */
  @Override
  public void createPartControl(Composite parent)
  {
    // viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL |
    // SWT.V_SCROLL);
    // viewer.setContentProvider(new ViewContentProvider());
    // viewer.setLabelProvider(new ViewLabelProvider());
    // viewer.setSorter(new NameSorter());
    // viewer.setInput(getViewSite());

    _viewer = new GraphViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
    // _viewer.addConstraintAdapter(new RelationshipConstraintAdapter());
    _viewer.setNodeStyle(_nodeStyle);

    // _viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
    _viewer.setLabelProvider(new AssociationViewLabelProvider(this));
    _viewer.setContentProvider(new AssociativeContentProvider());

    setLayoutAlgorithm(RadialLayoutAlgorithm.class);
    /*
     * handle resize correctly
     */
    parent.addControlListener(new ControlListener() {

      private boolean _resizeQueued = false;

      public void controlMoved(ControlEvent e)
      {

      }

      public void controlResized(final ControlEvent e)
      {
        if (!_viewer.getGraphControl().isDisposed() && !_resizeQueued)
        {
          _resizeQueued = true;
          Runnable apply = new Runnable() {
            public void run()
            {
              if (!_viewer.getGraphControl().isDisposed())
                _viewer.applyLayout();
              _resizeQueued = false;
            }
          };
          _viewer.getGraphControl().getDisplay().asyncExec(apply);
        }
      }

    });

    _viewer.addDoubleClickListener(new IDoubleClickListener() {

      public void doubleClick(DoubleClickEvent event)
      {
        ACTRModelEditor editor = ACTRModelEditor.getActiveEditor();
        if (editor == null) editor = _lastEditor;
        Object selection = getSelection();
        if (selection == null) return;

        view(editor, (CommonTree) selection, _lastLayoutClass);
      }
    });

    makeActions();
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();
  }

  public Object getSelection()
  {
    Object selection = ((StructuredSelection) _viewer.getSelection())
        .getFirstElement();
    return selection;
  }

  protected void setLayoutAlgorithm(Class clazz)
  {
    _lastLayoutClass = clazz;
    /*
     * do we need to actually change the layout?
     */
    // if (_viewer.getGraphControl().getLayoutAlgorithm() != null
    // && _viewer.getGraphControl().getLayoutAlgorithm().getClass() == clazz)
    // return;
    try
    {
      final LayoutAlgorithm alg = (LayoutAlgorithm) clazz.newInstance();
      // alg.setFilter(_layoutFilter);
      alg.setComparator(_layoutComparator);

      Display.getCurrent().asyncExec(new Runnable() {
        public void run()
        {
          _viewer.setLayoutAlgorithm(alg, true);
          // _viewer.refresh();
        }
      });
    }
    catch (Exception e)
    {

    }
  }

  private void hookContextMenu()
  {
    MenuManager menuMgr = new MenuManager("#PopupMenu");
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager manager)
      {
        AssociationViewer.this.fillContextMenu(manager);
      }
    });
    // Menu menu = menuMgr.createContextMenu(viewer.getControl());
    // viewer.getControl().setMenu(menu);
    // getSite().registerContextMenu(menuMgr, viewer);
  }

  private void contributeToActionBars()
  {
    IActionBars bars = getViewSite().getActionBars();
    fillLocalPullDown(bars.getMenuManager());
    fillLocalToolBar(bars.getToolBarManager());
  }

  private void fillLocalPullDown(IMenuManager manager)
  {
    // manager.add(action1);
    // manager.add(new Separator());
    // manager.add(action2);
  }

  private void fillContextMenu(IMenuManager manager)
  {
    getViewSite().getActionBars().getMenuManager().add(_zoomAction);

    // manager.add(action1);
    // manager.add(action2);
    // Other plug-ins can contribute there actions here
    manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
  }

  private void fillLocalToolBar(IToolBarManager manager)
  {
    manager.add(_viewAll);
    manager.add(new Separator());
    // manager.add(action1);
    // manager.add(action2);

    manager.add(new Action("Refresh Layout" /*
                                             * , ProductionViewActivator
                                             * .getDefault
                                             * ().getImageRegistry().
                                             * getDescriptor (REFRESH)
                                             */) {
      @Override
      public void run()
      {
        _viewer.applyLayout();
      }
    });

    /*
     * layout control
     */
    IMenuCreator menuCreator = new IMenuCreator() {

      private Menu     _menu;

      private String[] _names      = new String[] { "Spring", "Radial", "Grid",
                                       "Tree" };

      private Class[]  _algorithms = new Class[] { SpringLayoutAlgorithm.class,
                                       RadialLayoutAlgorithm.class,
                                       GridLayoutAlgorithm.class,
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

            public void widgetSelected(SelectionEvent e)
            {
              setLayoutAlgorithm((Class) ((MenuItem) e.widget).getData());
            }
          });
        }
      }
    };

    Action layoutAction = new Action("Set Layout Algorithm",
        IAction.AS_DROP_DOWN_MENU) {
    };

    // layoutAction.setImageDescriptor(ProductionViewActivator.getDefault()
    // .getImageRegistry().getDescriptor(LAYOUT));
    layoutAction.setMenuCreator(menuCreator);

    manager.add(layoutAction);

  }

  private void makeActions()
  {
    _viewAll = new Action("View All", IAction.AS_PUSH_BUTTON) {
      @Override
      public void run()
      {
        ACTRModelEditor editor = ACTRModelEditor.getActiveEditor();
        if (editor != null) viewAll(editor);
        else if (_lastEditor != null) viewAll(_lastEditor);
      }
    };

    _zoomAction = new ZoomContributionViewItem(this);

    // _viewAll.setImageDescriptor(ProductionViewActivator.getDefault()
    // .getImageRegistry().getDescriptor(ALL));

    // action1 = new Action() {
    // public void run()
    // {
    // showMessage("Action 1 executed");
    // }
    // };
    // action1.setText("Action 1");
    // action1.setToolTipText("Action 1 tooltip");
    // action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
    // .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    //
    // action2 = new Action() {
    // public void run()
    // {
    // showMessage("Action 2 executed");
    // }
    // };
    // action2.setText("Action 2");
    // action2.setToolTipText("Action 2 tooltip");
    // action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
    // .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    // doubleClickAction = new Action() {
    // public void run()
    // {
    // ISelection selection = viewer.getSelection();
    // Object obj = ((IStructuredSelection) selection).getFirstElement();
    // showMessage("Double-click detected on " + obj.toString());
    // }
    // };
  }

  private void hookDoubleClickAction()
  {
    // viewer.addDoubleClickListener(new IDoubleClickListener() {
    // public void doubleClick(DoubleClickEvent event)
    // {
    // doubleClickAction.run();
    // }
    // });
  }

  private void showMessage(String message)
  {
    // MessageDialog.openInformation(viewer.getControl().getShell(),
    // "Associative Link Viewer", message);
  }

  /**
   * Passing the focus request to the viewer's control.
   */
  @Override
  public void setFocus()
  {
    _viewer.getControl().setFocus();
  }
}