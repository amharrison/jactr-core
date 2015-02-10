package org.jactr.eclipse.association.ui.mapper;

import java.util.Collection;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.jactr.eclipse.association.ui.model.Association;

/*
 * default logging
 */

/**
 * associations can be defined in many ways, based on customization at the
 * architectural level. This mapper allows modelers to provide new parsers for
 * extracting associations and new label providers.
 * 
 * @author harrison
 */
public interface IAssociationMapper
{

  public String getLabel(Association association);

  public String getLabel(CommonTree element);

  public String getToolTip(CommonTree element);

  public String getToolTip(Association association);

  /**
   * extract the Associations for jChunk defined in the string. Other chunk
   * references are provided in the map
   * 
   * @param associationString
   * @param jChunk
   * @param allChunks
   * @return
   */
  public Collection<Association> extractAssociations(String associationString,
      CommonTree jChunk, Map<String, CommonTree> allChunks);
}
