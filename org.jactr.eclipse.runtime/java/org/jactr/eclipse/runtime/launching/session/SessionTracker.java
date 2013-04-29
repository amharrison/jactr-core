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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.jactr.core.utils.collections.CachedCollection;
import org.jactr.eclipse.core.CorePlugin;

public class SessionTracker<S extends AbstractSession>
{
  static private transient final Log    LOGGER = LogFactory
                                                   .getLog(SessionTracker.class);

  Collection<S>                         _activeSessions;

  List<Object[]>                        _queuedSessions;

  CachedCollection<ISessionListener<S>> _listeners;

  public SessionTracker()
  {
    _listeners = new CachedCollection<ISessionListener<S>>(
        new ArrayList<ISessionListener<S>>());
    _activeSessions = new ArrayList<S>();
    _queuedSessions = new ArrayList<Object[]>();
  }

  public void addListener(ISessionListener<S> listener)
  {
    _listeners.add(listener);
  }

  public void removeListener(ISessionListener<S> listener)
  {
    _listeners.remove(listener);
  }

  public Collection<S> getActiveSessions()
  {
    return new ArrayList<S>(_activeSessions);
  }

  public boolean hasActiveSessions()
  {
    return _activeSessions.size() != 0;
  }

  protected void destroy(S session)
  {
    for (ISessionListener<S> listener : _listeners.getCachedValues())
      try
      {
        listener.sessionDestroyed(session);
      }
      catch (Exception e)
      {
        LOGGER.error("Failed to propogate event ", e);
      }
  }

  protected void add(S session)
  {
    _activeSessions.add(session);
    // notify
    for (ISessionListener<S> listener : _listeners.getCachedValues())
      try
      {
        listener.sessionOpened(session);
      }
      catch (Exception e)
      {
        LOGGER.error("failed to send event ", e);
      }
  }

  protected void remove(S session, boolean normal)
  {
    _activeSessions.remove(session);

    // notify
    for (ISessionListener<S> listener : _listeners.getCachedValues())
      try
      {
        listener.sessionClosed(session, normal);
      }
      catch (Exception e)
      {
        LOGGER.error("failed to send event", e);
      }

    if (_queuedSessions.size() != 0 && _activeSessions.size() == 0)
      launchQueued(false);
  }

  protected void cancel(S session)
  {
    remove(session, false);
  }

  public void addDeferredLaunch(ILaunchConfiguration configuration, String mode)
  {
    _queuedSessions.add(new Object[] { configuration, mode });
  }

  public int getNumberOfDeferredLaunches()
  {
    return _queuedSessions.size();
  }

  protected void launchQueued(boolean queryFirst)
  {

    if (queryFirst)
    {
      /*
       * prompt the user. should we defer?
       */
      FutureTask<Integer> response = new FutureTask<Integer>(
          new Callable<Integer>() {
            public Integer call()
            {
              String[] buttons = new String[] { "Cancel All", "Run Queued" };
              MessageDialog prompt = new MessageDialog(Display.getDefault()
                  .getActiveShell(), "Runs queued", null, "There are "
                  + _queuedSessions.size()
                  + " runs pending. Cancel them as well?",
                  MessageDialog.QUESTION, buttons, 1);
              return prompt.open();
            }
          });

      Display.getDefault().syncExec(response);

      try
      {
        if (response.get() == 0)
        {
          _queuedSessions.clear();
          return;
        }
      }
      catch (Exception e)
      {

      }
    }

    Object[] info = _queuedSessions.remove(0);
    ILaunchConfiguration configuration = (ILaunchConfiguration) info[0];
    String mode = (String) info[1];

    try
    {
      configuration.launch(mode, new NullProgressMonitor());
    }
    catch (CoreException e)
    {
      CorePlugin.error(
          "Could not launch deferred execution " + configuration.getName(), e);
    }
  }
}
