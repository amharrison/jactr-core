package org.jactr.io2.compilation;

import java.net.URI;

import org.jactr.core.model.IModel;

public interface ICompilationUnit
{

  public URI getURI();

  public Object getAST();

  /**
   * return the AST with all its imports normalized
   * 
   * @return
   */
  public Object getNormalizedAST();

  public IModel build() throws Exception;

}
