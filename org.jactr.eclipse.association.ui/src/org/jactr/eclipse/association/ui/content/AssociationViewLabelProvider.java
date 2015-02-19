package org.jactr.eclipse.association.ui.content;

/*
 * default logging
 */
import java.text.NumberFormat;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.zest.core.viewers.IConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IEntityStyleProvider;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.jactr.eclipse.association.ui.mapper.IAssociationMapper;
import org.jactr.eclipse.association.ui.model.Association;
import org.jactr.eclipse.association.ui.views.AssociationViewer;
import org.jactr.eclipse.ui.content.ACTRLabelProvider;

public class AssociationViewLabelProvider extends ACTRLabelProvider implements
    IConnectionStyleProvider, IEntityStyleProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AssociationViewLabelProvider.class);

  private NumberFormat               _format;

  private AssociationViewer          _viewer;

  private Color                      _incoming;

  private Color                      _outgoing;

  private Color                      _default;

  private Color                      _selected;

  private IAssociationMapper         _mapper;

  public AssociationViewLabelProvider(AssociationViewer viewer,
      IAssociationMapper mapper)
  {
    _mapper = mapper;
    _format = NumberFormat.getNumberInstance();
    _format.setMinimumFractionDigits(2);
    _format.setMaximumFractionDigits(2);
    _viewer = viewer;

    _incoming = new Color(Display.getCurrent(), new RGB(0, 128, 0));
    _outgoing = new Color(Display.getCurrent(), new RGB(128, 0, 0));
    _default = new Color(Display.getCurrent(), new RGB(128, 128, 128));
    _selected = new Color(Display.getCurrent(), new RGB(0, 0, 128));
  }

  public void setMapper(IAssociationMapper mapper)
  {
    _mapper = mapper;
  }

  @Override
  public void dispose()
  {
    _incoming.dispose();
    _outgoing.dispose();
    _default.dispose();
    _selected.dispose();
    super.dispose();
  }

  @Override
  public String getText(Object element)
  {
    if (element instanceof Association)
      return _mapper.getLabel((Association) element);
    if (element instanceof CommonTree)
      return _mapper.getLabel((CommonTree) element);
    return "";
  }

  public Color getColor(Object rel)
  {
    // if (proceedsCurrentProduction(rel)) return _positiveProceed;
    // if (followsCurrentProduction(rel)) return _positiveFollow;

    // double score = getScore(rel);
    // if (score > 0) return _positiveFollow;
    // if (score < 0) return _negative;
    Object selection = _viewer.getSelection();
    /*
     * could be a commonTree, in which case, we color the out going and in
     * coming differently if it is an association, we just use the default
     * highlight color. if there is no selection, we return null
     */

    if (selection instanceof Association) return _default;

    if (selection instanceof CommonTree)
    {
      Association ass = (Association) rel;
      if (ass.getIChunk().equals(selection)) return _outgoing;
      if (ass.getJChunk().equals(selection)) return _incoming;
    }

    return _default;
  }

  // protected double getScore(Object rel)
  // {
  // IRelationship relationship = (IRelationship) rel;
  // return relationship.getScore();
  // }

  public int getConnectionStyle(Object rel)
  {
    int style = ZestStyles.CONNECTIONS_DIRECTED;

    // if (proceedsCurrentProduction(rel) || followsCurrentProduction(rel))
    style |= ZestStyles.CONNECTIONS_SOLID;
    // else
    // style |= ZestStyles.CONNECTIONS_DOT;

    return style;
  }

  public Color getHighlightColor(Object rel)
  {
    return _selected;
  }

  public int getLineWidth(Object rel)
  {
    // if (proceedsCurrentProduction(rel) || followsCurrentProduction(rel))
    return 2;

    // return 0;
  }

  public IFigure getTooltip(Object element)
  {
    if (element instanceof Association)
      return new Label(_mapper.getToolTip((Association) element));
    if (element instanceof CommonTree)
      return new Label(_mapper.getToolTip((CommonTree)element));
    return null;
  }



  public boolean fisheyeNode(Object entity)
  {
    return true;
  }

  public Color getBackgroundColour(Object entity)
  {
    // if (followsCurrentProduction(entity)) return _positiveFollow;
    // if (proceedsCurrentProduction(entity)) return _positiveProceed;
    return null;
  }

  public Color getBorderColor(Object entity)
  {
    // if (proceedsCurrentProduction(entity)) return _positiveProceed;
    // if (followsCurrentProduction(entity)) return _positiveFollow;
    return null;
  }

  public Color getBorderHighlightColor(Object entity)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public int getBorderWidth(Object entity)
  {
    // if (proceedsCurrentProduction(entity) ||
    // followsCurrentProduction(entity)) return 2;
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


}
