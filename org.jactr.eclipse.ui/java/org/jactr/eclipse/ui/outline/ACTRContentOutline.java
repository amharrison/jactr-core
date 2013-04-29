/*
 * Created on Jan 31, 2004 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jactr.eclipse.ui.outline;

import java.net.URL;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.ui.content.ACTRContentProvider;
import org.jactr.eclipse.ui.content.ACTRContentSorter;
import org.jactr.eclipse.ui.content.ACTRLabelProvider;
import org.jactr.eclipse.ui.content.ACTRLazyContentProvider;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.editor.command.GoTo;
import org.jactr.eclipse.ui.editor.markers.PositionMarker;
import org.jactr.eclipse.ui.images.JACTRImages;
import org.jactr.io.antlr3.misc.DetailedCommonTree;

/**
 * basic ast viewer for {@link ACTRCompilationUnit}s. It can currently display
 * using both lazy and normal content providers. The prefered use is the lazy as
 * it will handle very large models MUCH easier.<br>
 * <br>
 * The state persistence of the expansion state does not work with the lazy
 * content provider. we will need to have a correct CommonTree.equals() method
 * before we can do that.
 */
public class ACTRContentOutline extends ContentOutlinePage
{
  /**
   * Logger definition
   */
  static private transient Log LOGGER               = LogFactory
                                                        .getLog(ACTRContentOutline.class);

  static public final String   ACTR_OUTLINE_CONTEXT = ACTRContentOutline.class
                                                        .getName()
                                                        + ".context";

  protected long               _lastASTUpdateTime;

  private TreeViewer           _treeViewer;

  private boolean              _isLazy              = false;

  private Action               _goToAction;
  private Action _showLocal;

  private ACTRModelEditor      _editor;

  public ACTRContentOutline(ACTRModelEditor editor, boolean isLazy)
  {
    super();
    _editor = editor;
    _isLazy = isLazy;
  }

  @Override
  public void createControl(Composite parent)
  {
    _treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
        | SWT.V_SCROLL | (_isLazy ? SWT.VIRTUAL : 0));
    _treeViewer.addSelectionChangedListener(this);
    _treeViewer.setComparator(new ACTRContentSorter());

    if (_isLazy)
      _treeViewer.setContentProvider(new ACTRLazyContentProvider());
    else
      _treeViewer.setContentProvider(new ACTRContentProvider(false));

    _treeViewer.setLabelProvider(new ACTRLabelProvider());
    // auto expand doesnt seem to be working
    // _treeViewer.setAutoExpandLevel(3);
    _treeViewer.addSelectionChangedListener(this);
    _treeViewer.setUseHashlookup(true);

    _treeViewer.setInput(_editor.getCompilationUnit());
    _treeViewer.expandToLevel(3);

    createFilterActions();
    createContextActions();
  }
  
  @Override
  public void setActionBars(IActionBars actionBars)
  {
    super.setActionBars(actionBars);
    actionBars.getToolBarManager().add(_showLocal);
  }
  
  protected void createFilterActions()
  {
    _showLocal = new Action("Show only local", IAction.AS_CHECK_BOX){
      @Override
      public void run()
      {
        ACTRLazyContentProvider lazy = (ACTRLazyContentProvider) _treeViewer.getContentProvider();
        lazy.setImportedContentFiltered(!lazy.isImportedContentFiltered());
        _treeViewer.setInput(_editor.getCompilationUnit());
        _treeViewer.expandToLevel(3);
      }
    };
    
    _showLocal.setChecked(true);
    _showLocal.setEnabled(_isLazy);
    _showLocal.setToolTipText("Toggle the viewing of imported content");
    _showLocal.setImageDescriptor(JACTRImages.getImageDescriptor(JACTRImages.BASIC_FILTER));
  }

  protected void createContextActions()
  {
    MenuManager menuMgr = new MenuManager();
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager mgr)
      {
        fillContextMenu(mgr);
      }
    });

    // Create menu.
    Menu menu = menuMgr.createContextMenu(getControl());
    getControl().setMenu(menu);

    // Register menu for extension.
    getSite().registerContextMenu(ACTR_OUTLINE_CONTEXT, menuMgr, this);
  }

  protected void fillContextMenu(IMenuManager manager)
  {
    if(_goToAction==null) _goToAction = new Action("Go to"){
      @Override
      public void run()
      {
        IStructuredSelection selection = (IStructuredSelection) getTreeViewer().getSelection();
        if(selection==null || selection.isEmpty())
          return;
        
        GoTo.goTo((DetailedCommonTree) selection.getFirstElement());
      }
    };
    
    manager.add(_goToAction);
    manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
  }

  // public void dispose()
  // {
  // _treeViewer.setInput(null);
  // super.dispose();
  // }

  /**
   * Sets focus to a part in the page.
   */
  @Override
  public void setFocus()
  {
    getTreeViewer().getControl().setFocus();
  }

  /*
   * (non-Javadoc) Method declared on ISelectionProvider.
   */
  @Override
  public void setSelection(ISelection selection)
  {
    if (getTreeViewer() != null) getTreeViewer().setSelection(selection);
  }

  /*
   * (non-Javadoc) Method declared on IPage (and Page).
   */
  @Override
  public Control getControl()
  {
    if (getTreeViewer() == null) return null;
    return getTreeViewer().getControl();
  }

  /*
   * (non-Javadoc) Method declared on ISelectionProvider.
   */
  @Override
  public ISelection getSelection()
  {
    if (getTreeViewer() == null) return StructuredSelection.EMPTY;
    return getTreeViewer().getSelection();
  }

  /**
   * Returns this page's tree viewer.
   * 
   * @return this page's tree viewer, or <code>null</code> if
   *         <code>createControl</code> has not been called yet
   */
  @Override
  protected TreeViewer getTreeViewer()
  {
    return _treeViewer;
  }

  /*
   * (non-Javadoc) Method declared on ContentOutlinePage
   */
  @Override
  public void selectionChanged(SelectionChangedEvent event)
  {
    super.selectionChanged(event);

    ISelection selection = event.getSelection();
    if (selection.isEmpty())
      _editor.resetHighlightRange();
    else
    {
      CommonTree segment = (CommonTree) ((IStructuredSelection) selection)
          .getFirstElement();

      CommonTree root = _editor.getCompilationUnit().getModelDescriptor();

      if (root != null && root instanceof DetailedCommonTree
          && segment instanceof DetailedCommonTree)
      {
        DetailedCommonTree dRoot = (DetailedCommonTree) root;
        DetailedCommonTree dSeg = (DetailedCommonTree) segment;

        URL source = dRoot.getSource();
        if (source == null)
        {
          CorePlugin
              .warn("No URL defined for compilation unit model descriptor");
          return;
        }

        /*
         * root had better have a url
         */
        if (source.equals(dSeg.getSource())
            || source.sameFile(dSeg.getSource()))
          try
          {
            Region region = PositionMarker.getTreeSpan(segment, source);

            if (region == null) return;

            if (LOGGER.isDebugEnabled())
              LOGGER.debug(segment.getText() + " at " + segment.getLine() + " "
                  + region);
            // _textEditor.selectAndReveal(region.getOffset(),
            // region.getLength());
            _editor.setHighlightRange(region.getOffset(), region.getLength(),
                true);
          }
          catch (Exception x)
          {
          }
      }
    }
  }

}