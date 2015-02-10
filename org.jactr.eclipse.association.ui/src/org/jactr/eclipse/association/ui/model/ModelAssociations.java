package org.jactr.eclipse.association.ui.model;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import javolution.util.FastList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.chunk.four.ISubsymbolicChunk4;
import org.jactr.eclipse.association.ui.mapper.IAssociationMapper;
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

  private IAssociationMapper                   _mapper;

  private CommonTree                           _modelDescriptor;

  public ModelAssociations(CommonTree modelDescriptor, IAssociationMapper mapper)
  {
    this(mapper);
    process(modelDescriptor, null);
  }

  public ModelAssociations(CommonTree modelDescriptor,
      IAssociationMapper mapper, String focalChunk)
  {
    this(mapper);
    process(modelDescriptor, focalChunk);
  }

  public ModelAssociations(IAssociationMapper mapper)
  {
    _mapper = mapper;
    _jChunks = new TreeMap<String, Collection<Association>>();
    _iChunks = new TreeMap<String, Collection<Association>>();
    _associations = new HashSet<Association>();
  }

  public CommonTree getModelDescriptor()
  {
    return _modelDescriptor;
  }

  private void process(CommonTree modelDescriptor, String focus)
  {
    _modelDescriptor = modelDescriptor;
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

      try
      {
      for (Association association : _mapper.extractAssociations(allLinks,
            jChunk.getValue(), allChunks))
        if (focus == null
            || focus.equals(ASTSupport.getName(association.getJChunk()))
            || focus.equals(ASTSupport.getName(association.getIChunk())))
          {
            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format("adding Link: j:%s i:%s str:%.2f",
                  association.getJChunk().toStringTree(), association
                      .getIChunk().toStringTree(), association.getStrength()));

          addAssociation(association);
          }

        }
        catch (Exception e)
        {
          if (LOGGER.isWarnEnabled())
          LOGGER.warn(String.format("Failed to extract link info from %s",
              allLinks));
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

  public Collection<Association> getOutboundAssociations(String jChunkName)
  {
    return get(jChunkName, _jChunks);
  }

  public Collection<Association> getInboundAssociations(String iChunkName)
  {
    return get(iChunkName, _iChunks);
  }

  protected Collection<Association> get(String keyName,
      Map<String, Collection<Association>> map)
  {
    Collection<Association> rtn = map.get(keyName.toLowerCase());
    if (rtn == null)
      rtn = Collections.EMPTY_LIST;
    else
      rtn = Collections.unmodifiableCollection(rtn);
    return rtn;
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
