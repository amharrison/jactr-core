package org.jactr.eclipse.core.comp.internal;

/*
 * default logging
 */
import java.net.URI;

import org.antlr.runtime.tree.CommonTree;
import org.jactr.eclipse.core.comp.ICompilationUnit;

public interface IMutableCompilationUnit extends ICompilationUnit
{

  public ExceptionContainer getParseContainer();

  public ExceptionContainer getCompileContainer();

  public void setModelDescriptor(CommonTree modelDescriptor);
  
  public void setModificationTime(long time);
  
  public void dispose();

  public void addImportSource(URI source);
}
