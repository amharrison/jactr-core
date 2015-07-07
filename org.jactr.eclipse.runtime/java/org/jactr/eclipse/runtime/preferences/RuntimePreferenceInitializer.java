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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jactr.eclipse.runtime.RuntimePlugin;

public class RuntimePreferenceInitializer extends AbstractPreferenceInitializer
{

  @Override
  public void initializeDefaultPreferences()
  {
    IPreferenceStore prefs = RuntimePlugin.getDefault().getPreferenceStore();

    prefs.setDefault(RuntimePreferences.RUNTIME_DATA_WINDOW, 2400);
    prefs.setDefault(RuntimePreferences.ITERATIVE_START_WAIT_PREF, 60);
    prefs.setDefault(RuntimePreferences.NORMAL_START_WAIT_PREF, 60);
    prefs.setDefault(RuntimePreferences.ITERATIVE_BEEP_PREF, true);
    prefs.setDefault(RuntimePreferences.DEBUG_STACK_PREF, 20);
    prefs.setDefault(RuntimePreferences.VERIFY_RUN_PREF, false);
    prefs.setDefault(RuntimePreferences.PROBE_RUNTIME_DATA_WINDOW, 2400);
    prefs.setDefault(RuntimePreferences.PLAYBACK_RATE, 1000);
    prefs.setDefault(RuntimePreferences.PLAYBACK_BLOCKSIZE, 200);
    prefs.setDefault(RuntimePreferences.TRANSLATE_TIME, true);

    prefs.setDefault(RuntimePreferences.DONT_ASK_RUN_SWITCH, false);
    prefs.setDefault(RuntimePreferences.DONT_ASK_DEBUG_SWITCH, false);
    prefs.setDefault(RuntimePreferences.SWITCH_TO_RUN_PERSPECTIVE, true);
    prefs.setDefault(RuntimePreferences.SWITCH_TO_DEBUG_PERSPECTIVE, true);
  }

}
