package org.jactr.eclipse.ui.editor.formatting;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class DocumentSeeker
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(DocumentSeeker.class);
  
  

  static public boolean matches(String pattern, IDocument document, int offset,
      boolean caseSensitive, boolean forward) throws BadLocationException
  {
    int start = offset;
    int length = pattern.length();
    if (!forward) start = start - length + 1;

    if (start < 0 || start + length > document.getLength()) return false;

    String string = document.get(start, length);

    return caseSensitive && string.equals(pattern) || !caseSensitive
        && string.equalsIgnoreCase(pattern);
  }

  /**
   * @param document
   * @param offset
   * @param forward
   * @return
   * @throws BadLocationException
   */
  static public int consumeWhitespace(IDocument document, int offset,
      boolean forward) throws BadLocationException
  {
    int increment = 1;
    if (!forward) increment = -1;

    int max = document.getLength() - 1;
    int min = 1;

    while (forward && offset < max || !forward && offset >= min)
    {
      if (!Character.isWhitespace(document.getChar(offset))) return offset;
      offset += increment;
    }

    if (forward) return document.getLength();
    return 0;
  }

  static public int consumeUntilWhitepspace(IDocument document, int offset,
      boolean forward) throws BadLocationException
  {
    int increment = 1;
    if (!forward) increment = -1;

    int max = document.getLength() - 1;
    int min = 1;

    while (forward && offset < max || !forward && offset >= min)
    {
      if (Character.isWhitespace(document.getChar(offset))) return offset;
      offset += increment;
    }

    if (forward) return document.getLength();
    return 0;
  }
  
  static public int consumeUntil(char character, IDocument document,
      int offset, int terminalOffset, boolean caseSensitive, boolean forward)
      throws BadLocationException
  {
    int increment = 1;
    if (!forward) increment = -1;
    if (!caseSensitive) character = Character.toLowerCase(character);


    while (forward && offset <= terminalOffset || !forward
        && offset >= terminalOffset)
    {
      char ch = document.getChar(offset);
      if (caseSensitive && ch == character || !caseSensitive
          && Character.toLowerCase(ch) == character) return offset;
      offset += increment;
    }

    return terminalOffset;
  }
  

  static public int consumeUntil(char character, IDocument document,
      int offset, boolean caseSensitive, boolean forward)
      throws BadLocationException
  {
    int increment = 1;
    if (!forward) increment = -1;
    if (!caseSensitive) character = Character.toLowerCase(character);

    int max = document.getLength() - 1;
    int min = 1;

    while (forward && offset < max || !forward && offset >= min)
    {
      char ch = document.getChar(offset);
      if (caseSensitive && ch == character || !caseSensitive
          && Character.toLowerCase(ch) == character) return offset;
      offset += increment;
    }

    if (forward) return document.getLength();
    return 0;
  }

  static public int consumeUntil(String pattern, IDocument document,
      int offset, boolean caseSensitive, boolean forward)
      throws BadLocationException
  {
    String searchPattern = pattern;
    int increment = 1;
    if (!forward)
    {
      searchPattern = new StringBuilder(searchPattern).reverse().toString();
      increment = -1;
    }

    int len = searchPattern.length();
    int max = document.getLength() - len;
    int min = len;

    while (forward && offset < max || !forward && offset >= min)
    {
      /*
       * got the first character.. try to snag the string to compare
       */
      char ch = document.getChar(offset);
      if (ch == searchPattern.charAt(0))
      {
        int stringStart = offset;
        int stringLength = searchPattern.length();

        int rtnOffset = Math.min(offset + stringLength, max);

        if (!forward)
        {
          stringStart = offset - stringLength + 1;
          rtnOffset = Math.max(stringStart, min);
        }

        String string = document.get(stringStart, stringLength);
        /*
         * but we compare using unmodified pattern (forward)
         */
        if (caseSensitive && string.equals(pattern) || !caseSensitive
            && string.equalsIgnoreCase(pattern)) return rtnOffset;
      }

      offset += increment;
    }

    return offset;
  }
}
