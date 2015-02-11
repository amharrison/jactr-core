package org.jactr.eclipse.core.concurrent;

/*
 * default logging
 */
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jactr.eclipse.core.CorePlugin;

public class JobExecutor implements Executor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(JobExecutor.class);

  private String _name;
  
  public JobExecutor(String name)
  {
    _name = name;
  }
  
  public void execute(Runnable command)
  {
    Job job = createJob(_name, command);
    job.schedule();
  }
  
  public void executeLater(Runnable command, long milliseconds)
  {
    Job job = createJob(_name, command);
    job.schedule(milliseconds);
  }
  
  
  protected Job createJob(String name, Runnable command)
  {
    return new ExecutorJob(_name, command);
  }
  
  private class ExecutorJob extends Job
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
    protected IStatus run(IProgressMonitor monitor)
    {
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
