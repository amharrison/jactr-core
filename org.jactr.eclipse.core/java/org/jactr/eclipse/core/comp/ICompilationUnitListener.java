package org.jactr.eclipse.core.comp;

/*
 * default logging
 */

@FunctionalInterface
public interface ICompilationUnitListener
{

  public void updated(ICompilationUnit compilationUnit);
}
