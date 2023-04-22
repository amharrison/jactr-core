package org.jactr.core.module.procedural.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;
import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.chunktype.event.ChunkTypeEvent;
import org.jactr.core.chunktype.event.ChunkTypeListenerAdaptor;
import org.jactr.core.chunktype.event.IChunkTypeListener;
import org.jactr.core.module.declarative.event.DeclarativeModuleEvent;
import org.jactr.core.module.declarative.event.DeclarativeModuleListenerAdaptor;
import org.jactr.core.module.declarative.event.IDeclarativeModuleListener;
import org.jactr.core.module.procedural.IProceduralModule;
import org.jactr.core.production.IProduction;
import org.jactr.core.production.condition.ChunkTypeCondition;
import org.jactr.core.production.condition.IBufferCondition;
import org.jactr.core.production.condition.ICondition;
import org.jactr.core.utils.collections.FastSetFactory;
import org.slf4j.LoggerFactory;

/**
 * generic storage for productions.
 * 
 * @author harrison
 */
public class DefaultProductionStorage implements IProductionStorage
{

  static private final transient org.slf4j.Logger        LOGGER                        = LoggerFactory
      .getLogger(DefaultProductionStorage.class);

  private ReentrantReadWriteLock                         _lock                         = new ReentrantReadWriteLock();

  /**
   * keyed by buffer name (null for ambiguous), then chunktype (null for query
   * or ambiguous) to get the set
   */
  private Map<String, Map<IChunkType, Set<IProduction>>> _productionMap                = new TreeMap<>();

  protected Map<String, IProduction>                     _allProductionsByName         = new TreeMap<>();

  private Collection<IProduction>                        _allProductions               = new ArrayList<>();

  /**
   * used to detect new (extended or refined) chunktypes that may require
   * reindexing
   */
  private IChunkTypeListener                             _chunkTypeListener            = new ChunkTypeListenerAdaptor() {
                                                                                         @Override
                                                                                         public void childAdded(
                                                                                             ChunkTypeEvent cte)
                                                                                         {
                                                                                                                                                   /*
                                                                                                                                                    * reindex
                                                                                                                                                    */
                                                                                           Set<IProduction> candidates = FastSetFactory
                                                                                               .newInstance();
                                                                                           for (IActivationBuffer buffer : getProceduralModule()
                                                                                               .getModel()
                                                                                               .getActivationBuffers())
                                                                                           {
                                                                                             candidates
                                                                                                 .clear();
                                                                                             String bufferName = buffer
                                                                                                 .getName()
                                                                                                 .toLowerCase();
                                                                                             getPossibleProductions(
                                                                                                 bufferName,
                                                                                                 cte.getSource(),
                                                                                                 candidates);

                                                                                             for (IProduction production : candidates)
                                                                                               addToProductionMap(
                                                                                                   bufferName,
                                                                                                   cte.getChild(),
                                                                                                   production);
                                                                                           }

                                                                                           FastSetFactory
                                                                                               .recycle(
                                                                                                   candidates);
                                                                                         }
                                                                                       };

  /**
   * so we know when a new chunktype is encoded, allowing us to attach our
   * listener
   */
  private IDeclarativeModuleListener                     _declarativeListener          = new DeclarativeModuleListenerAdaptor() {
                                                                                         @Override
                                                                                         public void chunkTypeAdded(
                                                                                             DeclarativeModuleEvent dme)
                                                                                         {
                                                                                           // inline
                                                                                           dme.getChunkType()
                                                                                               .addListener(
                                                                                                   _chunkTypeListener,
                                                                                                   null);
                                                                                         }
                                                                                       };

  private boolean                                        _declarativeListenerInstalled = false;

  private IProceduralModule                              _module;

  public DefaultProductionStorage()
  {

  }

  /*
   * (non-Javadoc)
   * @see org.jactr.core.module.procedural.storage.IProductionStorage#
   * setProceduralModule(org.jactr.core.module.procedural.IProceduralModule)
   */
  @Override
  public void setProceduralModule(IProceduralModule module)
  {
    if (_module != null && module == null && _module.getModel() != null
        && _module.getModel().getDeclarativeModule() != null) // remove
                                                              // listeneer
      _module.getModel().getDeclarativeModule()
          .removeListener(_declarativeListener);

    _module = module;

  }

  /*
   * (non-Javadoc)
   * @see org.jactr.core.module.procedural.storage.IProductionStorage#
   * getProceduralModule()
   */
  @Override
  public IProceduralModule getProceduralModule()
  {
    return _module;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.jactr.core.module.procedural.storage.IProductionStorage#add(org.jactr.
   * core.production.IProduction)
   */
  @Override
  public IProduction add(IProduction productionToAdd)
  {

    try
    {
      return writeLocked(() -> {
        if (!_declarativeListenerInstalled)
        {
          getProceduralModule().getModel().getDeclarativeModule()
              .addListener(_declarativeListener, null);
          _declarativeListenerInstalled = true;
        }

        return addInternal(productionToAdd);
      });
    }
    catch (Exception e)
    {
      LOGGER.error("DefaultProductionStorage.add threw Exception : ", e);
      return null;
    }
  }

  /**
   * called within the write lock
   * 
   * @param productionToAdd
   * @return
   */
  protected IProduction addInternal(IProduction productionToAdd)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(productionToAdd + " might not be unique");
    // in depth check
    IProduction existing = checkForExistingProduction(productionToAdd);
    if (existing == null)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug(productionToAdd + " is unique");
      index(productionToAdd);
      return productionToAdd;
    }
    else
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(productionToAdd + " matches " + existing);
      return existing;
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.jactr.core.module.procedural.storage.IProductionStorage#remove(org.
   * jactr.core.production.IProduction)
   */
  @Override
  public void remove(IProduction productionToRemove)
  {
    writeLocked(() -> {
      unindex(productionToRemove);
    });
  }

  /*
   * (non-Javadoc)
   * @see
   * org.jactr.core.module.procedural.storage.IProductionStorage#getProduction(
   * java.lang.String)
   */
  @Override
  public IProduction getProduction(String productionName)
  {
    try
    {
      return readLocked(() -> {
        return _allProductionsByName.get(productionName.toLowerCase());
      });
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("DefaultProductionStorage.getProduction threw Exception : ",
          e);
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.jactr.core.module.procedural.storage.IProductionStorage#getProductions(
   * java.util.Collection)
   */
  @Override
  public Collection<IProduction> getProductions(
      Collection<IProduction> container)
  {
    if (container == null) container = new ArrayList<>();
    final Collection<IProduction> fContainer = container;
    try
    {
      container = readLocked(() -> {
        fContainer.addAll(_allProductions);
        return fContainer;
      });
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("DefaultProductionStorage.getProductions threw Exception : ",
          e);
    }

    return container;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.jactr.core.module.procedural.storage.IProductionStorage#getProductions(
   * java.lang.String, org.jactr.core.chunktype.IChunkType, java.util.Set)
   */
  @Override
  public Set<IProduction> getProductions(String bufferName,
      IChunkType chunkType, Set<IProduction> container,
      Predicate<IProduction> selector)
  {
    if (container == null) container = Sets.mutable.empty();

    if (bufferName == null) bufferName = "null";

    final String fBufferName = bufferName;
    try
    {
      Set<IProduction> tmpContainer = readLocked(() -> {
        // specified
        Set<IProduction> fContainer = Sets.mutable.empty();
        fContainer.addAll(
            _productionMap.getOrDefault(fBufferName, Collections.emptyMap())
                .getOrDefault(chunkType, Collections.emptySet()));
        return fContainer;
      });

      if (selector != null) tmpContainer.removeIf((p) -> {
        return !selector.test(p);
      });

      container.addAll(tmpContainer);
      return container;

    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("DefaultProductionStorage.getProductions threw Exception : ",
          e);
    }
    return container;
  }

  private Set<IProduction> getPossibleProductions(String bufferName,
      IChunkType chunkType, Set<IProduction> container)
  {
    if (bufferName == null) bufferName = "null";

    container
        .addAll(_productionMap.getOrDefault(bufferName, Collections.emptyMap())
            .getOrDefault(chunkType, Collections.emptySet()));

    return container;
  }

  private Set<IProduction> getPossibleProductions(String bufferName,
      Set<IProduction> container)
  {
    return getPossibleProductions(bufferName, null, container);
  }

  private Set<IProduction> getAmbiguousProductions(Set<IProduction> container)
  {
    return getPossibleProductions(null, null, container);
  }

  /**
   * @param production
   * @return
   */
  protected IProduction checkForExistingProduction(IProduction production)
  {
    Map<String, Set<IChunkType>> map = new TreeMap<String, Set<IChunkType>>();
    Set<String> ambiguous = FastSetFactory.newInstance();

    extractIndexInfo(production, map, ambiguous);

    MutableSet<IProduction> tmp = Sets.mutable.empty();
    MutableSet<IProduction> candidates = Sets.mutable.empty();
    /*
     * we always include the fully ambiguous productions
     */
    getAmbiguousProductions(tmp);
    candidates.addAll(tmp);
    tmp.clear();

    /*
     * now ambiguous buffers
     */
    boolean intersect = candidates.size() != 0;

    for (String buffer : ambiguous)
    {
      tmp.clear();
      getPossibleProductions(buffer, tmp);
      if (intersect)
        candidates = candidates.intersect(tmp);
      else
      {
        candidates.addAll(tmp);
        intersect = tmp.size() != 0;
      }
    }

    /*
     * now the map
     */
    for (String bufferName : map.keySet())
    {
      tmp.clear();
      Set<IChunkType> chunkTypes = map.get(bufferName);
      for (IChunkType chunkType : chunkTypes)
      {
        getPossibleProductions(bufferName, chunkType, tmp);
        if (intersect)
          candidates = candidates.intersect(tmp);
        else
        {
          candidates.addAll(tmp);
          intersect = tmp.size() != 0;
        }
      }
      FastSetFactory.recycle(chunkTypes);
    }

    /*
     * the candidates should contain all the possible matching productions now
     * simply zip through and apply equals
     */
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String
        .format("Checking %d productions for equality", candidates.size()));

    for (IProduction candidate : candidates)
      if (candidate.equalsSymbolic(production)) return candidate;

    return null;
  }

  private void extractIndexInfo(IProduction production,
      Map<String, Set<IChunkType>> chunkTypeMapping,
      Set<String> ambiguousBuffers)
  {
    int minimumSize = Integer.MAX_VALUE;

    for (ICondition condition : production.getSymbolicProduction()
        .getConditions())
      if (condition instanceof IBufferCondition)
      {
        String bufferName = ((IBufferCondition) condition).getBufferName();

        if (condition instanceof ChunkTypeCondition)
        {
          IChunkType ct = ((ChunkTypeCondition) condition).getChunkType();

          if (ct != null)
          {
            Set<IChunkType> chunkTypes = chunkTypeMapping.get(bufferName);
            if (chunkTypes == null)
            {
              chunkTypes = FastSetFactory.newInstance();
              chunkTypeMapping.put(bufferName, chunkTypes);
            }

            chunkTypes.add(ct);
            chunkTypes.addAll(ct.getSymbolicChunkType().getChildren());

            /*
             * the smallest will also be the most specific condition (in terms
             * of chunktype only)
             */
            if (minimumSize > chunkTypes.size())
              minimumSize = chunkTypes.size();
          }
          else
            ambiguousBuffers.add(bufferName);

        }
        else
          // ambiguous, use a null chunktype
          ambiguousBuffers.add(bufferName);
      }
  }

  private String getSafeName(String name)
  {
    if (!_allProductionsByName.containsKey(name)) return name;

    int count = 0;
    try
    {
      int index = name.lastIndexOf("-");
      count = Integer.parseInt(name.substring(index + 1));
      name = name.substring(0, index);
    }
    catch (Exception e)
    {

    }

    String tmpName = name;
    while (_allProductionsByName.containsKey(tmpName))
      tmpName = name + "-" + count++;
    return tmpName;
  }

  /**
   * must be called within write lock
   * 
   * @param production
   */
  protected void index(IProduction production)
  {
    _allProductions.add(production);

    String name = getSafeName(production.getSymbolicProduction().getName());
    production.getSymbolicProduction().setName(name);

    _allProductionsByName.put(name.toLowerCase(), production);

    Map<String, Set<IChunkType>> map = new TreeMap<String, Set<IChunkType>>();
    Set<String> ambiguous = FastSetFactory.newInstance();

    extractIndexInfo(production, map, ambiguous);

    /*
     * a completely ambiguous production has no buffer conditions at all
     */
    if (ambiguous.size() == 0 && map.size() == 0)
      addToProductionMap(null, null, production);
    else
    {
      /*
       * if we are indexing everyone, do so.
       */
      for (String buffer : ambiguous)
        addToProductionMap(buffer, null, production);

      for (Map.Entry<String, Set<IChunkType>> entry : map.entrySet())
      {
        for (IChunkType chunkType : entry.getValue())
          addToProductionMap(entry.getKey(), chunkType, production);

        FastSetFactory.recycle(entry.getValue());
      }
    }

    FastSetFactory.recycle(ambiguous);
  }

  private void addToProductionMap(String bufferName, IChunkType chunkType,
      IProduction production)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String
        .format("Indexing %s by %s & %s", production, bufferName, chunkType));

    Map<IChunkType, Set<IProduction>> tree = _productionMap.get(bufferName);
    if (tree == null)
    {
      tree = new HashMap<IChunkType, Set<IProduction>>();
      _productionMap.put(bufferName, tree);
    }

    Set<IProduction> productions = tree.get(chunkType);
    if (productions == null)
    {
      productions = FastSetFactory.newInstance();
      tree.put(chunkType, productions);
    }

    productions.add(production);
  }

  private void removeFromProductionMap(String bufferName, IChunkType chunkType,
      IProduction production)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String
        .format("unIndexing %s by %s & %s", production, bufferName, chunkType));
    _productionMap.getOrDefault(bufferName, Collections.emptyMap())
        .getOrDefault(chunkType, Collections.emptySet()).remove(production);
  }

  /**
   * must be called within write lock
   * 
   * @param production
   */
  protected void unindex(IProduction production)
  {
    _allProductions.remove(production);
    _allProductionsByName
        .remove(production.getSymbolicProduction().getName().toLowerCase());

    Map<String, Set<IChunkType>> map = new TreeMap<String, Set<IChunkType>>();
    Set<String> ambiguous = FastSetFactory.newInstance();

    extractIndexInfo(production, map, ambiguous);

    /*
     * a completely ambiguous production has no buffer conditions at all
     */
    if (ambiguous.size() == 0 && map.size() == 0)
      removeFromProductionMap(null, null, production);
    else
    {
      /*
       * if we are indexing everyone, do so.
       */
      for (String buffer : ambiguous)
        removeFromProductionMap(buffer, null, production);

      for (Map.Entry<String, Set<IChunkType>> entry : map.entrySet())
      {
        for (IChunkType chunkType : entry.getValue())
          removeFromProductionMap(entry.getKey(), chunkType, production);

        FastSetFactory.recycle(entry.getValue());
      }
    }

    FastSetFactory.recycle(ambiguous);
  }

  private void writeLocked(Runnable runner)
  {
    try
    {
      _lock.writeLock().lock();
      runner.run();
    }
    finally
    {
      _lock.writeLock().unlock();
    }
  }

  private <T> T writeLocked(Callable<T> callable) throws Exception
  {
    try
    {
      _lock.writeLock().lock();
      return callable.call();
    }
    finally
    {
      _lock.writeLock().unlock();
    }
  }

  private <T> T readLocked(Callable<T> callable) throws Exception
  {
    try
    {
      _lock.readLock().lock();
      return callable.call();
    }
    finally
    {
      _lock.readLock().unlock();
    }
  }
}
