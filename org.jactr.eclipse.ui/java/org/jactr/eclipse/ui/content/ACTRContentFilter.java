package org.jactr.eclipse.ui.content;

/*
 * default logging
 */
import java.util.Set;
import java.util.TreeSet;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jactr.io.antlr3.builder.JACTRBuilder;

public class ACTRContentFilter extends ViewerFilter
{

  static private Set<Integer>        _alwaysShow = new TreeSet<Integer>();

  static
  {
    _alwaysShow.add(JACTRBuilder.BUFFER);
  }

  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element)
  {
    CommonTree node = (CommonTree) element;
    return isAnyLinkedToSource(node);
  }

  private boolean isAnyLinkedToSource(CommonTree node)
  {
    if (node.getLine() >= 0) return true;
    if(_alwaysShow.contains(node.getType())) return true;

    for (int i = 0; i < node.getChildCount(); i++)
      if (isAnyLinkedToSource((CommonTree) node.getChild(i))) return true;

    return false;
  }
}
