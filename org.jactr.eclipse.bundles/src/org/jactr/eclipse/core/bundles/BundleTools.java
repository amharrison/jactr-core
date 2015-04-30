/*
 * Created on Mar 26, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.core.bundles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

public class BundleTools extends Plugin
{
  static private transient final Log LOGGER    = LogFactory
                                                   .getLog(BundleTools.class);

  static public final String         PLUGIN_ID = "org.jactr.eclipse.core";

  // The shared instance.
  private static BundleTools          _defaultInstance;


  private BundleContext              _bundleContext;

  /**
   * The constructor.
   */
  public BundleTools()
  {
    super();
    _defaultInstance = this;
  }

  public BundleContext getBundleContext()
  {
    return _bundleContext;
  }

  /**
   * This method is called upon plug-in activation
   */
  @Override
  public void start(BundleContext context) throws Exception
  {
    super.start(context);
    _bundleContext = context;

  }

  /**
   * This method is called when the plug-in is stopped
   */
  @Override
  public void stop(BundleContext context) throws Exception
  {
    super.stop(context);
  }

  /**
   * Returns the shared instance.
   */
  public static BundleTools getDefault()
  {
    return _defaultInstance;
  }

  

  static public void log(IStatus status)
  {
    BundlesActivator.getDefault().getLog().log(status);
  }

  static public void log(int level, int code, String message, Throwable thrown)
  {
    Status status = new Status(level, BundlesActivator.PLUGIN_ID,
        code, message, thrown);
    log(status);
  }

  static public void debug(String message, Throwable thrown)
  {
    log(IStatus.INFO, 0, message, thrown);
    if (LOGGER.isDebugEnabled()) LOGGER.debug(message, thrown);
  }

  static public void debug(String message)
  {
    debug(message, null);
  }

  static public void warn(String message, Throwable thrown)
  {
    log(IStatus.WARNING, 0, message, thrown);
    if (LOGGER.isWarnEnabled()) LOGGER.warn(message, thrown);
  }

  static public void warn(String message)
  {
    warn(message, null);
  }

  static public void error(String message, Throwable thrown)
  {
    log(IStatus.ERROR, 0, message, thrown);
    if (LOGGER.isErrorEnabled()) LOGGER.error(message, thrown);
  }

  static public void error(String message)
  {
    error(message, null);
  }
}
