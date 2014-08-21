package org.jactr.eclipse.runtime.ui.simple;

/*
 * default logging
 */
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javolution.util.FastList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.ui.content.ACTRLabelProvider;
import org.jactr.eclipse.ui.content.AbstractACTRContentProvider;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;

public abstract class SimpleVerticalASTViewComponent implements
    IOrientedComponent
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SimpleVerticalASTViewComponent.class);

  private TreeViewer                 _treeViewer;

  private ASTSupport                 _astSupport      = new ASTSupport();

  private ACTRLabelProvider          _labelProvider   = new ACTRLabelProvider();

  private ITreeContentProvider       _contentProvider = new AbstractACTRContentProvider() {

                                                        public void inputChanged(
                                                            Viewer viewer,
                                                            Object oldInput,
                                                            Object newInput)
                                                        {
                                                          if (newInput instanceof CommonTree)
                                                            setRoot((CommonTree) newInput);
                                                        }
                                                      };

  public void dispose()
  {
    _treeViewer.getTree().dispose();
    _treeViewer = null;
  }

  public void createPartControl(Composite parent)
  {
    _treeViewer = new TreeViewer(parent);
    _treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
    _treeViewer.setContentProvider(_contentProvider);
    _treeViewer.setLabelProvider(_labelProvider);
  }

  /**
   * @param session
   * @param modelName
   * @param time
   * @param isPostConflictResolution
   * @param container
   */
  abstract protected void getAST(ISession session, String modelName,
      double time, boolean isPostConflictResolution,
      Collection<CommonTree> container);

  public void setData(ISession session, String modelName, double time,
      boolean isPostConflictResolution)
  {
    final FastList<CommonTree> trees = FastList.newInstance();
    getAST(session, modelName, time, isPostConflictResolution, trees);

    if (trees.size() == 0)
    {
      noAST();
      FastList.recycle(trees);
      return;
    }

    Collection<Object> collapsedElements = new LinkedList<Object>();
    getCollapsedEntries(_treeViewer.getInput(), collapsedElements);
    
    /*
     * combine the asts
     */
    CommonTree root = _astSupport.create(JACTRBuilder.MODEL, "data");
    for (CommonTree tree : trees)
      root.addChild(tree);

    _treeViewer.getTree().setRedraw(false);
    _treeViewer.setInput(root);
    collapseElements(root, collapsedElements);
    _treeViewer.getTree().setRedraw(true);
  }
  
  private void getCollapsedEntries(Object element, Collection<Object> collapsedElements) {
	  getCollapsedEntries(element, collapsedElements, "", 0);
  }
  
  private void getCollapsedEntries(Object element, Collection<Object> collapsedElements, String prefix,
		  int index) {
	  if(element == null)
		  return;
	  if(element instanceof CommonTree) {
		  CommonTree ct = (CommonTree)element;
		  String path = prefix+"."+ct.getText()+index;
		  if(!_treeViewer.getExpandedState(ct))
			  collapsedElements.add(path);
		  List children = ct.getChildren();
		  if(children != null) {
			  for(int i=0;i<children.size();i++) {
				  getCollapsedEntries(children.get(i), collapsedElements, path, i);
			  }
		  }
	  }
  }
  
  private void collapseElements(Object newElement, Collection<Object> collapsedOldElements) {
	  collapseElements(newElement, collapsedOldElements, "", 0);
  }
  
  private void collapseElements(Object newElement, Collection<Object> collapsedOldElements,
		  String prefix, int index) {
	  if(newElement == null)
		  return;
	  if(newElement instanceof CommonTree) {
		  CommonTree ct = (CommonTree)newElement;
		  String path = prefix+"."+ct.getText()+index;
		  if(collapsedOldElements.contains(path)) {
			  _treeViewer.setExpandedState(ct, false);
		  }
		  List children = ct.getChildren();
		  if(children != null) {
			  for(int i=0;i<children.size();i++) {
				  collapseElements(children.get(i), collapsedOldElements, path, i);
			  }
		  }
  	}
  }
  
  public void noAST()
  {
    _treeViewer.setInput(_astSupport.create(JACTRBuilder.MODEL, "no data"));
  }

  public void setFocus()
  {
    _treeViewer.getTree().setFocus();

  }

}
