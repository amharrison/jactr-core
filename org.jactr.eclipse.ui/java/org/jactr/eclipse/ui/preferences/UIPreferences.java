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
package org.jactr.eclipse.ui.preferences;

import org.eclipse.swt.SWT;

public interface UIPreferences
{

  public static final String CUSTOM_TEMPLATES_PREF  = "org.jactr.eclipse.ui.template.customtemplates"; //$NON-NLS-1$

  public static final int    DEFAULT_KEYWORD        = SWT.COLOR_BLUE;

  public static final int    DEFAULT_STRING         = SWT.COLOR_DARK_BLUE;

  public static final int    DEFAULT_COMMENT        = SWT.COLOR_DARK_RED;

  public static final String KEYWORD_COLOR_PREF     = "keywords";

  public static final String STRING_COLOR_PREF      = "string";

  public static final String COMMENT_COLOR_PREF     = "comment";

  public static final String ENABLE_RECONCILER_PREF = "enableReconciler";

  public static final String ENABLE_HYPERLINK_PREF  = "enableHyperlink";

  public static final String ENABLE_FOLDING_PREF    = "enableFolding";

  public static final String ENABLE_ASSIST_PREF     = "enableAssist";

  public static final String ENABLE_HOVER_PREF      = "enableHover";

  public static final String ENABLE_FORMAT_PREF     = "enableFormatOnSave";
  
  public static final String ENABLE_AUTO_ACTIVATE_PREF = "enableAutoActivate";

  public static final String ENABLE_STRING_COMPLETION  = "enableStringCompletion";

  public static final String ENABLE_COMMENT_COMPLETION = "enableCommentCompletion";

  public static final String ENABLE_CARRET_COMPLETION  = "enableCarretCompletion";

  public static final String ENABLE_CLOSE_COMPLETION   = "enableCloseCompletion";
}
