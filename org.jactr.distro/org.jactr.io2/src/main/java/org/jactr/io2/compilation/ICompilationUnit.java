package org.jactr.io2.compilation;

import java.net.URI;

import org.jactr.core.model.IModel;

public interface ICompilationUnit
{

  public URI getURI();

  public Object getAST();

  public IModel build() throws Exception;

}
