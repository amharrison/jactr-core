package org.jactr.eclipse.core.comp.internal;

/*
 * default logging
 */
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.jactr.eclipse.core.comp.IFileStoreCompilationUnit;

public class FileStoreCompilationUnit extends AbstractCompilationUnit implements
    IFileStoreCompilationUnit
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(FileStoreCompilationUnit.class);
  
  final private IFileStore           _fileStore;

  public FileStoreCompilationUnit(IFileStore fileStore)
  {
    _fileStore = fileStore;
  }

  @Override
  public long getSourceModificationTime()
  {
    IFileInfo info = _fileStore.fetchInfo();
    return info.getLastModified();
  }

  public IFileStore getFileStore()
  {
    return _fileStore;
  }

  public URI getSource()
  {
    return _fileStore.toURI();
  }
}
