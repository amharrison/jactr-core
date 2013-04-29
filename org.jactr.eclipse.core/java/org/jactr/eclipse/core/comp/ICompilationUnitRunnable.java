package org.jactr.eclipse.core.comp;

/*
 * default logging
 */
import org.jactr.eclipse.core.concurrent.IRunnableWithProgress;

public interface ICompilationUnitRunnable extends IRunnableWithProgress
{

  public ICompilationUnit getCompilationUnit();
}
