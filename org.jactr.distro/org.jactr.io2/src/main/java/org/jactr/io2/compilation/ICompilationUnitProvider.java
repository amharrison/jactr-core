package org.jactr.io2.compilation;

import java.net.URI;

public interface ICompilationUnitProvider
{

  public boolean handles(URI modelToLoad);
  
  public ICompilationUnit get(URI modelToLoad) throws Exception;
}
