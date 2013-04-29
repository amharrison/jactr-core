/*
 * Created on Jul 8, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.jactr.eclipse.ui.UIPlugin;

public class UIPreferenceInitiliazer extends AbstractPreferenceInitializer
{

  @Override
  public void initializeDefaultPreferences()
  {
    IPreferenceStore prefs = UIPlugin.getDefault().getPreferenceStore();

    /*
     * set up the colors
     */
    Color color = Display.getDefault().getSystemColor(
        UIPreferences.DEFAULT_KEYWORD);
    PreferenceConverter.setDefault(prefs, UIPreferences.KEYWORD_COLOR_PREF,
        color.getRGB());

    color = Display.getDefault().getSystemColor(UIPreferences.DEFAULT_STRING);
    PreferenceConverter.setDefault(prefs, UIPreferences.STRING_COLOR_PREF,
        color.getRGB());

    color = Display.getDefault().getSystemColor(UIPreferences.DEFAULT_COMMENT);
    PreferenceConverter.setDefault(prefs, UIPreferences.COMMENT_COLOR_PREF,
        color.getRGB());

    /*
     * editor behavior
     */
    prefs.setDefault(UIPreferences.ENABLE_RECONCILER_PREF, true);
    prefs.setDefault(UIPreferences.ENABLE_ASSIST_PREF, true);
    prefs.setDefault(UIPreferences.ENABLE_HOVER_PREF, true);
    prefs.setDefault(UIPreferences.ENABLE_HYPERLINK_PREF, true);
    prefs.setDefault(UIPreferences.ENABLE_FOLDING_PREF, false);
    prefs.setDefault(UIPreferences.ENABLE_FORMAT_PREF, false);
    prefs.setDefault(UIPreferences.ENABLE_AUTO_ACTIVATE_PREF, false);
    prefs.setDefault(UIPreferences.ENABLE_STRING_COMPLETION, true);
    prefs.setDefault(UIPreferences.ENABLE_COMMENT_COMPLETION, true);
    prefs.setDefault("org.eclipse.ui.texteditor.templates.preferences.format_templates", true);
  }

}
