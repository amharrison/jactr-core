package org.jactr.eclipse.association.ui.mapper.impl;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.association.ui.model.Association;

public class DefaultAssociationMapper extends AbstractAssociationMapper
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(DefaultAssociationMapper.class);

  public DefaultAssociationMapper()
  {

  }

  @Override
  public Collection<Association> extractAssociations(String associationString,
      CommonTree jChunk, Map<String, CommonTree> allChunks)
  {
    ArrayList<Association> rtn = new ArrayList<Association>();

    String[] links = associationString.split(",");
    for (String link : links)
      try
      {
        /*
         * expecting (iChunkName count strength)
         */
        link = link.substring(link.lastIndexOf('(') + 1, link.indexOf(')'));
        String[] components = link.split(" ");
        String iChunk = components[0].toLowerCase();
        int count = Integer.parseInt(components[1]);
        double strength = Double.parseDouble(components[2]);

        Association association = new Association(jChunk,
            allChunks.get(iChunk), count, strength);
        rtn.add(association);

      }
      catch (Exception e)
      {
        if (LOGGER.isWarnEnabled())
          LOGGER.warn(String
              .format("Failed to extract link info from %s", link));
      }

    return rtn;
  }

}
