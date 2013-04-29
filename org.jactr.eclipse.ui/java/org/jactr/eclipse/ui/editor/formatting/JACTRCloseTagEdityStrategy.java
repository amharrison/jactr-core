package org.jactr.eclipse.ui.editor.formatting;

/*
 * default logging
 */
import javolution.util.FastSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;

public class JACTRCloseTagEdityStrategy implements IAutoEditStrategy
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(JACTRCloseTagEdityStrategy.class);

  final private boolean              _enabled;

  public JACTRCloseTagEdityStrategy(boolean enable)
  {
    _enabled = enable;
  }

  public void customizeDocumentCommand(IDocument document,
      DocumentCommand command)
  {
    if (!_enabled) return;

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Evaluating " + command.text);

    if (!command.text.equals("/")) return;
    try
    {
      if (document.getChar(command.offset - 1) != '<'
          || document.getChar(command.offset) != '>') return;
    }
    catch (Exception e)
    {
      return;
    }

    String openTag = getOpenTag(document, command.offset - 1);

    if (LOGGER.isDebugEnabled()) LOGGER.debug("openTag " + openTag);
    if (openTag == null || openTag.length() == 0) return;

    command.text = "/" + openTag;
    command.caretOffset = command.offset + openTag.length() + 2;
    command.shiftsCaret = false;
  }

  private String getOpenTag(IDocument document, int startOffset)
  {
    FastSet<String> closed = FastSet.newInstance();
    try
    {
      int closeOffset = 0;
      int openOffset = 0;
      int nextStartOffset = startOffset;
      String rtn = "";
      do
      {
        // search back to find first >
        closeOffset = DocumentSeeker.consumeUntil('>', document,
            nextStartOffset, 0, true, false);
        // then '<'
        nextStartOffset = openOffset = DocumentSeeker.consumeUntil('<',
            document, closeOffset, 0, true, false);

        openOffset++;
        boolean isOpen = true;
        if (document.getChar(openOffset) == '/')
        {
          isOpen = false;
          openOffset++;
        }
        else if (document.getChar(closeOffset - 1) == '/')
        {
          closeOffset--;
          isOpen = false;
        }

        // start at openOffset+1 and find first whitespace
        int endOfTag = DocumentSeeker.consumeUntilWhitepspace(document,
            openOffset, true);
        if (endOfTag > closeOffset) endOfTag = closeOffset;

        rtn = document.get(openOffset, endOfTag - openOffset).trim();

        if (!isOpen)
        {
          // skip comments
          if (rtn.charAt(0) != '!') closed.add(rtn);
          rtn = "";
        }
      }
      while (closeOffset > openOffset
          && (rtn.length() == 0 || closed.contains(rtn)));

      return rtn;
    }
    catch (Exception e)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Failed to find open tag ", e);
      return null;
    }
    finally
    {
      FastSet.recycle(closed);
    }
  }
}
