package org.jactr.eclipse.production.content;

/*
 * default logging
 */
import java.util.Collection;
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

public class TreeContentProvider implements IGraphContentProvider
{
  /**
   * Logger definition
   */
  static private final transient Log               LOGGER     = LogFactory
                                                                  .getLog(TreeContentProvider.class);

  private Map<CommonTree, ProductionRelationships> _relationships;

  private CommonTree                               _root;

  private ProductionSequenceView                   _viewer;

  private boolean                                  _following = false;

  public TreeContentProvider(ProductionSequenceView view, CommonTree root,
      boolean following)
  {
    _viewer = view;
    _root = root;
    _following = following;
  }

  public void dispose()
  {

  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    _relationships = (Map<CommonTree, ProductionRelationships>) newInput;
  }

  public Object[] getElements(Object input)
  {
    Set<IRelationship> relationships = new HashSet<IRelationship>();

    ProductionRelationships rels = _relationships.get(_root);
    if (rels != null)
    {
      Collection<IRelationship> relations = null;
      if (_following)
        relations = rels.getTailRelationships();
      else
        relations = rels.getHeadRelationships();

      for (IRelationship rel : relations)
        if (_viewer.isShowingAmbiguous() && rel.getScore() == 0)
          relationships.add(rel);
        else if (_viewer.isShowingPositive() && rel.getScore() > 0)
          relationships.add(rel);
        else if (_viewer.isShowingNegative() && rel.getScore() < 0)
          relationships.add(rel);
    }

    return relationships.toArray();
  }

  public Object getDestination(Object rel)
  {
    if (rel instanceof IRelationship)
      return ((IRelationship) rel).getTailProduction();
    return null;
  }

  public Object getSource(Object rel)
  {
    if (rel instanceof IRelationship)
      return ((IRelationship) rel).getHeadProduction();
    return null;
  }

  // public Object[] getRelationships(Object source, Object dest)
  // {
  // if (_following && source != _root) return null;
  // if (!_following && dest != _root) return null;
  //
  // Collection<IRelationship> relationships = new ArrayList<IRelationship>();
  // ProductionRelationships relations = _relationships.get(_root);
  //
  // if (relations != null)
  // if (_following)
  // {
  // for (IRelationship relationship : relations.getTailRelationships())
  // if (relationship.getTailProduction() == dest)
  // relationships.add(relationship);
  // }
  // else
  // for (IRelationship relationship : relations.getHeadRelationships())
  // if (relationship.getHeadProduction() == source)
  // relationships.add(relationship);
  //
  // return relationships.toArray();
  // }

}
