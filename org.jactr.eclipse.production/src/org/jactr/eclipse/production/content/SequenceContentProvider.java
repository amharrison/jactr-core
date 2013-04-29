package org.jactr.eclipse.production.content;

/*
 * default logging
 */
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.zest.core.viewers.IGraphContentProvider;
import org.jactr.eclipse.production.view.ProductionSequenceView;
import org.jactr.tools.analysis.production.relationships.IRelationship;
import org.jactr.tools.analysis.production.relationships.ProductionRelationships;

public class SequenceContentProvider implements IGraphContentProvider
{
  /**
   * Logger definition
   */
  static private final transient Log               LOGGER = LogFactory
                                                              .getLog(SequenceContentProvider.class);

  private Map<CommonTree, ProductionRelationships> _relationships;

  private CommonTree                               _root;

  private ProductionSequenceView                   _view;

  private int                                      _depth = 2;

  public SequenceContentProvider(ProductionSequenceView view, CommonTree root,
      int depth)
  {
    _root = root;
    _depth = depth;
    _view = view;
  }

  public Object[] getElements(Object inputElement)
  {
    if (_relationships == null) return new Object[0];

    HashSet<IRelationship> relationships = new HashSet<IRelationship>();

    HashSet<CommonTree> processedHeads = new HashSet<CommonTree>();
    HashSet<CommonTree> processedTails = new HashSet<CommonTree>();

    getUpstreamRelationships(_root, _depth, processedHeads, processedTails,
        relationships);
    getDownstreamRelationships(_root, _depth, processedHeads, processedTails,
        relationships);

    return relationships.toArray();
  }

  public void dispose()
  {

  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    _relationships = (Map<CommonTree, ProductionRelationships>) newInput;
  }

  protected void getUpstreamRelationships(CommonTree root, int depth,
      Set<CommonTree> processedHeads, Set<CommonTree> processedTails,
      Set<IRelationship> relationships)
  {
    /*
     * by the time we reach one, the node is terminal and we dont want any of
     * its relationships
     */
    if (depth <= 1) return;

    ProductionRelationships rels = _relationships.get(root);
    if (rels == null) return;

    Set<CommonTree> toFollow = new HashSet<CommonTree>();

    for (IRelationship relation : rels.getHeadRelationships())
      if (_view.isShowingAmbiguous() && relation.getScore() == 0)
      {
        relationships.add(relation);
        toFollow.add(relation.getHeadProduction());
      }
      else if (_view.isShowingPositive() && relation.getScore() > 0)
      {
        relationships.add(relation);
        toFollow.add(relation.getHeadProduction());
      }
      else if (_view.isShowingNegative() && relation.getScore() < 0)
      {
        relationships.add(relation);
        toFollow.add(relation.getHeadProduction());
      }

    processedHeads.add(root);

    for (CommonTree node : toFollow)
    {
//      if (!processedHeads.contains(node))
        getUpstreamRelationships(node, depth - 1, processedHeads,
            processedTails, relationships);

//      if (!processedTails.contains(node))
//        getDownstreamRelationships(root, depth - 1, processedHeads,
//            processedTails, relationships);
    }
  }

  protected void getDownstreamRelationships(CommonTree root, int depth,
      Set<CommonTree> processedHeads, Set<CommonTree> processedTails,
      Set<IRelationship> relationships)
  {
    if (depth <= 1) return;

    ProductionRelationships rels = _relationships.get(root);
    if (rels == null) return;

    Set<CommonTree> toFollow = new HashSet<CommonTree>();

    for (IRelationship relation : rels.getTailRelationships())
      if (_view.isShowingAmbiguous() && relation.getScore() == 0)
      {
        relationships.add(relation);
        toFollow.add(relation.getTailProduction());
      }
      else if (_view.isShowingPositive() && relation.getScore() > 0)
      {
        relationships.add(relation);
        toFollow.add(relation.getTailProduction());
      }
      else if (_view.isShowingNegative() && relation.getScore() < 0)
      {
        relationships.add(relation);
        toFollow.add(relation.getTailProduction());
      }

    processedTails.add(root);

    for (CommonTree node : toFollow)
    {
//      if (!processedHeads.contains(node))
//        getUpstreamRelationships(node, depth - 1, processedHeads,
//            processedTails, relationships);

//      if (!processedTails.contains(node))
        getDownstreamRelationships(root, depth - 1, processedHeads,
            processedTails, relationships);
    }
  }

  public Object getDestination(Object rel)
  {
    return ((IRelationship) rel).getTailProduction();
  }

  public Object getSource(Object rel)
  {
    return ((IRelationship) rel).getHeadProduction();
  }

}
