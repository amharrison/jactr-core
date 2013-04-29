package org.jactr.eclipse.core.comp;

/*
 * default logging
 */
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.builder.BuildJob;
import org.jactr.eclipse.core.comp.internal.AbstractCompilationUnit;
import org.jactr.eclipse.core.comp.internal.FileStoreCompilationUnit;
import org.jactr.eclipse.core.comp.internal.IMutableCompilationUnit;
import org.jactr.eclipse.core.comp.internal.LoadFromCache;
import org.jactr.eclipse.core.comp.internal.ProjectCompilationUnit;
import org.jactr.eclipse.core.comp.internal.SaveToCache;
import org.jactr.eclipse.core.comp.internal.URLCompilationUnit;
import org.jactr.eclipse.core.project.ACTRProjectUtils;
import org.jactr.io.parser.ModelParserFactory;

public class CompilationUnitManager
{
  /**
   * Logger definition
   */
  static private final transient Log          LOGGER      = LogFactory
                                                              .getLog(CompilationUnitManager.class);

  private static final String                 MODELS_ROOT = "models";

  private static final String                 CACHE_DIR   = ".compiled";

  static private final CompilationUnitManager _manager    = new CompilationUnitManager();

  static public CompilationUnitManager getManager()
  {
    return _manager;
  }

  static public boolean isJACTRModel(IResource resource)
  {
    if (resource.getFileExtension() == null) return false;
    for (String validExtension : ModelParserFactory.getValidExtensions())
      if (resource.getFileExtension().equalsIgnoreCase(validExtension))
        return true;
    return false;
  }

  static public boolean isOnModelPath(IResource resource)
  {
    IPath path = resource.getProjectRelativePath();
    if (path.segmentCount() < 1) return true;

    return path.segment(0).equals(MODELS_ROOT);
  }

  static public IProjectCompilationUnit acquire(IResource resource)
  {
    return _manager.acquire(resource, true);
  }

  static public IFileStoreCompilationUnit acquire(IFileStore resource)
  {
    return _manager.acquire(resource, true);
  }

  static public ICompilationUnit acquire(URL resource)
  {
    return _manager.acquire(resource, true);
  }

  static public void release(ICompilationUnit compilationUnit)
  {
    if (compilationUnit != null) _manager.releaseInternal(compilationUnit);
  }

  /**
   * keyed on URI
   */
  private Map<URI, Entry>                _activeCompilationUnits;

  private ConcurrentMap<URI, ReleaseJob> _toBeReleased = new ConcurrentHashMap<URI, ReleaseJob>();

  private CompilationUnitManager()
  {
    _activeCompilationUnits = new HashMap<URI, Entry>();
  }

  protected void scheduleBuildIfNecessary(ICompilationUnit compilationUnit)
  {
    if (!compilationUnit.isFresh())
    {
      BuildJob job = new BuildJob((IMutableCompilationUnit) compilationUnit,
          false);
      job.schedule();
    }
  }

  private ICompilationUnit acquireInternal(URI source)
  {
    ReleaseJob job = _toBeReleased.get(source);
    Entry entry = null;
    if (job != null && job.cancel()) entry = job.getEntry();
    if (entry == null) entry = _activeCompilationUnits.get(source);
    if (entry == null) return null;

    entry.acquire();
    return entry.get();
  }

  synchronized public IProjectCompilationUnit acquire(IResource resource,
      boolean compileIfNecessary)
  {
    if (!CompilationUnitManager.isJACTRModel(resource)) return null;

    URI uri = resource.getLocationURI();
    ICompilationUnit comp = acquireInternal(uri);
    if (comp == null)
    {
      comp = new ProjectCompilationUnit(resource);
      /*
       * attempt to load it..
       */
      compileIfNecessary = !load((ProjectCompilationUnit) comp);

      Entry entry = new Entry((AbstractCompilationUnit) comp);
      entry.acquire();
      _activeCompilationUnits.put(uri, entry);
    }

    if (comp != null && compileIfNecessary)
      _manager.scheduleBuildIfNecessary(comp);

    return (IProjectCompilationUnit) comp;
  }

  synchronized public IFileStoreCompilationUnit acquire(IFileStore resource,
      boolean compileIfNecessary)
  {
    URI uri = resource.toURI();
    ICompilationUnit comp = acquireInternal(uri);
    if (comp == null)
    {
      comp = new FileStoreCompilationUnit(resource);
      compileIfNecessary = true;
      _activeCompilationUnits.put(uri,
          new Entry((AbstractCompilationUnit) comp));
    }

    if (comp != null && compileIfNecessary)
      _manager.scheduleBuildIfNecessary(comp);

    return (IFileStoreCompilationUnit) comp;
  }

  synchronized public ICompilationUnit acquire(URL resource,
      boolean compileIfNecessary)
  {
    try
    {
      URI uri = resource.toURI();
      ICompilationUnit comp = acquireInternal(uri);
      if (comp == null)
      {
        comp = new URLCompilationUnit(resource);
        compileIfNecessary = true;
        _activeCompilationUnits.put(uri, new Entry(
            (AbstractCompilationUnit) comp));
      }

      if (comp != null && compileIfNecessary)
        _manager.scheduleBuildIfNecessary(comp);

      return comp;
    }
    catch (Exception e)
    {
      CorePlugin.error("Could not acquire url " + resource, e);
      return null;
    }
  }

  synchronized protected void releaseInternal(ICompilationUnit compilationUnit)
  {
    URI uri = compilationUnit.getSource();
    Entry entry = _activeCompilationUnits.get(uri);

    if (entry == null) return;

    if (entry.release())
    {
      _activeCompilationUnits.remove(uri);
      ReleaseJob job = new ReleaseJob(entry);
      _toBeReleased.putIfAbsent(uri, job);
      job.schedule(60000); // in a minute
      // /**
      // * we can cache it..
      // */
      // if (entry.get() instanceof ProjectCompilationUnit)
      // save((ProjectCompilationUnit) entry.get());
      // else
      // ((IMutableCompilationUnit) compilationUnit).dispose();
    }
  }

  /**
   * clear the cache for a project, or all if null
   * 
   * @param project
   */
  synchronized public void clearCache(IProject project)
  {
    FastList<IProject> projects = FastList.newInstance();
    if (project != null)
      projects.add(project);
    else
      for (IProject tmp : ACTRProjectUtils.getACTRProjects())
        projects.add(tmp);

    for (IProject tmp : projects)
    {
      final IResource compiled = tmp.findMember(CACHE_DIR);
      if (compiled != null && compiled.exists())
      {
        WorkspaceJob delete = new WorkspaceJob("Cleaning " + tmp.getName()
            + " cache") {

          @Override
          public IStatus runInWorkspace(IProgressMonitor monitor)
              throws CoreException
          {
            if (monitor.isCanceled()) return Status.CANCEL_STATUS;
            compiled.delete(true, monitor);
            return Status.OK_STATUS;
          }
        };

        delete.setRule(ResourcesPlugin.getWorkspace().getRuleFactory()
            .deleteRule(compiled));
        delete.schedule();
      }
    }
  }

  private void save(final ProjectCompilationUnit projectCompilationUnit)
  {
    IResource original = projectCompilationUnit.getResource();
    SaveToCache job = new SaveToCache(projectCompilationUnit,
        getCachedResource(original)) {

      @Override
      public IStatus runInWorkspace(IProgressMonitor monitor)
          throws CoreException
      {
        IStatus rtn = super.runInWorkspace(monitor);
        projectCompilationUnit.dispose();
        return rtn;
      }
    };

    job.schedule();
  }

  /**
   * @param projectCompilationUnit
   * @return true if loadde
   */
  private boolean load(ProjectCompilationUnit projectCompilationUnit)
  {
    IResource original = projectCompilationUnit.getResource();
    IFile cached = getCachedResource(original);
    if (!cached.exists()) return false;

    LoadFromCache job = new LoadFromCache(projectCompilationUnit, cached);
    // using this as a job is causing some deadlock situations
    // job.schedule();

    try
    {
      job.load();

      // job.join();
      return true;
    }
    catch (Exception e)
    {
      CorePlugin.error("Could not wait for load", e);
      return false;
    }
  }

  static private IFile getCachedResource(IResource resource)
  {
    String location = resource.getProjectRelativePath().toString();
    location = CACHE_DIR + "/" + location;
    location = location + ".cached";
    IFile cacheFile = resource.getProject().getFile(location);
    return cacheFile;
  }

  private class Entry
  {
    private AtomicInteger           _acquisitions = new AtomicInteger();

    private AbstractCompilationUnit _compilationUnit;

    public Entry(AbstractCompilationUnit unit)
    {
      _compilationUnit = unit;
    }

    public void acquire()
    {
      _acquisitions.incrementAndGet();
    }

    public boolean release()
    {
      return _acquisitions.decrementAndGet() <= 0;
    }

    public AbstractCompilationUnit get()
    {
      return _compilationUnit;
    }

    public void dispose()
    {

    }
  }

  private class ReleaseJob extends Job
  {
    private volatile Entry _entry;

    public ReleaseJob(Entry entry)
    {
      super("Delayed release" + entry._compilationUnit.getSource().getPath());

      setSystem(true);
      // setUser(true);

      setPriority(Job.SHORT);
      _entry = entry;
    }

    public Entry getEntry()
    {
      return _entry;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
      if (monitor.isCanceled()) return Status.CANCEL_STATUS;

      Entry entry = _entry;
      _entry = null;
      ICompilationUnit compilationUnit = entry._compilationUnit;
      URI uri = compilationUnit.getSource();
      if (uri != null) _toBeReleased.remove(uri);

      /**
       * we can cache it..
       */
      if (entry.get() instanceof ProjectCompilationUnit)
        save((ProjectCompilationUnit) entry.get());
      else
        ((IMutableCompilationUnit) compilationUnit).dispose();

      return Status.OK_STATUS;
    }

  }
}
