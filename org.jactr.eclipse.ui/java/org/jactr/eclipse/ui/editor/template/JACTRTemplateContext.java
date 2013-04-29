/*
 * Created on Apr 19, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.ui.editor.template;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.formatting.JACTRFormattingStrategy;

public class JACTRTemplateContext extends DocumentTemplateContext
{
  private boolean _useSpaces;

  private int     _tabWidth;

  public JACTRTemplateContext(TemplateContextType type, IDocument document,
      int offset, int length, boolean formatUseSpaces, int formatTabWidth)
  {
    super(type, document, offset, length);
    _useSpaces = formatUseSpaces;
    _tabWidth = formatTabWidth;
  }

  @Override
  public TemplateBuffer evaluate(Template template)
      throws BadLocationException, TemplateException
  {
    if (!canEvaluate(template)) return null;

    TemplateTranslator translator = new TemplateTranslator();
    TemplateBuffer buffer = translator.translate(template);

    boolean shouldFormat = UIPlugin.getDefault().getPreferenceStore()
        .getBoolean(
            "org.eclipse.ui.texteditor.templates.preferences.format_templates");

    if (shouldFormat)
    {
      JACTRFormattingStrategy format = new JACTRFormattingStrategy(_useSpaces,
          _tabWidth);
      setVariable("indent", format.format(buffer, this, getDocument(),
          getStart()));
    }
    else
      setVariable("indent", "");

    getContextType().resolve(buffer, this);

    return buffer;
  }
}
