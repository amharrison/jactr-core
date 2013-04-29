package org.jactr.eclipse.ui.commands;

/*
 * default logging
 */
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.jactr.eclipse.ui.UIPlugin;

public class ArchiveAndDeleteJob extends WorkspaceJob
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ArchiveAndDeleteJob.class);
  
  static private final int           WORK_UNITS = 1000;

  final private boolean              _deleteWhenDone;

  final private IFolder              _root;

  final private IFile                _archive;

  public ArchiveAndDeleteJob(IFolder folder, boolean delete)
  {
    super("Archiving " + folder.getName());
    _root = folder;
    _deleteWhenDone = delete;

    _archive = _root.getParent().getFile(new Path(_root.getName() + ".zip"));

    ISchedulingRule refreshRule = ResourcesPlugin.getWorkspace()
        .getRuleFactory().refreshRule(_root.getParent());

    ISchedulingRule archiveRule = ResourcesPlugin.getWorkspace()
        .getRuleFactory().createRule(_archive);
    ISchedulingRule rule = null;

    if (_deleteWhenDone)
      rule = MultiRule.combine(new ISchedulingRule[] { archiveRule,
          ResourcesPlugin.getWorkspace().getRuleFactory().deleteRule(_root),
          refreshRule });
    else
      rule = MultiRule.combine(archiveRule, refreshRule);

    setRule(rule);
  }

  @Override
  public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
  {
    monitor.beginTask("Archiving " + _root.getName(), WORK_UNITS + 1);
    
    Collection<IResource> resources = getResources(_root, monitor);
    IStatus returnStatus = Status.OK_STATUS;
    int lastTotal = 0;
    int currentTotal = 0;
    try
    {
      IPath strip = _root.getFullPath().removeLastSegments(1);

      ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
          new FileOutputStream(_archive.getLocation().toFile())));

      byte[] block = new byte[4096];
      float count = 0;
      for (IResource resource : resources)
      {
        IPath path = resource.getFullPath();

        // strip prefix
        if (strip.isPrefixOf(path))
          path = path.removeFirstSegments(strip.segmentCount());

        String name = path.toString();

        if (name.startsWith("/")) name = name.substring(1, name.length());
        if (resource instanceof IContainer) name += "/"; // make sure folders

        monitor.setTaskName("Archiving " + name);

        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);

        if (resource instanceof IFile)
        {
          IFile file = (IFile) resource;
          InputStream is = file.getContents(true);
          int read = 0;
          int written = 0;
          while ((read = is.read(block)) > 0)
          {
            zos.write(block, 0, read);
            written += read;
          }
          is.close();
          if (LOGGER.isDebugEnabled()) LOGGER.debug(written + " bytes ");
        }

        zos.closeEntry();
        
        count++;
        currentTotal = (int) (WORK_UNITS * count / resources.size());
        if (currentTotal > lastTotal)
        {
          monitor.worked(currentTotal - lastTotal);
          lastTotal = currentTotal;
        }
      }

      zos.close();

      return returnStatus;
    }
    catch (IOException ioe)
    {
      if (_archive.exists()) _archive.delete(true, monitor);

      return new Status(IStatus.ERROR, UIPlugin.ID, "Failed to archive "
          + _root.getName(), ioe);
    }
    finally
    {
      if (returnStatus.isOK() && _deleteWhenDone) delete(monitor);

      _root.getParent().refreshLocal(IResource.DEPTH_INFINITE, monitor);

      monitor.done();
    }
  }

  private Collection<IResource> getResources(IFolder root,
      IProgressMonitor monitor) throws CoreException
  {
    ArrayList<IResource> resources = new ArrayList<IResource>();
    monitor = new SubProgressMonitor(monitor, 1);
    monitor.beginTask("Scanning for children of " + root.getName(), 1);
    try
    {
      getResources(root, resources);
      return resources;
    }
    finally
    {
      monitor.done();
    }
  }

  private void getResources(IContainer root, Collection<IResource> resources)
      throws CoreException
  {
    resources.add(root);
    for (IResource resource : root.members(false))
      if (resource.isAccessible()) if (resource instanceof IContainer)
        getResources((IContainer) resource, resources);
      else
        resources.add(resource);
  }

  private void delete(IProgressMonitor monitor) throws CoreException
  {
    _root.delete(true, monitor);
  }
}
