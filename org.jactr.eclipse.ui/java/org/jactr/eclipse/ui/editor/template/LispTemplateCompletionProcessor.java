/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html Contributors:
 * IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.jactr.eclipse.ui.editor.template;

import java.util.ArrayList;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;

/**
 * A completion processor for XML templates.
 */
public class LispTemplateCompletionProcessor extends
    ACTRTemplateCompletionProcessor
{
  private static final String DEFAULT_IMAGE = "icons/template.gif"; //$NON-NLS-1$
  
  private Template[]          _copies;

  public LispTemplateCompletionProcessor(ACTRModelEditor editor)
  {
    super(editor, "lisp");
  }

  @Override
  protected TemplateContext createContext(ITextViewer viewer, IRegion region)
  {
    // TemplateContextType contextType = getContextType(viewer, region);
    // tmp hack to provide all templates
    ContributionContextTypeRegistry registry = UIPlugin.getDefault()
        .getContextTypeRegistry();
    TemplateContextType contextType = registry.getContextType("lisp.rhs");
    if (contextType != null)
      return new LispTemplateContext(contextType, viewer.getDocument(), region
          .getOffset(), region.getLength());
     return null;
  }

  @Override
  protected Template[] getTemplates(String contextTypeId)
  {
    if (_copies == null)
    {
      ArrayList<Template> copies = new ArrayList<Template>();
      for (Template template : super.getTemplates(contextTypeId))
        copies.add(new Template(template) {
          @Override
          public boolean matches(String prefix, String contextTypeId)
          {
            return getContextTypeId().startsWith("lisp");
          }
        });
      _copies = copies.toArray(new Template[0]);
    }
    return _copies;
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

}
