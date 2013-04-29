package org.jactr.eclipse.ui.reconciler;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.jactr.eclipse.core.comp.ICompilationUnitRunnable;

public class CompilationUnitJob extends Job
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER     = LogFactory
                                                    .getLog(CompilationUnitJob.class);

  private ICompilationUnitRunnable   _runnable;

  private volatile boolean           _scheduled = false;

  private volatile boolean           _running   = false;

  public CompilationUnitJob(String name, ICompilationUnitRunnable runnable)
  {
    super(name);
    _runnable = runnable;
    setSystem(true);

    /*
     * ICompilationUnit compUnit = _runnable.getCompilationUnit(); // use the
     * project resource to allow correct chaining of rules when local if
     * (compUnit instanceof IProjectCompilationUnit)
     * setRule(((IProjectCompilationUnit) compUnit).getResource()); else
     * setRule(new
     * CompilationUnitSchedulingRule(_runnable.getCompilationUnit()));
     */
    addJobChangeListener(new IJobChangeListener() {

      public void aboutToRun(IJobChangeEvent event)
      {
        _running = true;
      }

      public void awake(IJobChangeEvent event)
      {
        // TODO Auto-generated method stub

      }

      public void done(IJobChangeEvent event)
      {
        _running = false;
        _scheduled = false;
      }

      public void running(IJobChangeEvent event)
      {
        // TODO Auto-generated method stub

      }

      public void scheduled(IJobChangeEvent event)
      {
        _scheduled = true;

      }

      public void sleeping(IJobChangeEvent event)
      {
        // TODO Auto-generated method stub

      }

    });
  }

  public boolean isScheduled()
  {
    return _scheduled;
  }

  public boolean isRunning()
  {
    return _running;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor)
  {
    IStatus status = _runnable.run(monitor);

    if (status.getSeverity() == IStatus.CANCEL) return Status.CANCEL_STATUS;

    return Status.OK_STATUS;
  }

}
