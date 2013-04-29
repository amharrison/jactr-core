package org.jactr.eclipse.ui.content;

/*
 * default logging
 */
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.jactr.eclipse.core.ast.Support;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.io.antlr3.misc.DetailedCommonTree;

public abstract class AbstractACTRContentProvider implements
    ITreeContentProvider
{

  static private final transient Log                      LOGGER         = LogFactory
                                                                             .getLog(AbstractACTRContentProvider.class);

  protected final Map<CommonTree, CommonTree>             _parentMap;

  protected final Map<CommonTree, Collection<CommonTree>> _childrenMap;

  protected CommonTree                                    _root;

  protected boolean                                       _showOnlyLocal = false;

  protected URL                                           _source;

  public AbstractACTRContentProvider()
  {
    _parentMap = new WeakHashMap<CommonTree, CommonTree>();
    _childrenMap = new WeakHashMap<CommonTree, Collection<CommonTree>>();
  }

  public void setShowOnlyLocal(boolean showOnlyLocal)
  {
    _showOnlyLocal = showOnlyLocal;
  }
  
  public boolean isShowOnlyLocal()
  {
    return _showOnlyLocal;
  }

  public void dispose()
  {
    clear();
  }

  /**
   * clear the cache
   */
  public void clear()
  {
    _parentMap.clear();
    _childrenMap.clear();
    _root = null;
    _source = null;
  }

  public Object[] getChildren(Object parentElement)
  {
    if (parentElement instanceof ICompilationUnit)
      return getElements(parentElement);

    if (parentElement == null) return new Object[0];

    CommonTree parent = (CommonTree) parentElement;
    Collection<CommonTree> children = _childrenMap.get(parent);
    if (children == null)
    {
      children = Support.getVisibleChildren(parent, _source);
      _childrenMap.put(parent, children);

      for (CommonTree child : children)
        _parentMap.put(child, parent);
    }

    Object[] rtn = children.toArray();

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(parent.getText() + " -> " + children);

    return rtn;
  }

  protected CommonTree getRoot()
  {
    return _root;
  }

  public Object getParent(Object element)
  {
    CommonTree parent = _parentMap.get(element);
    if (LOGGER.isDebugEnabled()) LOGGER.debug(element + " <- " + parent);
    return parent;
  }

  public boolean hasChildren(Object element)
  {
    boolean rtn = getChildren(element).length != 0;
    return rtn;
  }

  public Object[] getElements(Object inputElement)
  {
    if (_root == null) return null;
    return getChildren(_root);
  }

  protected void setRoot(CommonTree root)
  {
    clear();
    _root = root;
    if(_showOnlyLocal && root instanceof DetailedCommonTree)
      _source = ((DetailedCommonTree)root).getSource();
  }

}