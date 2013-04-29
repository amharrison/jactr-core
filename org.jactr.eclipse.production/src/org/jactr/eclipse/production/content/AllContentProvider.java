package org.jactr.eclipse.production.content;

/*
 * default logging
 */
import java.util.ArrayList;
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

public class AllContentProvider implements IGraphContentProvider
{
  /**
   * Logger definition
   */
  static private final transient Log               LOGGER = LogFactory
                                                              .getLog(AllContentProvider.class);

  private Map<CommonTree, ProductionRelationships> _relationships;
  private ProductionSequenceView _viewer;
  
  
  public AllContentProvider(ProductionSequenceView view)
  {
    _viewer = view;
  }
  
  public Object[] getElements(Object input)
  {
    Set<IRelationship> relationships = new HashSet<IRelationship>();
    ArrayList<IRelationship> headsAndTails = new ArrayList<IRelationship>();

    for(ProductionRelationships rels : _relationships.values())
    {
      headsAndTails.addAll(rels.getHeadRelationships());
      headsAndTails.addAll(rels.getTailRelationships());
      
      for (IRelationship rel : headsAndTails)
        if (_viewer.isShowingAmbiguous() && rel.getScore() == 0)
          relationships.add(rel);
        else if (_viewer.isShowingPositive() && rel.getScore() > 0)
          relationships.add(rel);
        else if (_viewer.isShowingNegative() && rel.getScore() < 0)
          relationships.add(rel);
      
      headsAndTails.clear();
    }

    return relationships.toArray();
  }

  // public Object[] getElements(Object inputElement)
  // {
  // if (_relationships == null) return new Object[0];
  //
  // /*
  // * return only relationships in one direction
  // */
  // // Collection<IRelationship> relationships = new
  // ArrayList<IRelationship>();
  // // for (ProductionRelationships relationship : _relationships.values())
  // // for (IRelationship relation : relationship.getHeadRelationships())
  // // if (relation.getScore() != 0) relationships.add(relation);
  // //
  // // return relationships.toArray();
  // return _relationships.keySet().toArray();
  // }

  public void dispose()
  {

  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
  {
    _relationships = (Map<CommonTree, ProductionRelationships>) newInput;
  }

  // public Object[] getRelationships(Object source, Object dest)
  // {
  // Collection<IRelationship> relationships = new ArrayList<IRelationship>();
  // ProductionRelationships relations = _relationships.get(source);
  //
  // if (relations != null)
  // {
  // for (IRelationship relation : relations.getTailRelationships())
  // if (relation.getTailProduction() == dest) relationships.add(relation);
  // }
  //
  // return relationships.toArray();
  // }

  public Object getDestination(Object rel)
  {
    return ((IRelationship) rel).getTailProduction();
  }

  public Object getSource(Object rel)
  {
    return ((IRelationship) rel).getHeadProduction();
  }

}
