/**
 * Copyright (C) 2001-3, Anthony Harrison anh23@pitt.edu This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/*
 * Created on May 5, 2004 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jactr.eclipse.runtime.ui.tabs.iterative;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaArgumentsTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;
import org.jactr.eclipse.runtime.ui.tabs.normal.LoggingAndTraceTab;

/**
 * @author harrison To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class IterativeLaunchConfigurationTabGroup extends
    AbstractLaunchConfigurationTabGroup
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(IterativeLaunchConfigurationTabGroup.class);

  public void createTabs(ILaunchConfigurationDialog dialog, String mode)
  {
    Collection<AbstractLaunchConfigurationTab> options = new ArrayList<AbstractLaunchConfigurationTab>();

    options.add(new IterativeMainTab());
    options.add(new IterativeListenersTab());
    options.add(new LoggingAndTraceTab(false));
    options.add(new JavaArgumentsTab());
    options.add(new JavaJRETab());
    options.add(new CommonTab());

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Setting tabs");

    setTabs(options.toArray(new ILaunchConfigurationTab[options.size()]));
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
  {
    /*
     * for some reason, this isn't getting called - no log messages?
     */
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Setting defaults!!");
    super.setDefaults(configuration);
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration)
  {
    /*
     * for some reason, this isn't getting called - no log messages?
     */
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Applying");
    super.performApply(configuration);
  }

}