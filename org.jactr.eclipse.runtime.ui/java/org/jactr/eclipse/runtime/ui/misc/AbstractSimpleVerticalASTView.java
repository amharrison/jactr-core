package org.jactr.eclipse.runtime.ui.misc;

/*
 * default logging
 */
import java.util.Collection;

import javolution.util.FastList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.ui.content.ACTRLabelProvider;
import org.jactr.eclipse.ui.content.AbstractACTRContentProvider;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;

public abstract class AbstractSimpleVerticalASTView extends
    AbstractSessionTimeViewPart
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER           = LogFactory
                                                          .getLog(AbstractSimpleVerticalASTView.class);

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

  public AbstractSimpleVerticalASTView()
  {
    super(true);
  }

  abstract protected void getAST(ISession session, String modelName,
      double time, boolean isPostConflictResolution,
      Collection<CommonTree> container);

  @Override
  protected void setData(ISession session, String modelName, double time,
      boolean isPostConflictResolution)
  {
    final FastList<CommonTree> trees = FastList.newInstance();
    getAST(session, modelName, time, isPostConflictResolution, trees);

    if (trees.size() == 0)
    {
      noData();
      FastList.recycle(trees);
      return;
    }

    /*
     * combine the asts
     */
    CommonTree root = _astSupport.create(JACTRBuilder.MODEL, "data");
    for (CommonTree tree : trees)
      root.addChild(tree);

    _treeViewer.setInput(root);
  }

  @Override
  protected void noData()
  {
    _treeViewer.setInput(_astSupport.create(JACTRBuilder.MODEL, "no data"));
  }

  @Override
  public void createPartControl(Composite parent)
  {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new FillLayout());

    _treeViewer = new TreeViewer(composite);
    _treeViewer.setAutoExpandLevel(4);
    _treeViewer.setContentProvider(_contentProvider);
    _treeViewer.setLabelProvider(_labelProvider);

  }

  @Override
  public void setFocus()
  {
    _treeViewer.getTree().setFocus();
  }

}
