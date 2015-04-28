package org.jactr.eclipse.association.ui.model;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;

import javolution.util.FastList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.chunk.four.ISubsymbolicChunk4;
import org.jactr.core.concurrent.ExecutorServices;
import org.jactr.eclipse.association.ui.mapper.IAssociationMapper;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;

public class ModelAssociations
{
  /**
   * Logger definition
   */
  static private final transient Log                             LOGGER = LogFactory
                                                                            .getLog(ModelAssociations.class);

  private ConcurrentSkipListMap<String, Collection<Association>> _jChunks;

  private ConcurrentSkipListMap<String, Collection<Association>> _iChunks;

  private Collection<Association>                                _associations;

  private IAssociationMapper                                     _mapper;

  private CommonTree                                             _modelDescriptor;

  private String                                                 _focus;



  public ModelAssociations(CommonTree modelDescriptor,
      IAssociationMapper mapper, String focalChunk)
  {
    this(mapper);
    _modelDescriptor = modelDescriptor;
    _focus = focalChunk;
  }

  public ModelAssociations(CommonTree modelDescriptor, IAssociationMapper mapper)
  {
    this(modelDescriptor, mapper, null);
  }

  public ModelAssociations(IAssociationMapper mapper)
  {
    _mapper = mapper;
    _jChunks = new ConcurrentSkipListMap<String, Collection<Association>>();
    _iChunks = new ConcurrentSkipListMap<String, Collection<Association>>();
    _associations = Collections
        .synchronizedCollection(new HashSet<Association>());
  }

  public CommonTree getModelDescriptor()
  {
    return _modelDescriptor;
  }

  public void process()
  {
    try
    {
      process(Runtime.getRuntime().availableProcessors(),
          ExecutorServices.INLINE_EXECUTOR).get();
    }
    catch (Exception e)
    {
      LOGGER.error("Failed to extract model associations", e);
    }
  }

  /**
   * @param maximumProcesses
   * @param executor
   * @return
   */
  public CompletableFuture<Void> process(final int maximumProcesses,
      final Executor executor)
  {
    /*
     * first a runnable for the map of all chunks
     */
    CompletableFuture<Map<String, CommonTree>> allChunksFuture = CompletableFuture
        .supplyAsync(
            () -> {
              Map<String, CommonTree> map = ASTSupport.getMapOfTrees(
                  _modelDescriptor, JACTRBuilder.CHUNK);
              if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format("Extracted map of trees [%d]",
                    map.size()));
              return map;
            }, executor);

    /*
     * once that is done, we can split
     */
    CompletableFuture<Void> subProcs = allChunksFuture
        .thenComposeAsync(
            (m) -> {

              Collection<CompletableFuture<Void>> submitted = new ArrayList<CompletableFuture<Void>>();
              FastList<CommonTree> allChunks = FastList.newInstance();

              int blockSize = m.size() / maximumProcesses;
              final Map<String, CommonTree> allChunksMap = m;

              if (LOGGER.isDebugEnabled())
                LOGGER.debug(String.format(
                    "Subdividing %s chunks into %d blocks", m.size(),
                    maximumProcesses));
              try
              {
                allChunks.addAll(m.values());

                for (int i = 0; i < maximumProcesses; i++)
                {
                  //
                  final Collection<CommonTree> subList = allChunks.subList(i
                      * blockSize, i * blockSize + blockSize);
                  submitted.add(CompletableFuture.runAsync(() -> {
                    process(subList, allChunksMap);
                  }, executor));
                }
              }
              catch (Exception e)
              {
                LOGGER.error("Failed to dispatch subprocesses ", e);
              }

              CompletableFuture<Void> allDone = CompletableFuture
                  .allOf(submitted.toArray(new CompletableFuture[submitted
                      .size()]));

              allDone.handle((v, t) -> {
                if (t != null)
                  LOGGER.error("Failed to complete all processes", t);
                else
                  LOGGER.debug("processing complete");
                return null;
              });

              return allDone;
            }, executor);

    return subProcs;
  }

  private void process(Collection<CommonTree> chunksToProcess,
      Map<String, CommonTree> allChunks)
  {
    Map<String, CommonTree> recycledParameters = new TreeMap<String, CommonTree>();

    if (LOGGER.isDebugEnabled())
      LOGGER
          .debug(String.format("Processing %d chunks", chunksToProcess.size()));

    for (CommonTree jChunk : chunksToProcess)
      process(ASTSupport.getName(jChunk), jChunk, recycledParameters, allChunks);
  }

  private void process(String jChunkName, CommonTree jChunk,
      Map<String, CommonTree> recycledParameters,
      Map<String, CommonTree> allChunks)
  {
    recycledParameters.clear();

    recycledParameters = ASTSupport.getMapOfTrees(jChunk,
        JACTRBuilder.PARAMETER, recycledParameters);

    String linkKey = ISubsymbolicChunk4.LINKS.toLowerCase();

    if (!recycledParameters.containsKey(linkKey)) return;

    String allLinks = recycledParameters.get(linkKey).getChild(1).getText();

    try
    {
      for (Association association : _mapper.extractAssociations(allLinks,
          jChunk, allChunks))
        if (_focus == null
            || _focus.equals(ASTSupport.getName(association.getJChunk()))
            || _focus.equals(ASTSupport.getName(association.getIChunk())))
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("adding Link: j:%s i:%s str:%.2f",
                association.getJChunk().toStringTree(), association.getIChunk()
                    .toStringTree(), association.getStrength()));

          addAssociation(association);
        }

    }
    catch (Exception e)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn(String.format("Failed to extract link info from %s",
 allLinks), e);
    }

  }

  // private void process(CommonTree modelDescriptor, String focus)
  // {
  // _modelDescriptor = modelDescriptor;
  // Map<String, CommonTree> allChunks = ASTSupport.getMapOfTrees(
  // modelDescriptor, JACTRBuilder.CHUNK);
  //
  // String linkKey = ISubsymbolicChunk4.LINKS.toLowerCase();
  // Map<String, CommonTree> parameters = new TreeMap<String, CommonTree>();
  // for (Map.Entry<String, CommonTree> jChunk : allChunks.entrySet())
  // {
  // /*
  // * snag the parameter needed
  // */
  // parameters.clear();
  // parameters = ASTSupport.getMapOfTrees(jChunk.getValue(),
  // JACTRBuilder.PARAMETER, parameters);
  //
  // if (!parameters.containsKey(linkKey)) continue;
  //
  // String allLinks = parameters.get(linkKey).getChild(1).getText();
  //
  // try
  // {
  // for (Association association : _mapper.extractAssociations(allLinks,
  // jChunk.getValue(), allChunks))
  // if (focus == null
  // || focus.equals(ASTSupport.getName(association.getJChunk()))
  // || focus.equals(ASTSupport.getName(association.getIChunk())))
  // {
  // if (LOGGER.isDebugEnabled())
  // LOGGER.debug(String.format("adding Link: j:%s i:%s str:%.2f",
  // association.getJChunk().toStringTree(), association
  // .getIChunk().toStringTree(), association.getStrength()));
  //
  // addAssociation(association);
  // }
  //
  // }
  // catch (Exception e)
  // {
  // if (LOGGER.isWarnEnabled())
  // LOGGER.warn(String.format("Failed to extract link info from %s",
  // allLinks));
  // }
  // }
  // }

  public void addAssociation(Association association)
  {
    String j = ASTSupport.getName(association.getJChunk()).toLowerCase();
    String i = ASTSupport.getName(association.getIChunk()).toLowerCase();

    // if (LOGGER.isDebugEnabled())
    // LOGGER.debug(String.format("Adding j:%s i:%s %d %.2f", j, i,
    // association.getCount(), association.getStrength()));

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
      ConcurrentSkipListMap<String, Collection<Association>> container)
  {
    FastList<Association> addIfMissing = FastList.newInstance();
    Collection<Association> collection = container.putIfAbsent(name,
        addIfMissing);

    // we already had a collection attached
    if (collection != null)
      FastList.recycle(addIfMissing);
    else
      collection = addIfMissing;

    collection.add(association);
  }
}
