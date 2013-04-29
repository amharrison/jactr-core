package org.jactr.eclipse.core.comp.internal;

/*
 * default logging
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jactr.eclipse.core.CorePlugin;

public class SaveToCache extends WorkspaceJob
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SaveToCache.class);

  private ProjectCompilationUnit     _compilationUnit;

  private IFile                      _cacheFile;

  public SaveToCache(ProjectCompilationUnit compUnit, IFile cache)
  {
    super("Caching " + compUnit.getResource().getName());
    _compilationUnit = compUnit;
    _cacheFile = cache;

    setUser(true);

    if (!_cacheFile.exists())
      setRule(ResourcesPlugin.getWorkspace().getRuleFactory().createRule(
          _cacheFile.getProject().getFolder(
              _cacheFile.getProjectRelativePath().segment(0))));
    else
      setRule(ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(
          _cacheFile));
  }

  @Override
  public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
  {
    /*
     * do we actually have to save it?
     */
    if (_cacheFile.exists()
        && _cacheFile.getLocalTimeStamp() > _compilationUnit
            .getModificationTime()) return Status.OK_STATUS;

    CompilationUnitFragment fragment = new CompilationUnitFragment(
        _compilationUnit.getSource(), _compilationUnit.getModelDescriptor(),
        _compilationUnit.getModificationTime());

    if (monitor.isCanceled()) return Status.CANCEL_STATUS;

    // has since been deleted..
    if (!_compilationUnit.getResource().exists()) return Status.CANCEL_STATUS;

    try
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(
          new GZIPOutputStream(baos));
      oos.writeObject(fragment);
      oos.close();

      if (_cacheFile.exists())
        _cacheFile.setContents(new ByteArrayInputStream(baos.toByteArray()),
            IResource.FORCE, monitor);
      else
      {
        IPath path = _cacheFile.getProjectRelativePath().removeLastSegments(1);
        IFolder root = _cacheFile.getProject().getFolder(path.segment(0));
        if (!root.exists()) root.create(true, true, monitor);

        for (String folder : path.removeFirstSegments(1).segments())
        {
          root = root.getFolder(folder);
          if (!root.exists()) root.create(true, true, monitor);
        }
        _cacheFile.create(new ByteArrayInputStream(baos.toByteArray()), true,
            monitor);
      }

      return Status.OK_STATUS;
    }
    catch (Exception e)
    {
      LOGGER.error("SaveToCache.runInWorkspace threw IOException : ", e);
      return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
          "Could not save to " + _cacheFile, e);
    }
  }
}
