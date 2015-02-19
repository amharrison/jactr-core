package org.jactr.eclipse.association.ui.mapper.impl;

/*
 * default logging
 */
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.association.ui.mapper.IAssociationMapper;
import org.jactr.eclipse.association.ui.model.Association;
import org.jactr.io.antlr3.misc.ASTSupport;

public abstract class AbstractAssociationMapper implements IAssociationMapper
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractAssociationMapper.class);

  public AbstractAssociationMapper()
  {
  }

  @Override
  public String getLabel(Association association)
  {
    return String.format("%.2f", association.getStrength());
  }

  @Override
  public String getLabel(CommonTree element)
  {
    return ASTSupport.getName(element);
  }

  @Override
  public String getToolTip(Association association)
  {
    return String.format("Strength %.2f", association.getStrength());
  }

  @Override
  public String getToolTip(CommonTree element)
  {
    return ASTSupport.getName(element);
  }


}
