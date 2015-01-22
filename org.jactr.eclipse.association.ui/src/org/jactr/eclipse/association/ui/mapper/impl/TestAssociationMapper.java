package org.jactr.eclipse.association.ui.mapper.impl;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.association.ui.model.Association;
import org.jactr.io.antlr3.misc.ASTSupport;

public class TestAssociationMapper extends DefaultAssociationMapper
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(TestAssociationMapper.class);

  public TestAssociationMapper()
  {
  }

  @Override
  public String getLabel(Association association)
  {
    return String.format("j:%s i:%s %.2f",
        ASTSupport.getName(association.getJChunk()),
        ASTSupport.getName(association.getIChunk()), association.getStrength());
  }
}
