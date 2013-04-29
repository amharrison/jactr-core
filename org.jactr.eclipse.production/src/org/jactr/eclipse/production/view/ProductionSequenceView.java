package org.jactr.eclipse.production.view;

/*
 * default logging
 */
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.production.ProductionViewActivator;
import org.jactr.eclipse.production.content.AllContentProvider;
import org.jactr.eclipse.production.content.SequenceContentProvider;
import org.jactr.eclipse.production.content.TreeContentProvider;
import org.jactr.eclipse.production.filters.AmbiguousFilter;
import org.jactr.eclipse.production.filters.FilterAction;
import org.jactr.eclipse.production.filters.LayoutFilter;
import org.jactr.eclipse.production.filters.NegativeFilter;
import org.jactr.eclipse.production.filters.PositiveFilter;
import org.jactr.eclipse.production.render.ProductionSequenceViewLabelProvider;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.editor.command.GoTo;
import org.jactr.io.antlr3.misc.DetailedCommonTree;
import org.jactr.tools.analysis.production.relationships.ProductionRelationships;

public class ProductionSequenceView extends ViewPart implements
    IZoomableWorkbenchPart
{
  /**
   * Logger definition
   */
  static private final transient Log               LOGGER     = LogFactory
                                                                  .getLog(ProductionSequenceView.class);

  static public final String                       VIEW_ID    = ProductionSequenceView.class
                                                                  .getName();

  private GraphViewer                              _viewer;

  private CommonTree                               _modelDescriptor;

  private Map<CommonTree, ProductionRelationships> _relationships;

  private int                                      _depth     = 2;

  private int                                      _nodeStyle = ZestStyles.NODES_FISHEYE
                                                                  | ZestStyles.NODES_NO_LAYOUT_RESIZE;

  private FilterAction                             _ambiguousFilter;

  private FilterAction                             _negativeFilter;

  private FilterAction                             _positiveFilter;

  private ZoomContributionViewItem                 _zoomAction;

  private Action                                   _viewAll;

  private Action                                   _viewNext;

  private Action                                   _viewPrevious;

  private Action                                   _viewSequence;

  private CommonTree                               _selectedProduction;

  private LayoutFilter                             _layoutFilter;

  static private final String                      AMBIGUOUS  = "ambiguous";

  static private final String                      POSITIVE   = "positive";

  static private final String                      NEGATIVE   = "negative";

  static private final String                      REFRESH    = "refresh";

  static private final String                      LAYOUT     = "layout";

  static private final String                      ALL        = "all";

  static private final String                      PREVIOUS   = "previous";

  static private final String                      NEXT       = "next";

  static private final String                      SEQUENCE   = "sequence";

  static
  {
    /*
     * prepopulate the image registry
     */
    ImageRegistry registry = ProductionViewActivator.getDefault()
        .getImageRegistry();
    registry.put(AMBIGUOUS, ImageDescriptor
        .createFromURL(ProductionSequenceView.class.getClassLoader()
            .getResource("icons/ambiguous.gif")));
    registry.put(POSITIVE, ImageDescriptor
        .createFromURL(ProductionSequenceView.class.getClassLoader()
            .getResource("icons/positive.gif")));
    registry.put(NEGATIVE, ImageDescriptor
        .createFromURL(ProductionSequenceView.class.getClassLoader()
            .getResource("icons/negative.gif")));
    registry.put(REFRESH, ImageDescriptor
        .createFromURL(ProductionSequenceView.class.getClassLoader()
            .getResource("icons/refresh.gif")));
    registry.put(LAYOUT, ImageDescriptor
        .createFromURL(ProductionSequenceView.class.getClassLoader()
            .getResource("icons/layout.gif")));
    registry.put(ALL, ImageDescriptor
        .createFromURL(ProductionSequenceView.class.getClassLoader()
            .getResource("icons/all.gif")));
    registry.put(PREVIOUS, ImageDescriptor
        .createFromURL(ProductionSequenceView.class.getClassLoader()
            .getResource("icons/previous.gif")));
    registry.put(NEXT, ImageDescriptor
        .createFromURL(ProductionSequenceView.class.getClassLoader()
            .getResource("icons/follow.gif")));
    registry.put(SEQUENCE, ImageDescriptor
        .createFromURL(ProductionSequenceView.class.getClassLoader()
            .getResource("icons/sequence.gif")));
  }

  @Override
  public void createPartControl(Composite parent)
  {
    // Group group = new Group(parent, SWT.SHADOW_IN);
    // _viewer = new GraphViewer(group, SWT.NONE);
    _viewer = new GraphViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL);
    // _viewer.addConstraintAdapter(new RelationshipConstraintAdapter());
    _viewer.setNodeStyle(_nodeStyle);
    // _viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
    _viewer.setLabelProvider(new ProductionSequenceViewLabelProvider(this));

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

    _viewer.addSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(SelectionChangedEvent event)
      {
        Object selection = ((StructuredSelection) event.getSelection())
            .getFirstElement();

        if (LOGGER.isDebugEnabled()) LOGGER.debug("Selected " + selection);

        if (selection instanceof CommonTree)
          _selectedProduction = (CommonTree) selection;
        else
          _selectedProduction = null;

        _viewer.refresh();
      }
    });

    _viewer.addDoubleClickListener(new IDoubleClickListener() {

      public void doubleClick(DoubleClickEvent event)
      {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("double click");
        /*
         * automatically do sequence view
         */
        _viewSequence.run();
      }
    });

    _viewer.getGraphControl().addMouseListener(new MouseListener() {

      public void mouseDoubleClick(MouseEvent e)
      {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("double click");

      }

      public void mouseDown(MouseEvent e)
      {
        // TODO Auto-generated method stub

      }

      public void mouseUp(MouseEvent e)
      {
        // TODO Auto-generated method stub

      }

    });

    createActions();
  }

  public CommonTree getSelectedProduction()
  {
    return _selectedProduction;
  }

  public Map<CommonTree, ProductionRelationships> getInput()
  {
    return _relationships;
  }

  protected void createActions()
  {
    IToolBarManager toolBar = getViewSite().getActionBars().getToolBarManager();

    _viewAll = new Action("View All", IAction.AS_CHECK_BOX) {
      @Override
      public void run()
      {
        ACTRModelEditor editor = ACTRModelEditor.getActiveEditor();
        if (editor != null) viewAll(editor);
      }
    };
    _viewAll.setImageDescriptor(ProductionViewActivator.getDefault()
        .getImageRegistry().getDescriptor(ALL));
    toolBar.add(_viewAll);
    toolBar.add(new Separator());

    _viewSequence = new Action("View Sequence", IAction.AS_CHECK_BOX) {
      @Override
      public void run()
      {
        ACTRModelEditor editor = ACTRModelEditor.getActiveEditor();
        if (editor != null) viewSequence(editor, _selectedProduction);
      }
    };
    _viewSequence.setImageDescriptor(ProductionViewActivator.getDefault()
        .getImageRegistry().getDescriptor(SEQUENCE));
    toolBar.add(_viewSequence);

    _viewPrevious = new Action("View Previous", IAction.AS_CHECK_BOX) {
      @Override
      public void run()
      {
        ACTRModelEditor editor = ACTRModelEditor.getActiveEditor();
        if (editor != null) viewPrevious(editor, _selectedProduction);
      }
    };
    _viewPrevious.setImageDescriptor(ProductionViewActivator.getDefault()
        .getImageRegistry().getDescriptor(PREVIOUS));
    toolBar.add(_viewPrevious);

    _viewNext = new Action("View Next", IAction.AS_CHECK_BOX) {
      @Override
      public void run()
      {
        ACTRModelEditor editor = ACTRModelEditor.getActiveEditor();
        if (editor != null) viewNext(editor, _selectedProduction);
      }
    };
    _viewNext.setImageDescriptor(ProductionViewActivator.getDefault()
        .getImageRegistry().getDescriptor(NEXT));
    toolBar.add(_viewNext);

    toolBar.add(new Separator());

    _positiveFilter = new FilterAction("Show Positive", new PositiveFilter(),
        _viewer);
    _positiveFilter.setChecked(true);
    _positiveFilter.setImageDescriptor(ProductionViewActivator.getDefault()
        .getImageRegistry().getDescriptor(POSITIVE));
    _viewer.addFilter(_positiveFilter.getFilter());
    toolBar.add(_positiveFilter);

    _negativeFilter = new FilterAction("Show Negative", new NegativeFilter(),
        _viewer);
    _negativeFilter.setImageDescriptor(ProductionViewActivator.getDefault()
        .getImageRegistry().getDescriptor(NEGATIVE));
    _viewer.addFilter(_negativeFilter.getFilter());
    _negativeFilter.setChecked(false);
    toolBar.add(_negativeFilter);

    _ambiguousFilter = new FilterAction("Show Ambiguous",
        new AmbiguousFilter(), _viewer);
    _ambiguousFilter.setImageDescriptor(ProductionViewActivator.getDefault()
        .getImageRegistry().getDescriptor(AMBIGUOUS));
    _viewer.addFilter(_ambiguousFilter.getFilter());
    toolBar.add(_ambiguousFilter);
    _ambiguousFilter.setChecked(false);

    _layoutFilter = new LayoutFilter(_viewer, _ambiguousFilter.getFilter(),
        _negativeFilter.getFilter(), _positiveFilter.getFilter());

    toolBar.add(new Separator());

    toolBar.add(new Action("Refresh Layout", ProductionViewActivator
        .getDefault().getImageRegistry().getDescriptor(REFRESH)) {
      @Override
      public void run()
      {
        _viewer.applyLayout();
      }
    });

    IMenuCreator menuCreator = new IMenuCreator() {

      private Menu     _menu;

      private String[] _names      = new String[] { "Spring", "Radial", "Grid",
                                       "Tree", "Horizontal", "Horizontal Tree",
                                       "Vertical" };

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

    layoutAction.setImageDescriptor(ProductionViewActivator.getDefault()
        .getImageRegistry().getDescriptor(LAYOUT));
    layoutAction.setMenuCreator(menuCreator);

    toolBar.add(layoutAction);

    IMenuManager root = getViewSite().getActionBars().getMenuManager();

    _zoomAction = new ZoomContributionViewItem(this);
    // toolBar.add(_zoomAction);

    // MenuManager zoom = new MenuManager("Zoom");
    // zoom.setRemoveAllWhenShown(true);
    // zoom.addMenuListener(new IMenuListener(){
    //
    // public void menuAboutToShow(IMenuManager manager)
    // {
    // manager.add(_zoomAction);
    // }
    // });

    // zoom.add(new ZoomContributionViewItem(this));
    // zoom.setRemoveAllWhenShown(true);
    // zoom.add(_zoomAction);

    // root.add(new Separator());
    // root.add(new Separator());
    root.add(_zoomAction);

    MenuManager menuMgr = new MenuManager();
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager mgr)
      {
        fillContextMenu(mgr);
      }
    });

    // Create menu.
    Menu menu = menuMgr.createContextMenu(_viewer.getControl());
    _viewer.getControl().setMenu(menu);

    // Register menu for extension.
    getSite().registerContextMenu(menuMgr, _viewer);
  }

  private void fillContextMenu(IMenuManager manager)
  {
    manager.add(_viewAll);
    if (_selectedProduction != null)
    {
      manager.add(new Separator());
      manager.add(_viewSequence);
      manager.add(new Separator());
      manager.add(_viewPrevious);
      manager.add(_viewNext);
      manager.add(new Separator());
      manager.add(new Action("Go to") {
        @Override
        public void run()
        {
          GoTo.goTo((DetailedCommonTree) getSelectedProduction());
        }
      });
    }
  }

  public boolean isShowingAmbiguous()
  {
    return _ambiguousFilter.isChecked();
  }

  public boolean isShowingNegative()
  {
    return _negativeFilter.isChecked();
  }

  public boolean isShowingPositive()
  {
    return _positiveFilter.isChecked();
  }

  @Override
  public void setFocus()
  {

  }

  protected void setLayoutAlgorithm(Class clazz)
  {

    /*
     * do we need to actually change the layout?
     */
//    if (_viewer.getGraphControl().getLayoutAlgorithm() != null
//        && _viewer.getGraphControl().getLayoutAlgorithm().getClass() == clazz)
//      return;

    try
    {
      final LayoutAlgorithm alg = (LayoutAlgorithm) clazz.newInstance();
      // alg.setFilter(_layoutFilter);

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
      LOGGER.error(
          "ProductionSequenceView.setLayoutAlgorithm threw Exception : ", e);
    }
  }

  public AbstractZoomableViewer getZoomableViewer()
  {
    return _viewer;
  }

  public void viewAll(final ACTRModelEditor editor)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Attempting to visualize all");

    setLayoutAlgorithm(RadialLayoutAlgorithm.class);

    Runnable runner = new Runnable() {
      public void run()
      {
        showBusy(true);

        _viewAll.setChecked(true);
        _viewPrevious.setChecked(false);
        _viewNext.setChecked(false);
        _viewSequence.setChecked(false);

        _viewer.setSelection(StructuredSelection.EMPTY);
        setACTRCompilationUnit(editor.getCompilationUnit());
        _viewer.setContentProvider(new AllContentProvider(
            ProductionSequenceView.this));
        // _viewer.refresh();
        showBusy(false);
      }
    };

    _viewer.getControl().getDisplay().asyncExec(runner);
  }

  public void viewPrevious(final ACTRModelEditor editor,
      final CommonTree current)
  {
    if (current != null)
    {
      Runnable runner = new Runnable() {
        public void run()
        {
          showBusy(true);

          _viewAll.setChecked(false);
          _viewPrevious.setChecked(true);
          _viewNext.setChecked(false);
          _viewSequence.setChecked(false);

          _selectedProduction = current;
          setLayoutAlgorithm(SpringLayoutAlgorithm.class);
          _viewer.setSelection(new StructuredSelection(current));
          setACTRCompilationUnit(editor.getCompilationUnit());
          _viewer.setContentProvider(new TreeContentProvider(
              ProductionSequenceView.this, current, false));
          // _viewer.refresh();
          showBusy(false);
        }
      };

      _viewer.getControl().getDisplay().asyncExec(runner);
    }
    else
      clear();
  }

  public void viewNext(final ACTRModelEditor editor,
      final CommonTree nearestProduction)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Attempting to visualize follosing");

    if (nearestProduction != null)
    {

      Runnable runner = new Runnable() {
        public void run()
        {
          showBusy(true);

          _viewAll.setChecked(false);
          _viewPrevious.setChecked(false);
          _viewNext.setChecked(true);
          _viewSequence.setChecked(false);

          setLayoutAlgorithm(RadialLayoutAlgorithm.class);
          _selectedProduction = nearestProduction;
          _viewer.setSelection(new StructuredSelection(nearestProduction));
          setACTRCompilationUnit(editor.getCompilationUnit());
          _viewer.setContentProvider(new TreeContentProvider(
              ProductionSequenceView.this, nearestProduction, true));
          showBusy(false);
        }
      };

      _viewer.getControl().getDisplay().asyncExec(runner);
    }
    else
      clear();
  }

  public void viewSequence(final ACTRModelEditor editor,
      final CommonTree current)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Attempting to visualize sequence");

    if (current != null)
    {
      Runnable runner = new Runnable() {
        public void run()
        {
          showBusy(true);

          _viewAll.setChecked(false);
          _viewPrevious.setChecked(false);
          _viewNext.setChecked(false);
          _viewSequence.setChecked(true);

          _selectedProduction = current;
          setLayoutAlgorithm(SpringLayoutAlgorithm.class);
          _viewer.setSelection(new StructuredSelection(current));
          setACTRCompilationUnit(editor.getCompilationUnit());
          _viewer.setContentProvider(new SequenceContentProvider(
              ProductionSequenceView.this, current, _depth));
          // _viewer.refresh();
          showBusy(false);
        }
      };

      _viewer.getControl().getDisplay().asyncExec(runner);
    }
    else
      clear();
  }

  protected void clear()
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Clearing");

    _viewer.setInput(null);
    _viewAll.setChecked(false);
    _viewPrevious.setChecked(false);
    _viewNext.setChecked(false);
    _viewSequence.setChecked(false);
    _modelDescriptor = null;
  }

  protected boolean setACTRCompilationUnit(ICompilationUnit unit)
  {
    if (unit == null)
      clear();
    else if (setModelDescriptor(unit))
    {
      /*
       * model descriptor has changed, we need to run the sequence analyzer
       */
      AnalyzerJob job = new AnalyzerJob(this, _modelDescriptor);
      job.schedule();
      return true;
    }

    return false;
  }

  protected void setProductionRelationships(
      Map<CommonTree, ProductionRelationships> relationships)
  {
    _relationships = relationships;
    /*
     * notify the graph viewer of the change
     */
    _viewer.getControl().getDisplay().asyncExec(new Runnable() {
      public void run()
      {
        showBusy(true);
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Setting input");
        _viewer.setInput(_relationships);
        showBusy(false);
      }
    });
  }

  private boolean setModelDescriptor(ICompilationUnit unit)
  {
    CommonTree modelDesc = unit.getModelDescriptor();

    if (modelDesc != _modelDescriptor)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Setting model descriptor");
      _modelDescriptor = modelDesc;
      return true;
    }
    return false;
  }
}
