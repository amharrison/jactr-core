package org.jactr.eclipse.association.ui.content;

import java.util.Set;
import java.util.TreeSet;

import org.antlr.runtime.tree.CommonTree;
/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.gef.zest.fx.jface.IGraphContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.jactr.eclipse.association.ui.model.ModelAssociations;
import org.jactr.io.antlr3.misc.ASTSupport;

public class AssociativeContentProvider implements IGraphContentProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AssociativeContentProvider.class);

  private ModelAssociations          _associations;

  public void dispose()
  {

  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    _associations = (ModelAssociations) newInput;
  }

  @Override
  public Object[] getAdjacentNodes(Object node)
  {
    String chunkName = ASTSupport.getName((CommonTree) node);

    Set<String> adjacentNodes = new TreeSet<>();
//    _associations.getInboundAssociations(chunkName).forEach(ass -> {
//      adjacentNodes.add(ASTSupport.getName(ass.getJChunk()));
//    });
    _associations.getOutboundAssociations(chunkName).forEach(ass -> {
      adjacentNodes.add(ASTSupport.getName(ass.getIChunk()));
    });
    return adjacentNodes.toArray();
  }

  @Override
  public Object[] getNestedGraphNodes(Object node)
  {
    return null;
  }

  @Override
  public Object[] getNodes()
  {
    if (_associations == null) return new Object[] {};

    // this should be all the chunks not the links.

    return _associations.chunks(null).values().toArray();
  }

  @Override
  public boolean hasNestedGraph(Object node)
  {
    return false;
  }

}
