package org.jactr.eclipse.production.render;

/*
 * default logging
 */
import java.text.NumberFormat;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IEntityStyleProvider;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.jactr.eclipse.production.view.ProductionSequenceView;
import org.jactr.eclipse.ui.content.ACTRLabelProvider;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.tools.analysis.production.relationships.IRelationship;
import org.jactr.tools.analysis.production.relationships.ProductionRelationships;

public class ProductionSequenceViewLabelProvider extends ACTRLabelProvider
    implements IConnectionStyleProvider,
    IEntityStyleProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProductionSequenceViewLabelProvider.class);

  private NumberFormat               _format;

  private Color                      _negative;

  private Color                      _positiveFollow;

  private Color                      _positiveProceed;

  private Color                      _ambiguous;

  private ProductionSequenceView     _viewer;

  public ProductionSequenceViewLabelProvider(ProductionSequenceView viewer)
  {
    _viewer = viewer;
    _format = NumberFormat.getNumberInstance();
    _format.setMinimumFractionDigits(2);
    _format.setMaximumFractionDigits(2);

    _negative = new Color(Display.getCurrent(), 255, 0, 0);
    _positiveFollow = new Color(Display.getCurrent(), 0, 255, 0);
    _positiveProceed = new Color(Display.getCurrent(), 30, 144, 255);
    _ambiguous = new Color(Display.getCurrent(), 127, 127, 127);
  }

  @Override
  public void dispose()
  {
    super.dispose();
    _negative.dispose();
    _positiveFollow.dispose();
    _ambiguous.dispose();
    _positiveProceed.dispose();
  }

  @Override
  public String getText(Object element)
  {
    if (element instanceof IRelationship) return "";
    return super.getText(element);
  }

  public Color getColor(Object rel)
  {
    if (proceedsCurrentProduction(rel)) return _positiveProceed;
    if (followsCurrentProduction(rel)) return _positiveFollow;

    double score = getScore(rel);
    if (score > 0) return _positiveFollow;
    if (score < 0) return _negative;
    return _ambiguous;
  }

  protected double getScore(Object rel)
  {
    IRelationship relationship = (IRelationship) rel;
    return relationship.getScore();
  }

  public int getConnectionStyle(Object rel)
  {
    int style = ZestStyles.CONNECTIONS_DIRECTED;

    if (proceedsCurrentProduction(rel) || followsCurrentProduction(rel))
      style |= ZestStyles.CONNECTIONS_SOLID;
    else
      style |= ZestStyles.CONNECTIONS_DOT;

    return style;
  }

  public Color getHighlightColor(Object rel)
  {
    return getColor(rel);
  }

  public int getLineWidth(Object rel)
  {
    if (proceedsCurrentProduction(rel) || followsCurrentProduction(rel))
      return 2;

    return 0;
  }

  public IFigure getTooltip(Object entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  private int compareNames(Object rel)
  {
    IRelationship relationship = (IRelationship) rel;
    String headName = ASTSupport.getName(relationship.getHeadProduction());
    String tailName = ASTSupport.getName(relationship.getTailProduction());
    return headName.compareToIgnoreCase(tailName);
  }

  public double getEndAngle(Object rel)
  {
    int comp = compareNames(rel);
    double score = getScore(rel);
    if (score == 0) return Double.NaN;

    return comp * 30;
  }

  public double getEndDistance(Object rel)
  {
    return 0.5;
  }

  public double getStartAngle(Object rel)
  {
    return getEndAngle(rel);
  }

  public double getStartDistance(Object rel)
  {
    return 0.5;
  }

  /**
   * uses the viewer's selection and input to determine if the this node or
   * connection is following the selection
   * 
   * @return
   */
  protected boolean followsCurrentProduction(Object node)
  {
    CommonTree production = _viewer.getSelectedProduction();

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Checking to see if " + node + " is connected to selection "
          + production);

    if (production == null) return false;

    Map<CommonTree, ProductionRelationships> rels = _viewer
        .getInput();
    ProductionRelationships relationships = rels.get(production);
    if (relationships == null) return false;
    for (IRelationship relation : relationships.getTailRelationships())
      if ((node == relation || relation.getTailProduction() == node)
          && relation.getScore() > 0)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(node + " follows " + production);
        return true;
      }

    return false;
  }

  protected boolean proceedsCurrentProduction(Object node)
  {
    CommonTree production = _viewer.getSelectedProduction();

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Checking to see if " + node + " is connected to selection "
          + production);

    if (production == null) return false;

    Map<CommonTree, ProductionRelationships> rels = _viewer
        .getInput();
    ProductionRelationships relationships = rels.get(production);
    if (relationships == null) return false;
    for (IRelationship relation : relationships.getHeadRelationships())
      if ((relation == node || relation.getHeadProduction() == node)
          && relation.getScore() > 0)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(node + " proceeds " + production);
        return true;
      }

    return false;
  }

  public boolean fisheyeNode(Object entity)
  {
    return true;
  }

  public Color getBackgroundColour(Object entity)
  {
    if (followsCurrentProduction(entity)) return _positiveFollow;
    if (proceedsCurrentProduction(entity)) return _positiveProceed;
    return null;
  }

  public Color getBorderColor(Object entity)
  {
    if (proceedsCurrentProduction(entity)) return _positiveProceed;
    if (followsCurrentProduction(entity)) return _positiveFollow;
    return null;
  }

  public Color getBorderHighlightColor(Object entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public int getBorderWidth(Object entity)
  {
    if (proceedsCurrentProduction(entity) || followsCurrentProduction(entity)) return 2;
    return -1;
  }

  public Color getForegroundColour(Object entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Color getNodeHighlightColor(Object entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public ConnectionRouter getRouter(Object rel)
  {
    // TODO Auto-generated method stub
    return null;
  }
}
