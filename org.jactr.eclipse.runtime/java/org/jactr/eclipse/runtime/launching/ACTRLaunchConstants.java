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
package org.jactr.eclipse.runtime.launching;

import org.jactr.eclipse.core.builder.LaunchConfigurationCleaner;

/**
 * constants for launching
 * 
 * @author developer
 */
public class ACTRLaunchConstants
{

  /**
   * used for CR runs, does it include a BS agent to send data to
   */
  static public final String  INCLUDE_MOCK_AGENT              = "org.commonreality.runtime.launching.includeMockAgent";

  static private final String ACTR                            = "org.jactr.eclipse.runtime.launching.";

  /**
   * the eclipse platform application to run normally
   */
  static public final String  DEFAULT_APPLICATION             = "org.jactr.launching.application";

  static public final String  DEFAULT_APPLICATION_BUNDLE      = "org.jactr.launching";

  static public final String  ITERATIVE_APPLICATION           = DEFAULT_APPLICATION;

  static public final String  ITERATIVE_APPLICATION_BUNDLE    = DEFAULT_APPLICATION_BUNDLE;

  /**
   * the current run model only supports the interactive running (having the
   * runtime connect to the ShadowController that eclipse is running, then
   * sending the start command). when a none interactive start is created, we
   * can use -r again
   */
  static public final String  DEFAULT_APPLICATION_RUN_ARG     = "-e";

  static public final String  DEFAULT_CR_RUN_ARG              = "-r";

  static public final String  DEFAULT_APPLICATION_DEBUG_ARG   = "-e";

  static public final String  ITERATIVE_APPLICATION_ARG       = "-i";

  /**
   * the workspace directory to use
   */
  static public final String  NORMAL_WORKSPACE_LOCATION       = "${system_property:user.home}/.jactr/workspaces/${actr_project}";

  static public final String  NORMAL_CONFIGURATION_LOCATION   = LaunchConfigurationCleaner.NORMAL_CONFIGURATION_LOCATION;

  static public final String  ATTR_SAVE_RUN                   = ACTR
                                                                  + "saveRun";

  static public final String  ATTR_DEBUG_PORT                 = ACTR
                                                                  + "debugPort";

  static public final String  ATTR_DEBUG_ADDRESS              = ACTR
                                                                  + "debugAddress";

  static public final String  ATTR_CREDENTIALS                = ACTR
                                                                  + "credentials";

  static public final String  ATTR_SUSPEND                    = ACTR
                                                                  + "suspendImmediately";

  static public final String  ATTR_DEBUG_CORE_ENABLED         = ACTR
                                                                  + "debugCoreEnabled";

  static public final String  ATTR_DEBUG_CORE_LOGGER          = ACTR
                                                                  + "debugCoreLogger";

  static public final String  ATTR_DEBUG_CORE_LOG_CONF        = ACTR
                                                                  + "debugCoreLogConfiguration";

  static public final String  DEFAULT_CORE_LOGGER             = "org.apache.commons.logging.impl.Log4JLogger";

  static public final String  DEFAULT_CORE_LOG_CONF           = "/jactr-log.xml";

  static public final String  ATTR_MODEL_FILES                = ACTR
                                                                  + "modelFiles";

  /**
   * modelfile gets appended to this
   */
  static public final String  ATTR_MODEL_ALIASES              = ACTR
                                                                  + "aliasesFor.";

  static public final String  ATTR_USE_EMBED_CONTROLLER       = ACTR
                                                                  + "useEmbed";
  /**
   * 
   */
  static public final String  ATTR_COMMON_REALITY_SENSORS     = ACTR
                                                                  + "sensors";

  /**
   * int, acknowledgement time that CR will wait to hear back from participants
   * in before disconnecting
   */
  static public final String  ATTR_COMMON_REALITY_ACK_TIME    = ACTR
                                                                  + "cr.ackTime";

  /**
   * boolean disconnect if participants dont ack in time
   */
  static public final String  ATTR_COMMON_REALITY_DISCONNECT  = ACTR
                                                                  + "cr.disconnect";

  static public final String  ATTR_INSTRUMENTS                = ACTR
                                                                  + "instruments";

  static public final String  ATTR_ITERATIVE_LISTENERS        = ACTR
                                                                  + "iterativeListeners";

  static public final String  ATTR_ITERATIVE_DEADLOCK_TIMEOUT = ACTR
                                                                  + "iterativeDeadlock";

  /**
   * append modelFile, instrument or sensor classnames coma separated list of
   * paramter names
   */
  static public final String  ATTR_PARAMETERS                 = ACTR
                                                                  + "parametersFor.";

  /**
   * append (modelFile,instrument,sensor).parameterName
   */
  static public final String  ATTR_PARAMETER_VALUE            = ACTR
                                                                  + "parameterValueFor.";

  static public final String  ATTR_ON_START                   = ACTR
                                                                  + "onStart";

  static public final String  ATTR_ON_STOP                    = ACTR + "onStop";

  /**
   * how many iterations if this is an iterative run?
   */
  static public final String  ATTR_ITERATIONS                 = ACTR
                                                                  + "iterations";

  static public final String  ATTR_SOURCE_CONFIG              = ACTR
                                                                  + "sourceConfiguration";

  static public final String  ATTR_TRACERS                    = ACTR
                                                                  + "tracers";

  static public final String  ATTR_IDE_TRACE                  = ACTR
                                                                  + "ideTrace";

  static public final String  ATTR_RECORD_TRACE               = ACTR
                                                                  + "recordTrace";
}
