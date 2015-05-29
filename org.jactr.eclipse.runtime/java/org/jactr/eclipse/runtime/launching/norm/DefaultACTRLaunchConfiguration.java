/*
 * Created on Mar 22, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.runtime.launching.norm;

import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.jactr.eclipse.runtime.debug.ACTRDebugTarget;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConstants;
import org.jactr.eclipse.runtime.launching.session.AbstractSession;
import org.jactr.eclipse.runtime.launching.session.SessionTracker;

public class DefaultACTRLaunchConfiguration extends
    AbstractACTRLaunchConfiguration
{
  /**
   * Logger definition
   */

  static final transient Log LOGGER = LogFactory
                                        .getLog(DefaultACTRLaunchConfiguration.class);

  @Override
  protected AbstractSession startSession(ILaunch launch,
      ILaunchConfigurationWorkingCopy configuration, String mode)
      throws CoreException
  {

    if (mode.equals(ILaunchManager.DEBUG_MODE))
      configuration.setAttribute(ACTRLaunchConstants.ATTR_SUSPEND, true);

    ACTRSession session = new ACTRSession(launch, configuration);

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Created session " + session);

    /*
     * we must start before we do much of anything else
     */
    session.start();

    if (mode.equals(ILaunchManager.DEBUG_MODE))
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Adding debug target");
      // create the debug target and attach it
      ACTRDebugTarget target = new ACTRDebugTarget(session);
      launch.addDebugTarget(target);
    }

    InetSocketAddress addr = session.getConnectionAddress();

    configuration.setAttribute(ACTRLaunchConstants.ATTR_DEBUG_PORT, addr
        .getPort());

    configuration.setAttribute(ACTRLaunchConstants.ATTR_DEBUG_ADDRESS, addr
        .getHostName());

    configuration.setAttribute(ACTRLaunchConstants.ATTR_CREDENTIALS, session
        .getCredentials().toString());

    return session;
  }

  @Override
  protected String getLaunchPrefix()
  {
    return "jACT-R";
  }

  /**
   * we don't support deferred executions..
   * 
   * @see org.jactr.eclipse.runtime.launching.norm.AbstractACTRLaunchConfiguration#getSessionTracker()
   */
  @Override
  protected SessionTracker getSessionTracker()
  {
    return null;
  }

}
