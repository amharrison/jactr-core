/*
 * Created on Jun 14, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.runtime.launching.session;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.UUID;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.jactr.eclipse.runtime.RuntimePlugin;
import org.jactr.eclipse.runtime.debug.ACTRDebugTarget;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.manager.internal.SessionManager;
import org.jactr.tools.async.credentials.ICredentials;

public abstract class AbstractSession
{

  protected ILaunch              _launch;

  protected ILaunchConfiguration _configuration;

  protected Job                  _sessionJob;

  private IPath                  _relativeWorkingDirectory;

  private IPath                  _absoluteWorkingDirectory;

  protected volatile boolean     _isRunning  = false;

  protected boolean              _wasStopped = false;

  protected boolean              _destroyed  = false;

  private Date                   _executionStart;

  private IJobChangeListener     _listener;

  private final UUID             _id;

  private ISession               _newSession;

  public AbstractSession(ILaunch launch, ILaunchConfiguration configuration)
  {
    _id = UUID.randomUUID();

    _launch = launch;
    _configuration = configuration;

    _listener = new IJobChangeListener() {

      public void aboutToRun(IJobChangeEvent event)
      {
        _isRunning = true;
        // RuntimePlugin.info("Starting " + event.getJob().getName());
      }

      public void awake(IJobChangeEvent event)
      {
        // TODO Auto-generated method stub

      }

      @SuppressWarnings("unchecked")
      public void done(IJobChangeEvent event)
      {
        _isRunning = false;

        boolean wasNormal = event.getResult().getSeverity() == IStatus.OK;

        // RuntimePlugin.info("Completed " + event.getJob().getName() +
        // " normal:"
        // + wasNormal + " terminated:" + _launch.isTerminated());

        try
        {
          /*
           * if something went wrong, force the termination
           */
          if (!wasNormal && !_launch.isTerminated())
          {
            for (IDebugTarget target : _launch.getDebugTargets())
              try
              {
                if (target.isSuspended() && target.canResume())
                  target.resume();
                if (target.canTerminate() && !target.isTerminated())
                  target.terminate();
              }
              catch (Exception e)
              {
                RuntimePlugin.error("Could not resume " + target.getName(), e);
              }
            _launch.terminate();
          }
        }
        catch (DebugException e1)
        {
          RuntimePlugin.getDefault().getLog().log(e1.getStatus());
        }

        /**
         * otherwise disconnect cleanly. this should wait until the otherside
         * has disconnected.
         */
        try
        {
          disconnect(!wasNormal);
          // RuntimePlugin.info("Disconnected wasNormal: " + wasNormal);
        }
        catch (Exception e)
        {
          RuntimePlugin.error(e.getMessage(), e);
        }

        if (wasNormal)
          getSessionTracker().remove(AbstractSession.this, wasNormal);
        else
          getSessionTracker().cancel(AbstractSession.this);

        try
        {
          IProject project = ACTRLaunchConfigurationUtils
              .getProject(_configuration);
          if (_relativeWorkingDirectory != null && project != null)
          {
            IFolder folder = project.getFolder(_relativeWorkingDirectory);
            folder.refreshLocal(IResource.DEPTH_INFINITE, null);
          }
        }
        catch (CoreException ce)
        {

        }

        _sessionJob = null;
      }

      public void running(IJobChangeEvent event)
      {
      }

      @SuppressWarnings("unchecked")
      public void scheduled(IJobChangeEvent event)
      {
        getSessionTracker().add(AbstractSession.this);
        ((SessionManager) RuntimePlugin.getDefault().getSessionManager())
            .addSession(AbstractSession.this);
      }

      public void sleeping(IJobChangeEvent event)
      {

      }

    };
  }

  /**
   * short term hack to bridge the two systems
   * 
   * @return
   */
  public ISession getSession()
  {
    return _newSession;
  }

  /**
   * short term hack to bridge the two systems
   * 
   * @param session
   */
  public void setSession(ISession session)
  {
    _newSession = session;
  }

  public UUID getId()
  {
    return _id;
  }

  public ILaunch getLaunch()
  {
    return _launch;
  }

  public ILaunchConfiguration getConfiguration()
  {
    return _configuration;
  }

  public void setRelativeWorkingDirectory(IPath path)
  {
    _relativeWorkingDirectory = path;
  }

  /**
   * project relative working directory
   * 
   * @return
   */
  public IPath getRelativeWorkingDirectory()
  {
    return _relativeWorkingDirectory;
  }

  public void setAbsoluteWorkingDirectory(IPath path)
  {
    _absoluteWorkingDirectory = path;
  }

  /**
   * project relative working directory
   * 
   * @return
   */
  public IPath getAbsoluteWorkingDirectory()
  {
    return _absoluteWorkingDirectory;
  }

  /**
   * destroy the session and signal. This is called by the client, typically
   * when all inspections have been done. However, it is not guaranteed to be
   * called at all.
   */
  public void destroy()
  {
    if (isDestroyed()) return;

    if (isActive())
      throw new IllegalStateException("Cannot destroy active sessions");

    _destroyed = true;

    if (_configuration != null) try
    {
      _configuration.delete();
    }
    catch (Exception e)
    {

    }
    finally
    {
      _configuration = null;
    }

    getSessionTracker().destroy(this);
  }

  public boolean isDestroyed()
  {
    return _destroyed;
  }

  abstract protected SessionTracker getSessionTracker();

  abstract public InetSocketAddress getConnectionAddress();

  abstract public ICredentials getCredentials();

  abstract protected Job createSessionJob();

  abstract protected void connect() throws CoreException;

  /**
   * disconnect will be called after the job completes
   * 
   * @param force
   * @throws CoreException
   */
  abstract protected void disconnect(boolean force) throws CoreException;

  public Date getExecutionStartTime()
  {
    return _executionStart;
  }

  public void start() throws CoreException
  {

    _executionStart = new Date();
    _sessionJob = createSessionJob();
    _sessionJob.addJobChangeListener(_listener);

    connect();

    _sessionJob.schedule();
  }

  public boolean isActive()
  {
    return _isRunning && !_wasStopped;
  }

  public boolean isDebugSession()
  {
    for (IDebugTarget debugger : _launch.getDebugTargets())
      if (debugger instanceof ACTRDebugTarget) return true;
    return false;
  }

  public ACTRDebugTarget getACTRDebugTarget()
  {
    for (IDebugTarget debugger : _launch.getDebugTargets())
      if (debugger instanceof ACTRDebugTarget)
        return (ACTRDebugTarget) debugger;
    return null;
  }

  /**
   * force the termination of the session
   */
  public void stop() throws CoreException
  {
    _wasStopped = true;
    if (_sessionJob != null) _sessionJob.cancel();
  }
}
