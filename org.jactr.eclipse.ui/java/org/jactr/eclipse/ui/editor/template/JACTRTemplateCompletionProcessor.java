/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.jactr.eclipse.ui.editor.template;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.preferences.UIPreferences;

/**
 * A completion processor for XML templates.
 */
public class JACTRTemplateCompletionProcessor extends
    ACTRTemplateCompletionProcessor
{

  private static final String DEFAULT_IMAGE          = "icons/template.gif"; //$NON-NLS-1$

  boolean                     _useSpaces             = false;

  int                         _tabWidth              = 0;

  boolean                     _autoActivationEnabled = false;

  public JACTRTemplateCompletionProcessor(ACTRModelEditor editor,
      boolean formatUseSpaces, int formatTabWidth)
  {
    super(editor, "jactr");
    _useSpaces = formatUseSpaces;
    _tabWidth = formatTabWidth;

    _autoActivationEnabled = UIPlugin.getDefault().getPluginPreferences()
        .getBoolean(UIPreferences.ENABLE_AUTO_ACTIVATE_PREF);
  }

  @Override
  protected TemplateContext createContext(ITextViewer viewer, IRegion region)
  {
    TemplateContextType contextType = getContextType(viewer, region);
    if (contextType != null)
      return new JACTRTemplateContext(contextType, viewer.getDocument(), region
          .getOffset(), region.getLength(), _useSpaces, _tabWidth);
    return null;
  }

  /**
   * Always return the default image.
   * 
   * @param template
   *          the template, ignored in this implementation
   * @return the default template image
   */
  @Override
  protected Image getImage(Template template)
  {
    ImageRegistry registry = UIPlugin.getDefault().getImageRegistry();
    Image image = registry.get(DEFAULT_IMAGE);
    if (image == null)
    {
      ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(
          "org.jactr.eclipse.ui", DEFAULT_IMAGE); //$NON-NLS-1$
      registry.put(DEFAULT_IMAGE, desc);
      image = registry.get(DEFAULT_IMAGE);
    }
    return image;
  }

  @Override
  public char[] getCompletionProposalAutoActivationCharacters()
  {
    if (_autoActivationEnabled) return new char[] { '\n', '\r' };
    return null;
  }
}
