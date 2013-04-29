package org.jactr.eclipse.core.comp;

/*
 * default logging
 */
import org.eclipse.core.filesystem.IFileStore;

public interface IFileStoreCompilationUnit extends ICompilationUnit
{

  
  public IFileStore getFileStore();
}
