package org.jactr.eclipse.core.comp;

/*
 * default logging
 */
import org.eclipse.core.resources.IResource;


public interface IProjectCompilationUnit extends ICompilationUnit
{

  public IResource getResource();
}
