package org.jactr.eclipse.ui.concurrent;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.concurrent.JobExecutor;

public class UIJobExecutor extends JobExecutor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(UIJobExecutor.class);

  public UIJobExecutor(String name)
  {
    super(name);
  }
  
  @Override
  protected Job createJob(String name, Runnable command)
  {
    return new ExecutorJob(name, command);
  }
  
  private class ExecutorJob extends UIJob
  {
    
    private Runnable _runner;
    
    public ExecutorJob(String name, Runnable runner)
    {
      super(name);
      _runner = runner;
      setSystem(false);
      setPriority(Job.SHORT);
    }


    @Override
    public IStatus runInUIThread(IProgressMonitor monitor)
    {
      // TODO Auto-generated method stub
      try
      {
        _runner.run();
        return Status.OK_STATUS;
      }
      catch(Exception e)
      {
        return new Status(IStatus.ERROR,CorePlugin.PLUGIN_ID,getName()+" failed to execute runnable "+_runner, e);
      }
    }
  }

}
