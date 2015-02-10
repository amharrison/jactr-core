package org.jactr.eclipse.association.ui.filter.impl;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;

public class ChunkTypeFilter extends ViewerFilter
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER        = LogFactory
                                                       .getLog(ChunkTypeFilter.class);

  private Set<String>                _excludeNames = new TreeSet<String>();

  public ChunkTypeFilter(Collection<String> excludeNames)
  {
    for (String name : excludeNames)
      _excludeNames.add(name);
  }

  @Override
  public boolean select(Viewer viewer, Object parentElement, Object element)
  {
    try
    {
      if (element instanceof CommonTree)
      {
        CommonTree node = (CommonTree) element;

        // only care about chunks..
        if (node.getType() != JACTRBuilder.CHUNK) return true;

        // now we need it's type
        CommonTree type = ASTSupport.getFirstDescendantWithType(node,
            JACTRBuilder.PARENT);

        // now we check the name.
        String name = type.getText();

        if (_excludeNames.contains(name)) return false;
      }

      return true;
    }
    catch (Exception e)
    {
      LOGGER.error(e);
      return true;
    }
  }

}
