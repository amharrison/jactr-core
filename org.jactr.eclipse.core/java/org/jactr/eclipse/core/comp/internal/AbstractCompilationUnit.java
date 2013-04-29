package org.jactr.eclipse.core.comp.internal;

/*
 * default logging
 */
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javolution.util.FastList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.core.comp.ICompilationUnitListener;
import org.jactr.io.antlr3.misc.ASTSupport;

public abstract class AbstractCompilationUnit implements
    IMutableCompilationUnit
{
  /**
   * Logger definition
   */
  static private final transient Log               LOGGER            = LogFactory
                                                                         .getLog(AbstractCompilationUnit.class);

  private final ExceptionContainer                 _parseContainer;

  private final ExceptionContainer                 _compileContainer;

  private final FastList<ICompilationUnitListener> _listeners;

  private CommonTree                               _modelDescriptor;

  private long                                     _modificationTime = -1;

  private Map<Integer, Map<String, CommonTree>>    _cachedNamedTypes;

  private Set<URI>                                 _importSources;

  public AbstractCompilationUnit()
  {
    _listeners = FastList.newInstance();
    _parseContainer = new ExceptionContainer();
    _compileContainer = new ExceptionContainer();
    _cachedNamedTypes = new TreeMap<Integer, Map<String, CommonTree>>();
    _importSources = new HashSet<URI>();
  }

  public void addImportSource(URI uri)
  {
    _importSources.add(uri);
  }

  public Set<URI> getImportSources()
  {
    return Collections.unmodifiableSet(_importSources);
  }

  public void dispose()
  {
    FastList.recycle(_listeners);
    _parseContainer.dispose();
    _compileContainer.dispose();
    _modelDescriptor = null;
  }

  public ExceptionContainer getCompileContainer()
  {
    return _compileContainer;
  }

  public ExceptionContainer getParseContainer()
  {
    return _parseContainer;
  }

  public boolean isParseClean()
  {
    return _modelDescriptor != null && _parseContainer.isClean();
  }

  public boolean isCompileClean()
  {
    return isParseClean() && _compileContainer.isClean();
  }

  synchronized public void setModelDescriptor(CommonTree modelDescriptor)
  {
    _cachedNamedTypes.clear();
    _modelDescriptor = modelDescriptor;
    setModificationTime(System.currentTimeMillis());

    for (ICompilationUnitListener listener : _listeners)
      listener.updated(this);
  }

  synchronized public Map<String, CommonTree> getNamedContents(int nodeType)
  {
    if (_modelDescriptor == null) return Collections.emptyMap();

    Map<String, CommonTree> nodes = _cachedNamedTypes.get(nodeType);
    if (nodes == null)
    {
      nodes = Collections.unmodifiableMap(ASTSupport.getMapOfTrees(
          _modelDescriptor, nodeType));
      _cachedNamedTypes.put(nodeType, nodes);
    }

    return nodes;
  }
  
  synchronized public Collection<CommonTree> getContents(int nodeType)
  {
    if (_modelDescriptor == null) return Collections.emptyList();
    return ASTSupport.getAllDescendantsWithType(_modelDescriptor, nodeType);
  }

  public void setModificationTime(long time)
  {
    _modificationTime = time;
  }

  public void addListener(ICompilationUnitListener listener)
  {
    _listeners.add(listener);
  }

  public CommonTree getModelDescriptor()
  {
    return _modelDescriptor;
  }

  /**
   * modification time of the model descriptor
   * 
   * @return
   */
  public long getModificationTime()
  {
    return _modificationTime;
  }

  /**
   * modification time of the source
   * 
   * @return
   */
  abstract public long getSourceModificationTime();

  public boolean isFresh()
  {
    return getModificationTime() >= getSourceModificationTime();
  }

  public void removeListener(ICompilationUnitListener listener)
  {
    _listeners.remove(listener);
  }

}
