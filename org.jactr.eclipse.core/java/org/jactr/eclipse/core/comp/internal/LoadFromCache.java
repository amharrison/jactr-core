package org.jactr.eclipse.core.comp.internal;

/*
 * default logging
 */
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jactr.eclipse.core.CorePlugin;

public class LoadFromCache extends WorkspaceJob
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(LoadFromCache.class);

  private ProjectCompilationUnit     _compilationUnit;

  private IFile                      _cacheFile;

  public LoadFromCache(ProjectCompilationUnit compUnit, IFile cache)
  {
    super("Loading " + compUnit.getResource().getName());
    setRule(cache);
    _compilationUnit = compUnit;
    _cacheFile = cache;

    setUser(true);
  }

  @Override
  public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
  {
    if (monitor.isCanceled()) return Status.CANCEL_STATUS;
    try
    {
      load();

      return Status.OK_STATUS;
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("LoadFromCache.runInWorkspace threw IOException : ", e);
      return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
          "Could not load from " + _cacheFile, e);
    }
  }

  public void load() throws Exception
  {
    ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(
        new BufferedInputStream(_cacheFile.getContents())));
    CompilationUnitFragment fragment = (CompilationUnitFragment) ois
        .readObject();

    _compilationUnit.setModelDescriptor(fragment.getModelDescriptor());
    _compilationUnit.setModificationTime(fragment.getModificationTime());

    ois.close();
  }

}
