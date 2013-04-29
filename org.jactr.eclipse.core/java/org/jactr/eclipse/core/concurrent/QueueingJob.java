package org.jactr.eclipse.core.concurrent;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;

public abstract class QueueingJob extends Job implements IJobChangeListener
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(QueueingJob.class);

  private boolean                    _isQueued;

  private boolean                    _isRunning;

  public QueueingJob(String name)
  {
    super(name);
    addJobChangeListener(this);
  }

  synchronized public boolean isQueued()
  {
    return _isQueued;
  }

  synchronized public boolean isRunning()
  {
    return _isRunning;
  }

  synchronized public boolean queue(long millis)
  {
    if (!_isQueued)
    {
      _isQueued = true;
      schedule(millis);
    }
    return _isQueued;
  }

  @Override
  synchronized protected void canceling()
  {
    super.canceling();
    _isQueued = false;
    _isRunning = false;
  }

  public void aboutToRun(IJobChangeEvent event)
  {
    // TODO Auto-generated method stub

  }

  public void awake(IJobChangeEvent event)
  {
    // TODO Auto-generated method stub

  }

  synchronized public void done(IJobChangeEvent event)
  {
    _isQueued = false;
    _isRunning = false;
  }

  synchronized public void running(IJobChangeEvent event)
  {
    _isRunning = true;

  }

  synchronized public void scheduled(IJobChangeEvent event)
  {
    _isQueued = true;
  }

  public void sleeping(IJobChangeEvent event)
  {
    // TODO Auto-generated method stub

  }

}
