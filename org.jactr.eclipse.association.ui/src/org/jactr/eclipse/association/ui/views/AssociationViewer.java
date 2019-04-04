package org.jactr.eclipse.association.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.concurrent.CompletableFuture;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.gef.layout.algorithms.RadialLayoutAlgorithm;
import org.eclipse.gef.layout.algorithms.SpringLayoutAlgorithm;
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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.AbstractZoomableViewer;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.viewers.IZoomableWorkbenchPart;
import org.eclipse.zest.core.viewers.ZoomContributionViewItem;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutAlgorithm;
import org.jactr.eclipse.association.ui.content.AssociationViewLabelProvider;
import org.jactr.eclipse.association.ui.content.AssociativeContentProvider;
import org.jactr.eclipse.association.ui.filter.IFilterProvider;
import org.jactr.eclipse.association.ui.filter.registry.AssociationFilterDescriptor;
import org.jactr.eclipse.association.ui.filter.registry.AssociationFilterRegistry;
import org.jactr.eclipse.association.ui.internal.Activator;
import org.jactr.eclipse.association.ui.mapper.IAssociationMapper;
import org.jactr.eclipse.association.ui.mapper.impl.DefaultAssociationMapper;
import org.jactr.eclipse.association.ui.mapper.registry.AssociationMapperDescriptor;
import org.jactr.eclipse.association.ui.mapper.registry.AssociationMapperRegistry;
import org.jactr.eclipse.association.ui.model.Association;
import org.jactr.eclipse.association.ui.model.ModelAssociations;
import org.jactr.eclipse.core.concurrent.JobExecutor;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.concurrent.UIJobExecutor;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.io.antlr3.misc.ASTSupport;

import javolution.util.FastList;

public class AssociationViewer extends ViewPart
    implements IZoomableWorkbenchPart
{

  /**
   * Logger definition
   */
  static private final transient Log       LOGGER            = LogFactory
      .getLog(AssociationViewer.class);

  /**
   * The ID of the view as specified by the extension.
   */
  public static final String               VIEW_ID           = "org.jactr.eclipse.association.ui.views.AssociationViewer";

  GraphViewer                              _viewer;

  private int                              _nodeStyle        = ZestStyles.NODES_FISHEYE
      | ZestStyles.NODES_NO_LAYOUT_RESIZE;

  private Action                           _viewAll;

  protected IAssociationMapper             _mapper;

  private ZoomContributionViewItem         _zoomAction;

  private ACTRModelEditor                  _lastEditor;

  private Object                           _currentSelection;                                                             // used
                                                                                                                          // for
                                                                                                                          // reset
                                                                                                                          // of
                                                                                                                          // visual

  private Class<? extends LayoutAlgorithm> _lastLayoutClass  = RadialLayoutAlgorithm.class;

  private Comparator                       _layoutComparator = new Comparator() {

                                                               public int compare(
                                                                   Object arg0,
                                                                   Object arg1)
                                                               {
                                                                 if (arg0 instanceof CommonTree)
                                                                 {
                                                                   String name0 = ASTSupport
                                                                       .getName(
                                                                           (CommonTree) arg0);
                                                                   String name1 = ASTSupport
                                                                       .getName(
                                                                           (CommonTree) arg1);
                                                                   return name0
                                                                       .compareTo(
                                                                           name1);
                                                                 }
                                                                 if (arg0 instanceof Association)
                                                                 {
                                                                   Double value0 = ((Association) arg0)
                                                                       .getStrength();
                                                                   Double value1 = ((Association) arg1)
                                                                       .getStrength();
                                                                   return value0
                                                                       .compareTo(
                                                                           value1);
                                                                 }
                                                                 return 0;
                                                               }

                                                             };

  Collection<IFilterProvider>              _availableFilterProviders;

  /**
   * The constructor.
   */
  public AssociationViewer()
  {

    // grab the last associaiton mapper, or default.
    _lastLayoutClass = getLastLayout();
    restoreAssociationMapper();

    assembleFilterProviders();
  }

  private void assembleFilterProviders()
  {
    _availableFilterProviders = new ArrayList<IFilterProvider>();
    AssociationFilterRegistry afr = AssociationFilterRegistry.getRegistry();
    for (AssociationFilterDescriptor afd : afr.getAllDescriptors())
      try
      {
        _availableFilterProviders.add((IFilterProvider) afd.instantiate());
      }
      catch (Exception e)
      {
        UIPlugin
            .log(String.format("Failed to instantiation filter provider : %s",
                afd.getClassName()), e);
      }
  }

  static public AssociationViewer getActiveViewer()
  {
    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
        .getActivePage();
    for (IViewReference reference : page.getViewReferences())
      if (reference.getId() == VIEW_ID)
        return (AssociationViewer) reference.getView(false);

    return null;
  }

  /**
   * grab the last used association Mapper
   */
  protected void restoreAssociationMapper()
  {
    String mapperClassName = Activator.getDefault().getPreferenceStore()
        .getString(VIEW_ID + ".mapper");
    if (mapperClassName.length() == 0) // null
      mapperClassName = DefaultAssociationMapper.class.getName();

    for (AssociationMapperDescriptor desc : AssociationMapperRegistry
        .getRegistry().getAllDescriptors())
      if (desc.getClassName().equals(mapperClassName))
        setAssociationMapper(desc);
  }

  protected Class<? extends LayoutAlgorithm> getLastLayout()
  {
    String layoutClassName = Activator.getDefault().getPreferenceStore()
        .getString(VIEW_ID + ".layout");
    if (layoutClassName.length() == 0) // null
      layoutClassName = SpringLayoutAlgorithm.class.getName();
    try
    {
      return (Class<? extends LayoutAlgorithm>) getClass().getClassLoader()
          .loadClass(layoutClassName);
    }
    catch (Exception e)
    {
      return SpringLayoutAlgorithm.class;
    }
  }

  protected void setAssociationMapper(AssociationMapperDescriptor descriptor)
  {
    // set
    try
    {
      IAssociationMapper assMapper = (IAssociationMapper) descriptor
          .instantiate();
      setAssociationMapper(assMapper);

      // save
      Activator.getDefault().getPreferenceStore().setValue(VIEW_ID + ".mapper",
          descriptor.getClassName());
    }
    catch (Exception e)
    {
      UIPlugin.log(IStatus.ERROR, String.format("Failed to instantiation %s ",
          descriptor.getClassName()), e);
    }

  }

  protected void setAssociationMapper(IAssociationMapper mapper)
  {
    _mapper = mapper;

    if (_viewer != null)
    {
      // I should probably test that we are in the ui thread..
      ((AssociationViewLabelProvider) _viewer.getLabelProvider())
          .setMapper(_mapper);

      _viewer.setInput(_viewer.getInput());

      forceCurve(30);
    }
  }

  public IAssociationMapper getAssociationMapper()
  {
    return _mapper;
  }

  /**
   * creates but does not process the modelAssociation data
   * 
   * @param modelDescriptor
   *          may be null
   * @param nearestChunk
   *          may be null
   * @return
   */
  private ModelAssociations getAssociations(CommonTree modelDescriptor,
      CommonTree nearestChunk)
  {
    ModelAssociations rtn = null;
    if (modelDescriptor != null)
      if (nearestChunk != null)
        rtn = new ModelAssociations(modelDescriptor, getAssociationMapper(),
            ASTSupport.getName(nearestChunk));
      else
        rtn = new ModelAssociations(modelDescriptor, getAssociationMapper());
    else
      rtn = new ModelAssociations(getAssociationMapper());

    return rtn;
  }

  public ModelAssociations getInput()
  {
    return (ModelAssociations) _viewer.getInput();
  }

  public void viewAll(final ACTRModelEditor editor)
  {
    view(editor, null);

    // Runnable runner = new Runnable() {
    // public void run()
    // {
    // try
    // {
    // showBusy(true);
    // _lastEditor = editor;
    //
    // CommonTree descriptor = editor.getCompilationUnit()
    // .getModelDescriptor();
    // _viewer.setInput(getAssociations(descriptor, null));
    // forceCurve(30);
    // }
    // finally
    // {
    // showBusy(false);
    // }
    // }
    // };
    //
    // _viewer.getControl().getDisplay().asyncExec(runner);
  }

  public void view(ACTRModelEditor editor, CommonTree nearestChunk)
  {
    view(editor, nearestChunk, _lastLayoutClass);
  }

  public void view(final ACTRModelEditor editor, final CommonTree nearestChunk,
      final Class<? extends LayoutAlgorithm> layoutClass)
  {
    // Runnable runner = new Runnable() {
    // public void run()
    // {
    // try
    // {
    // showBusy(true);
    // _lastEditor = editor;
    //
    // CommonTree descriptor = editor.getCompilationUnit()
    // .getModelDescriptor();
    //
    // setLayoutAlgorithm(layoutClass);
    //
    // _viewer.setInput(getAssociations(descriptor, nearestChunk));
    //
    // forceCurve(30);
    //
    // }
    // catch (Exception e)
    // {
    // UIPlugin.log("Failed to focus on " + nearestChunk, e);
    // }
    // finally
    // {
    // showBusy(false);
    // }
    // }
    // };
    //
    // _viewer.getControl().getDisplay().asyncExec(runner);

    Runnable setup = new Runnable() {

      @Override
      public void run()
      {
        LOGGER.debug("starting view");

        showBusy(true);
        _lastEditor = editor;

        setLayoutAlgorithm(layoutClass, false);
      }
    };

    Runnable cleanup = new Runnable() {

      @Override
      public void run()
      {
        LOGGER.debug("Done setting view");
        showBusy(false);
      }

    };

    /*
     * our new version: 1) get the model association data (this is already
     * parallelized)
     */
    CommonTree descriptor = editor.getCompilationUnit().getModelDescriptor();
    final ModelAssociations associations = getAssociations(descriptor,
        nearestChunk);
    JobExecutor ex = new JobExecutor("association slave");
    final UIJobExecutor uiex = new UIJobExecutor("association slave ui");

    // setup
    CompletableFuture<Void> setupProc = CompletableFuture.runAsync(setup, uiex);

    // build the associations, but only after setup
    final CompletableFuture<Void> assProc = setupProc.thenComposeAsync((v) -> {

      return associations
          .process(Runtime.getRuntime().availableProcessors() / 2 + 1, ex);
    }, ex);

    // cleanup just in case
    assProc.exceptionally(t -> {
      UIPlugin.log(String.format("Failed to process associations in parallel"),
          t);
      uiex.execute(cleanup);
      return null;
    });

    // when this is done, we can actually set the display
    CompletableFuture<Void> setProc = assProc.thenAcceptAsync((v) -> {
      try
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Setting input for viewer"));

        _viewer.setInput(associations);

        forceCurve(30);
      }
      catch (Exception e)
      {
        UIPlugin.log("Failed to focus on " + nearestChunk, e);
      }
    }, uiex);

    // clean up after failure or success
    setProc.handle((v, t) -> {
      if (t != null) UIPlugin
          .log(String.format("Failed to set associations in parallel"), t);
      uiex.execute(cleanup);
      return null;
    });

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
  @SuppressWarnings("unchecked")
  void forceCurve(int depth)
  {
    double trueDepth = 25; // default
    FastList<GraphConnection> connections = FastList.newInstance();
    boolean completed = false;
    while (!completed)
      try
      {
        connections.clear();
        connections.addAll(_viewer.getGraphControl().getConnections());

        for (GraphConnection link : connections)
        {
          Object data = link.getData();
          /*
           * we use a mod hashCode on the curve so that multiple links between
           * the same chunks aren't stacked on top of each other.
           */
          if (data instanceof Association)
          {
            Association ass = (Association) data;

            // we don't tweak self-links
            if (ass.getIChunk() != ass.getJChunk())
            {
              trueDepth = 10 + data.hashCode() % 50;
              link.setCurveDepth((int) trueDepth);
            }
          }

          // still trying to figure out how to force labels to midpoint
          // after using curve depth
          // for(Object child : link.getConnectionFigure().getChildren())
          // {
          // IFigure childFigure = (IFigure) child;
          // link.getConnectionFigure().add(childFigure, childFigure);
          // childFigure.getUpdateManager()
          // }

        }
        completed = true;
      }
      catch (ConcurrentModificationException cme)
      {

      }

    FastList.recycle(connections);
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
    _viewer.setLabelProvider(
        new AssociationViewLabelProvider(this, getAssociationMapper()));
    _viewer.setContentProvider(new AssociativeContentProvider());

    setLayoutAlgorithm(_lastLayoutClass, false);
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
        // we only focus on nodes, not edges
        if (selection instanceof CommonTree)
          view(editor, (CommonTree) selection, _lastLayoutClass);
      }
    });

    _viewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event)
      {
        Object selected = ((StructuredSelection) event.getSelection())
            .getFirstElement();

        Object priorSelection = _currentSelection;

        if (selected != null && priorSelection != selected)
        {
          // reset prior
          _currentSelection = selected;
          if (priorSelection instanceof CommonTree)
            refreshNode((CommonTree) priorSelection);
          if (priorSelection instanceof Association)
            refreshEdge((Association) priorSelection);

          if (selected instanceof CommonTree)
            refreshNode((CommonTree) selected);
          if (selected instanceof Association)
            refreshEdge((Association) selected);
        }

        forceCurve(10);
      }

    });

    makeActions();
    hookContextMenu();
    hookDoubleClickAction();
    contributeToActionBars();
  }

  protected void refreshNode(CommonTree chunkNode)
  {
    _viewer.update(chunkNode, null);
    ModelAssociations associations = (ModelAssociations) _viewer.getInput();
    String chunkName = ASTSupport.getName(chunkNode);
    for (Association ass : associations.getOutboundAssociations(chunkName))
      _viewer.update(ass, null);

    for (Association ass : associations.getInboundAssociations(chunkName))
      _viewer.update(ass, null);
  }

  protected void refreshEdge(Association link)
  {
    _viewer.update(link, null);
    _viewer.update(link.getIChunk(), null);
    _viewer.update(link.getJChunk(), null);
  }

  public Object getSelection()
  {
    return _currentSelection;
  }

  protected void setLayoutAlgorithm(Class clazz, final boolean forceLayout)
  {
    _lastLayoutClass = clazz;

    Activator.getDefault().getPreferenceStore().setValue(VIEW_ID + ".layout",
        clazz.getName());

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
          _viewer.setLayoutAlgorithm(alg, forceLayout);
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

  private IMenuCreator createFilterMenu()
  {
    IMenuCreator menuCreator = new FilterMenuCreator(() -> {
      return _availableFilterProviders;
    }, (filters) -> {
    });

    return menuCreator;
  }

  private IMenuCreator createLayoutMenu()
  {
    /*
     * layout control
     */
    IMenuCreator menuCreator = new LayoutMenuCreator(this);

    return menuCreator;
  }

  private IMenuCreator createMappingMenu()
  {
    /*
     * associationMapper control
     */
    IMenuCreator menuCreator = new MapperMenuCreator(this);
    return menuCreator;
  }

  private void fillLocalPullDown(IMenuManager manager)
  {
    // manager.add(action1);
    Action filterAction = new Action("Filter", IAction.AS_DROP_DOWN_MENU) {
    };
    filterAction.setMenuCreator(createFilterMenu());

    manager.add(filterAction);

    manager.add(new Separator());

    Action layoutAction = new Action("Set Layout Algorithm",
        IAction.AS_DROP_DOWN_MENU) {
    };

    // layoutAction.setImageDescriptor(ProductionViewActivator.getDefault()
    // .getImageRegistry().getDescriptor(LAYOUT));
    layoutAction.setMenuCreator(createLayoutMenu());

    manager.add(layoutAction);

    manager.add(new Separator());

    // manager.add(action2);

    Action mapperAction = new Action("Set Association Mapper",
        IAction.AS_DROP_DOWN_MENU) {
    };

    // layoutAction.setImageDescriptor(ProductionViewActivator.getDefault()
    // .getImageRegistry().getDescriptor(LAYOUT));
    mapperAction.setMenuCreator(createMappingMenu());

    manager.add(mapperAction);

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

    manager.add(new Action(
        "Refresh Layout" /*
                          * , ProductionViewActivator .getDefault
                          * ().getImageRegistry(). getDescriptor (REFRESH)
                          */) {
      @Override
      public void run()
      {
        _viewer.applyLayout();
      }
    });

  }

  private void makeActions()
  {
    _viewAll = new Action("View All", IAction.AS_PUSH_BUTTON) {
      @Override
      public void run()
      {
        ACTRModelEditor editor = ACTRModelEditor.getActiveEditor();
        if (editor != null)
          viewAll(editor);
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