package org.jactr.eclipse.core.comp;

/*
 * default logging
 */
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.tree.CommonTree;

public interface ICompilationUnit
{

  public void addListener(ICompilationUnitListener listener);

  public void removeListener(ICompilationUnitListener listener);
  
  /**
   * @return
   */
  public URI getSource();

  /**
   * @return
   */
  public boolean isFresh();

  /**
   * @return
   */
  public CommonTree getModelDescriptor();
  
  /**
   * @return
   */
  public boolean isParseClean();

  public boolean isCompileClean();
  
  public Map<String, CommonTree> getNamedContents(int nodeType);
  
  public Collection<CommonTree> getContents(int nodeType);

  public Set<URI> getImportSources();
}
