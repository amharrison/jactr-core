/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.jactr.eclipse.ui.preferences;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;
import org.jactr.eclipse.ui.UIPlugin;

/**
 * @see org.eclipse.jface.preference.PreferencePage
 */
public class TemplatesPreferencePage extends TemplatePreferencePage implements
    IWorkbenchPreferencePage
{

  public TemplatesPreferencePage()
  {
    setPreferenceStore(UIPlugin.getDefault().getPreferenceStore());
    setTemplateStore(UIPlugin.getDefault().getTemplateStore());
    setContextTypeRegistry(UIPlugin.getDefault().getContextTypeRegistry());
  }

  @Override
  protected boolean isShowFormatterSetting()
  {
    return true;
  }

  @Override
  public void performApply()
  {
    UIPlugin.getDefault().savePluginPreferences();
    super.performApply();
  }
}
