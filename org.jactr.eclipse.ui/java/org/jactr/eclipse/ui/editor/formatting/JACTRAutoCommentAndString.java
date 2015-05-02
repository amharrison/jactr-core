/*
 * Created on Apr 18, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.ui.editor.formatting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

/**
 * automatically insert full comment text after <!
 * 
 * @author developer
 */
public class JACTRAutoCommentAndString implements IAutoEditStrategy
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(JACTRAutoCommentAndString.class);

  private final boolean              _completeStrings;

  private final boolean              _completeComments;

  private final boolean              _closeCarret;

  public JACTRAutoCommentAndString(boolean completeStrings,
      boolean completeComments, boolean closeCarret)
  {
    _completeComments = completeComments;
    _completeStrings = completeStrings;
    _closeCarret = closeCarret;
  }

  public void customizeDocumentCommand(IDocument document,
      DocumentCommand command)
  {
    if (command.text.equals("<") && _closeCarret)
    {
      command.text = "<>";
      command.caretOffset = command.offset + 1;
      command.shiftsCaret = false;
    }
    else if (command.text.equals("\""))
    {
      if (!_completeStrings) return;
      command.text = "\"\"";
      command.caretOffset = command.offset + 1;
      command.shiftsCaret = false;
    }
    else if (command.text.equals("'"))
    {
      if (!_completeStrings) return;
      command.text = "''";
      command.caretOffset = command.offset + 1;
      command.shiftsCaret = false;
    }
    else
      try
      {
        if (!_completeComments) return;

        if (command.text.equals("!")
            && document.getChar(command.offset - 1) == '<')
        {
          if (_closeCarret)
            command.text = "!--  --";
          else
            command.text = "!--  -->";
          command.caretOffset = command.offset + 4;
          command.shiftsCaret = false;
        }
      }
      catch (BadLocationException ble)
      {
        LOGGER.error("Problem while inserting comment ", ble);
      }
  }

}
