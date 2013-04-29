package org.jactr.eclipse.ui.concurrent;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.ui.progress.UIJob;

public abstract class QueueingUIJob extends UIJob implements IJobChangeListener
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER    = LogFactory
                                                   .getLog(QueueingUIJob.class);

  private boolean                    _isQueued = false;

  public QueueingUIJob(String name)
  {
    super(name);
    addJobChangeListener(this);
  }

  public boolean queue(long millis)
  {
    if (!_isQueued) super.schedule(millis);
    return _isQueued;
  }

  public void aboutToRun(IJobChangeEvent event)
  {
    // TODO Auto-generated method stub

  }

  public void awake(IJobChangeEvent event)
  {
    // TODO Auto-generated method stub

  }

  public void done(IJobChangeEvent event)
  {
    _isQueued = false;
  }

  public void running(IJobChangeEvent event)
  {
    // TODO Auto-generated method stub

  }

  public void scheduled(IJobChangeEvent event)
  {
    _isQueued = true;

  }

  public void sleeping(IJobChangeEvent event)
  {
    // TODO Auto-generated method stub

  }

}
