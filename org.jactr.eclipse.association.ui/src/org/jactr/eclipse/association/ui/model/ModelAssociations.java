package org.jactr.eclipse.association.ui.model;

/*
 * default logging
 */
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import javolution.util.FastList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.chunk.four.ISubsymbolicChunk4;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;

public class ModelAssociations
{
  /**
   * Logger definition
   */
  static private final transient Log           LOGGER = LogFactory
                                                          .getLog(ModelAssociations.class);

  private Map<String, Collection<Association>> _jChunks;

  private Map<String, Collection<Association>> _iChunks;

  private Collection<Association>              _associations;

  public ModelAssociations(CommonTree modelDescriptor)
  {
    this();
    process(modelDescriptor, null);
  }

  public ModelAssociations(CommonTree modelDescriptor, String focalChunk)
  {
    this();
    process(modelDescriptor, focalChunk);
  }

  public ModelAssociations()
  {
    _jChunks = new TreeMap<String, Collection<Association>>();
    _iChunks = new TreeMap<String, Collection<Association>>();
    _associations = new HashSet<Association>();
  }

  private void process(CommonTree modelDescriptor, String focus)
  {
    Map<String, CommonTree> allChunks = ASTSupport.getMapOfTrees(
        modelDescriptor, JACTRBuilder.CHUNK);

    String linkKey = ISubsymbolicChunk4.LINKS.toLowerCase();
    Map<String, CommonTree> parameters = new TreeMap<String, CommonTree>();
    for (Map.Entry<String, CommonTree> jChunk : allChunks.entrySet())
    {
      /*
       * snag the parameter needed
       */
      parameters.clear();
      parameters = ASTSupport.getMapOfTrees(jChunk.getValue(),
          JACTRBuilder.PARAMETER, parameters);

      if (!parameters.containsKey(linkKey)) continue;

      String allLinks = parameters.get(linkKey).getChild(1).getText();
      /*
       * ((ichunk count strength), ...)
       */
      String[] links = allLinks.split(",");
      for (String link : links)
        try
        {
          link = link.substring(link.lastIndexOf('(') + 1, link.indexOf(')'));
          String[] components = link.split(" ");
          String iChunk = components[0].toLowerCase();
          int count = Integer.parseInt(components[1]);
          double rStrength = Double.parseDouble(components[2]);

          if (focus == null || focus.equals(jChunk.getKey())
              || focus.equals(iChunk))
          {
            Association association = new Association(jChunk.getValue(),
                allChunks.get(iChunk), count, rStrength);
            addAssociation(association);
          }
        }
        catch (Exception e)
        {
          if (LOGGER.isWarnEnabled())
            LOGGER.warn(String.format("Failed to extract link info from %s",
                link));
        }
    }
  }

  public void addAssociation(Association association)
  {
    String j = ASTSupport.getName(association.getJChunk()).toLowerCase();
    String i = ASTSupport.getName(association.getIChunk()).toLowerCase();

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Adding j:%s i:%s %d %.2f", j, i, association
          .getCount(), association.getStrength()));

    add(j, association, _jChunks);
    add(i, association, _iChunks);
    _associations.add(association);
  }

  public Association[] getAssociations()
  {
    return _associations.toArray(new Association[0]);
  }

  private void add(String name, Association association,
      Map<String, Collection<Association>> container)
  {
    Collection<Association> collection = container.get(name);
    if (collection == null)
    {
      collection = new FastList<Association>();
      container.put(name, collection);
    }

    collection.add(association);
  }
}
