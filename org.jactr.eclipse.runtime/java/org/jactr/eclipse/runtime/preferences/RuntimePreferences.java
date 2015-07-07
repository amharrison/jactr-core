/*
 * Created on Jun 11, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.runtime.preferences;

public interface RuntimePreferences
{

  public static final String RUNTIME_DATA_WINDOW       = "dataWindow";

  public static final String PROBE_RUNTIME_DATA_WINDOW = "probeDataWindow";

  public static final String DEBUG_STACK_PREF          = "debugStackSize";

  public static final String NORMAL_START_WAIT_PREF    = "normalWaitTime";

  public static final String ITERATIVE_START_WAIT_PREF = "iterativeWaitTime";

  public static final String ITERATIVE_BEEP_PREF       = "iterativeBeep";

  public static final String VERIFY_RUN_PREF           = "verifyRuns";

  public static final String SWITCH_TO_RUN_PERSPECTIVE   = "switchToRunPerspective";

  public static final String SWITCH_TO_DEBUG_PERSPECTIVE = "switchToDebugPerspective";
  
  public static final String DONT_ASK_RUN_SWITCH         = "dontAskToSwitchToRun";

  public static final String DONT_ASK_DEBUG_SWITCH       = "dontAskToSwitchToDebug";

  public static final String PLAYBACK_RATE               = "playbackMessagesPerSecond";

  public static final String PLAYBACK_BLOCKSIZE          = "playbackBlockSize";

  public static final String TRANSLATE_TIME              = "translateTime";

}
