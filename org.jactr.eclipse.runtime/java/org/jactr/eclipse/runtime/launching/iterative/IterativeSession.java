/*
 * Created on Apr 12, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jactr.eclipse.runtime.launching.iterative;

import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.netty.service.ServerService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Display;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.launching.session.AbstractSession;
import org.jactr.eclipse.runtime.launching.session.SessionTracker;
import org.jactr.eclipse.runtime.preferences.RuntimePreferences;
import org.jactr.tools.async.credentials.ICredentials;
import org.jactr.tools.async.iterative.tracker.IterativeRunTracker;
import org.jactr.tools.deadlock.IDeadLockListener;

public class IterativeSession extends AbstractSession
{

  private final class IterativeTrackerJob extends Job
  {

    final private int TOTAL_WORK = 1000;

    private IterativeTrackerJob(String name)
    {
      super(name);
    }

    protected int waitForConnection(long timeToWait, IProgressMonitor monitor)
    {
      long start = System.currentTimeMillis();
      long delta = 0;
      while (_tracker.getTotalIterations() == 0 && delta < timeToWait
          && !monitor.isCanceled())
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(delta + " is less than " + timeToWait
              + " and no total iterations, snoozing");
        try
        {
          Thread.sleep(500);
        }
        catch (Exception e)
        {

        }
        delta = System.currentTimeMillis() - start;
      }
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Got " + _tracker.getTotalIterations() + " iterations");
      return _tracker.getTotalIterations();
    }

    protected String generateLabel()
    {
      StringBuilder sb = new StringBuilder("Running ");
      sb.append(_tracker.getCurrentIteration()).append("/");
      sb.append(_tracker.getTotalIterations()).append(" ETA : ");
      DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT,
          DateFormat.SHORT);
      sb.append(format.format(new Date(_tracker.getETA()))).append(" (");

      /*
       * how much time is remaining
       */

      long remaining = _eta = _tracker.getTimeToCompletion();

      remaining /= 1000;

      long[] scalors = { 86400, 3600, 60, 1 };
      char[] tags = { 'd', 'h', 'm', 's' };
      int len = scalors.length;

      for (int i = 0; i < len; i++)
      {
        long unit = remaining / scalors[i];
        if (unit > 0)
        {
          sb.append(unit).append(tags[i]).append(' ');
          remaining -= unit * scalors[i];
          // cut the seconds if anything else is displayed
          if (tags[i] != 's') len = scalors.length - 1;
        }
      }

      sb.append("remain)");

      Collection<Integer> exceptions = _tracker.getExceptionCycles();
      if (exceptions.size() != 0)
      {
        _cleanRun = false;
        sb.append(" Errors on iterations:");
        sb.append(exceptions);
      }

      int queued = getIterativeSessionTracker().getNumberOfDeferredLaunches();
      if (queued != 0) sb.append(" (").append(queued).append(" runs queued)");

      return sb.toString();
    }

    protected void processIterations(IProgressMonitor monitor)
    {
      int total = _tracker.getTotalIterations();

      monitor = new SubProgressMonitor(monitor, TOTAL_WORK - 2);
      monitor.beginTask(generateLabel(), TOTAL_WORK - 2);
      try
      {
        double lastProgress = 0;
        int currentIteration = 0;
        ISessionInfo session = _tracker.getActiveSession();

        while ((currentIteration = _tracker.getCurrentIteration()) <= total
            && !monitor.isCanceled() && session.isConnected())
        {
          double currentProgress = (double) (currentIteration - 1)
              / (double) total;

          _percent = (float) currentProgress;

          boolean shouldSleep = true;

          if (currentProgress > lastProgress)
          {
            int increment = (int) ((currentProgress - lastProgress) * (TOTAL_WORK - 2));

            if (increment >= 1)
            {
              monitor.worked(increment);
              /*
               * ratchet up the lastProgress by the same increment so that we
               * don't drift when iterations > TOTAL_WORK
               */
              lastProgress += (double) increment / (double) (TOTAL_WORK - 2);
              shouldSleep = false;
            }

            // but always update the display
            String label = generateLabel();
            monitor.setTaskName(label);
          }

          if (shouldSleep) try
          {
            Thread.sleep(1000);
          }
          catch (InterruptedException e)
          {
          }
        }
      }
      finally
      {
        monitor.done();
      }
    }

    @Override
    protected IStatus run(IProgressMonitor monitor)
    {
      try
      {
        monitor.beginTask("Waiting for connection", TOTAL_WORK);
        /*
         * wait for the connection..
         */
        long waitTime = 1000 * RuntimePlugin.getDefault()
            .getPluginPreferences()
            .getInt(RuntimePreferences.ITERATIVE_START_WAIT_PREF);
        int totalIterations = waitForConnection(waitTime, monitor);
        monitor.setTaskName("Will run " + totalIterations + " iterations");
        monitor.worked(1);

        if (totalIterations == 0)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("Could not establish connection");
          monitor.setTaskName("Could not establish connection");
        }
        else
          /*
           * we've got something to do..
           */
          processIterations(monitor);

        monitor.setTaskName("Cleaning up");

        if (monitor.isCanceled() || totalIterations == 0)
          if (!_launch.isTerminated()) try
          {
            _launch.terminate();
          }
          catch (DebugException de)
          {
            CorePlugin.error("Could not terminate iterative run ", de);
          }

        monitor.worked(1);

        if (monitor.isCanceled()) return Status.CANCEL_STATUS;

        /*
         * beep to notify
         */
        if (RuntimePlugin.getDefault().getPluginPreferences()
            .getBoolean(RuntimePreferences.ITERATIVE_BEEP_PREF))
        {
          final Display display = Display.getDefault();

          Runnable beep = new Runnable() {
            public void run()
            {
              display.beep();
            }
          };

          display.asyncExec(beep);
        }
        return Status.OK_STATUS;
      }
      finally
      {
        monitor.done();
      }
    }
  }

  static public final String                      LAUNCH_TYPE     = "org.jactr.eclipse.runtime.launching.iterative";

  static private SessionTracker<IterativeSession> _sessionTracker = new SessionTracker<IterativeSession>();

  static public SessionTracker<IterativeSession> getIterativeSessionTracker()
  {
    return _sessionTracker;
  }

  /**
   * Logger definition
   */
  static private final transient Log LOGGER    = LogFactory
                                                   .getLog(IterativeSession.class);

  private final IterativeRunTracker  _tracker;

  private long                       _eta;

  private float                      _percent;

  private boolean                    _cleanRun = true;

  public IterativeSession(ILaunch launch, ILaunchConfiguration configuration)
  {
    super(launch, configuration);

    _tracker = new IterativeRunTracker();
    _tracker.setService(new ServerService());
    _tracker.setCredentialInformation("none:nopass");
    _tracker.setAddressInfo("localhost:0");

    _tracker.setDeadLockListener(new IDeadLockListener() {
      public void deadlockDetected()
      {
        CorePlugin.error("Deadlock detected, terminating session");
        try
        {
          stop();
        }
        catch (CoreException ce)
        {
          CorePlugin.error("Failed to terminate session ", ce);
        }
      }
    });
  }

  public boolean wasCleanRun()
  {
    return _cleanRun;
  }

  public float getPercentCompleted()
  {
    return _percent;
  }

  public long getETA()
  {
    return _eta;
  }

  @Override
  protected Job createSessionJob()
  {
    Job job = new IterativeTrackerJob(getConfiguration().getName());
    job.setPriority(Job.LONG);
    return job;
  }

  public IterativeRunTracker getIterativeRunTracker()
  {
    return _tracker;
  }

  @Override
  protected void connect() throws CoreException
  {
    try
    {
      _tracker.start();
    }
    catch (Exception e)
    {
      LOGGER.error("Could not start iterative tracker ", e);
      throw new CoreException(new Status(IStatus.ERROR,
          RuntimePlugin.PLUGIN_ID, "Could not start iterative tracker", e));
    }
  }

  @Override
  protected void disconnect(boolean force) throws CoreException
  {
    try
    {
      _tracker.stop();
      ISessionInfo session = _tracker.getActiveSession();
      if (session != null) session.waitForDisconnect();
    }
    catch (Exception e)
    {
      LOGGER.error("Could not stop iterative tracker ", e);
      throw new CoreException(new Status(IStatus.ERROR,
          RuntimePlugin.PLUGIN_ID, "Could not stop iterative tracker", e));
    }
  }

  @Override
  public InetSocketAddress getConnectionAddress()
  {
    return (InetSocketAddress) _tracker.getActualAddress();
  }

  @Override
  public ICredentials getCredentials()
  {
    return _tracker.getActualCredentials();
  }

  @Override
  protected SessionTracker getSessionTracker()
  {
    return _sessionTracker;
  }

}
