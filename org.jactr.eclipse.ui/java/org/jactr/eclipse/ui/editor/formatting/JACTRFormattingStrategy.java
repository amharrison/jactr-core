/*
 * Created on Apr 17, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.jactr.eclipse.ui.editor.partioner.JACTRPartitionScanner;
import org.jactr.eclipse.ui.editor.partioner.JACTRPartitions;

/**
 * code formatter
 * 
 * @author developer
 */
public class JACTRFormattingStrategy extends ContextBasedFormattingStrategy
{

  /**
   * Logger definition
   */

  static private final transient Log      LOGGER          = LogFactory
                                                              .getLog(JACTRFormattingStrategy.class);

  static private final String             COMMENT_START   = "<!--";

  static private final String             COMMENT_END     = "-->";

  static private final String[][]         IGNORE_PATTERNS = {
      { COMMENT_START, COMMENT_END }, { "<![cdata[", "]]>" }, { "<?", "?>" } };

  private final boolean                   _trimTails      = false;

  private final LinkedList<IDocument>     _documents      = new LinkedList<IDocument>();

  private final LinkedList<TypedPosition> _partitions     = new LinkedList<TypedPosition>();

  private String                          _indentation;

  private ThreadLocal<LineIndentInfo>     _previousLine   = new ThreadLocal<LineIndentInfo>();

  public JACTRFormattingStrategy(boolean useSpacesForTabs, int tabLength)
  {
    if (useSpacesForTabs)
    {
      StringBuilder sb = new StringBuilder();
      while (tabLength-- > 0)
        sb.append(" ");
      _indentation = sb.toString();
    }
    else
      _indentation = " ";
  }

  private static class ExclusivePositionUpdater implements IPositionUpdater
  {

    /** The position category. */
    private final String fCategory;

    /**
     * Creates a new updater for the given <code>category</code>.
     * 
     * @param category
     *          the new category.
     */
    public ExclusivePositionUpdater(String category)
    {
      fCategory = category;
    }

    /*
     * @see
     * org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text
     * .DocumentEvent)
     */
    public void update(DocumentEvent event)
    {

      int eventOffset = event.getOffset();
      int eventOldLength = event.getLength();
      int eventNewLength = event.getText() == null ? 0 : event.getText()
          .length();
      int deltaLength = eventNewLength - eventOldLength;

      try
      {
        Position[] positions = event.getDocument().getPositions(fCategory);

        for (int i = 0; i != positions.length; i++)
        {

          Position position = positions[i];

          if (position.isDeleted()) continue;

          int offset = position.getOffset();
          int length = position.getLength();
          int end = offset + length;

          if (offset >= eventOffset + eventOldLength)
            // position comes
            // after change - shift
            position.setOffset(offset + deltaLength);
          else if (end <= eventOffset)
          {
            // position comes way before change -
            // leave alone
          }
          else if (offset <= eventOffset && end >= eventOffset + eventOldLength)
            // event completely internal to the position - adjust length
            position.setLength(length + deltaLength);
          else if (offset < eventOffset)
          {
            // event extends over end of position - adjust length
            int newEnd = eventOffset;
            position.setLength(newEnd - offset);
          }
          else if (end > eventOffset + eventOldLength)
          {
            // event extends from before position into it - adjust offset
            // and length
            // offset becomes end of event, length adjusted accordingly
            int newOffset = eventOffset + eventNewLength;
            position.setOffset(newOffset);
            position.setLength(end - newOffset);
          }
          else
            // event consumes the position - delete it
            position.delete();
        }
      }
      catch (BadPositionCategoryException e)
      {
        // ignore and return
      }
    }

    /**
     * Returns the position category.
     * 
     * @return the position category
     */
    public String getCategory()
    {
      return fCategory;
    }

  }

  /**
   * Wraps a {@link TemplateBuffer} and tracks the variable offsets while
   * changes to the buffer occur. Whitespace variables are also tracked.
   */
  private static final class VariableTracker
  {
    private static final String       CATEGORY = "__template_variables"; //$NON-NLS-1$

    private Document                  fDocument;

    private final TemplateBuffer      fBuffer;

    private final List<TypedPosition> fPositions;

    /**
     * Creates a new tracker.
     * 
     * @param buffer
     *          the buffer to track
     * @throws MalformedTreeException
     * @throws BadLocationException
     */
    public VariableTracker(TemplateBuffer buffer)
        throws MalformedTreeException, BadLocationException
    {
      Assert.isLegal(buffer != null);
      fBuffer = buffer;
      fDocument = new Document(fBuffer.getString());
      installPartitioner(fDocument);
      fDocument.addPositionCategory(CATEGORY);
      fDocument.addPositionUpdater(new ExclusivePositionUpdater(CATEGORY));
      fPositions = createRangeMarkers(fBuffer.getVariables(), fDocument);
    }

    /**
     * Installs a partitioner with <code>document</code>.
     * 
     * @param document
     *          the document
     */
    private static void installPartitioner(Document document)
    {
      String[] types = new String[] { JACTRPartitions.COMMENT,
          JACTRPartitions.IDENTIFIER, IDocument.DEFAULT_CONTENT_TYPE };
      FastPartitioner partitioner = new FastPartitioner(
          new JACTRPartitionScanner(), types);
      partitioner.connect(document);
      document.setDocumentPartitioner(JACTRPartitions.ID, partitioner);
    }

    /**
     * Returns the document with the buffer contents. Whitespace variables are
     * decorated with comments.
     * 
     * @return the buffer document
     */
    public IDocument getDocument()
    {
      checkState();
      return fDocument;
    }

    private void checkState()
    {
      if (fDocument == null) throw new IllegalStateException();
    }

    /**
     * Restores any decorated regions and updates the buffer's variable offsets.
     * 
     * @return the buffer.
     * @throws MalformedTreeException
     * @throws BadLocationException
     */
    public TemplateBuffer updateBuffer() throws MalformedTreeException,
        BadLocationException
    {
      checkState();
      TemplateVariable[] variables = fBuffer.getVariables();
      try
      {
        removeRangeMarkers(fPositions, fDocument, variables);
      }
      catch (BadPositionCategoryException x)
      {
        Assert.isTrue(false);
      }
      fBuffer.setContent(fDocument.get(), variables);
      fDocument = null;

      return fBuffer;
    }

    private List<TypedPosition> createRangeMarkers(
        TemplateVariable[] variables, IDocument document)
        throws MalformedTreeException, BadLocationException
    {
      Map<ReplaceEdit, String> markerToOriginal = new HashMap<ReplaceEdit, String>();

      MultiTextEdit root = new MultiTextEdit(0, document.getLength());
      List<TextEdit> edits = new ArrayList<TextEdit>();
      boolean hasModifications = false;
      for (int i = 0; i != variables.length; i++)
      {
        final TemplateVariable variable = variables[i];
        int[] offsets = variable.getOffsets();

        String value = variable.getDefaultValue();
        if (isWhitespaceVariable(value))
        {
          // replace whitespace positions with unformattable comments
          String placeholder = COMMENT_START + value + COMMENT_END;
          for (int j = 0; j != offsets.length; j++)
          {
            ReplaceEdit replace = new ReplaceEdit(offsets[j], value.length(),
                placeholder);
            root.addChild(replace);
            hasModifications = true;
            markerToOriginal.put(replace, value);
            edits.add(replace);
          }
        }
        else
          for (int j = 0; j != offsets.length; j++)
          {
            RangeMarker marker = new RangeMarker(offsets[j], value.length());
            root.addChild(marker);
            edits.add(marker);
          }
      }

      if (hasModifications) // update the document and convert the replaces to
        // markers
        root.apply(document, TextEdit.UPDATE_REGIONS);

      List<TypedPosition> positions = new ArrayList<TypedPosition>();
      for (TextEdit edit : edits)
        try
        {
          // abuse TypedPosition to piggy back the original contents of the
          // position
          final TypedPosition pos = new TypedPosition(edit.getOffset(), edit
              .getLength(), markerToOriginal.get(edit));
          document.addPosition(CATEGORY, pos);
          positions.add(pos);
        }
        catch (BadPositionCategoryException x)
        {
          Assert.isTrue(false);
        }

      return positions;
    }

    private boolean isWhitespaceVariable(String value)
    {
      int length = value.length();
      return length == 0 || Character.isWhitespace(value.charAt(0))
          || Character.isWhitespace(value.charAt(length - 1));
    }

    private void removeRangeMarkers(List<TypedPosition> positions,
        IDocument document, TemplateVariable[] variables)
        throws MalformedTreeException, BadLocationException,
        BadPositionCategoryException
    {

      // revert previous changes
      for (TypedPosition position : positions)
      {
        // remove and re-add in order to not confuse ExclusivePositionUpdater
        document.removePosition(CATEGORY, position);
        final String original = position.getType();
        if (original != null)
        {
          document
              .replace(position.getOffset(), position.getLength(), original);
          position.setLength(original.length());
        }
        document.addPosition(position);
      }

      Iterator<TypedPosition> it = positions.iterator();
      for (int i = 0; i != variables.length; i++)
      {
        TemplateVariable variable = variables[i];

        int[] offsets = new int[variable.getOffsets().length];
        for (int j = 0; j != offsets.length; j++)
          offsets[j] = it.next().getOffset();

        variable.setOffsets(offsets);
      }

    }
  }

  /*
   * @see
   * org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStarts
   * (org.eclipse.jface.text.formatter.IFormattingContext)
   */
  @Override
  public void formatterStarts(final IFormattingContext context)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("formatterStarts");
    super.formatterStarts(context);

    _partitions.addLast((TypedPosition) context
        .getProperty(FormattingContextProperties.CONTEXT_PARTITION));
    _documents.addLast((IDocument) context
        .getProperty(FormattingContextProperties.CONTEXT_MEDIUM));
  }

  /*
   * @see
   * org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy#formatterStops
   * ()
   */
  @Override
  public void formatterStops()
  {
    super.formatterStops();

    _partitions.clear();
    _documents.clear();
  }

  @Override
  public void format()
  {
    super.format();

    IDocument document = _documents.removeFirst();
    TypedPosition partition = _partitions.removeFirst();

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Formatting " + document + " " + partition);

    if (document != null && partition != null)
    {
      Map partitioners = null;
      try
      {
        partitioners = TextUtilities.removeDocumentPartitioners(document);
        format(document, partition, true);
      }
      catch (BadLocationException ble)
      {
        LOGGER.error(ble);
      }
      finally
      {
        if (partitioners != null)
          TextUtilities.addDocumentPartitioners(document, partitioners);
      }
    }
  }

  /*
   * @see
   * org.eclipse.jface.text.formatter.IFormattingStrategy#format(java.lang.String
   * , boolean, java.lang.String, int[])
   */
  @Override
  public String format(String content, boolean start, String indentation,
      int[] positions)
  {
    return null;
  }

  @Override
  public void formatterStarts(String initialIndentation)
  {

  }

  /**
   * perform the actual editing
   * 
   * @param document
   * @param partition
   * @return
   */
  private void format(IDocument document, TypedPosition partition,
      boolean resetIndentInfo) throws BadLocationException
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Formatting " + partition.getType());

    int startLine = document.getLineOfOffset(partition.getOffset());
    int endLine = document.getLineOfOffset(partition.getOffset()
        + partition.getLength());

    if (resetIndentInfo) _previousLine.set(null);

    MultiTextEdit mte = new MultiTextEdit();
    for (int line = startLine; line <= endLine; line++)
      try
      {
        TextEdit edit = formatLine(document, line);
        if (edit != null)
        // edit.apply(document);
          mte.addChild(edit);

        if (_trimTails)
        {
          edit = trimLine(document, line);
          if (edit != null)
          // edit.apply(document);
            mte.addChild(edit);
        }
      }
      catch (BadLocationException ble)
      {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("BLE : " + ble);
      }

    mte.apply(document, 0);
  }

  private TextEdit trimLine(IDocument document, int line)
      throws BadLocationException
  {
    int lineStart = document.getLineOffset(line);
    String delimit = document.getLineDelimiter(line);

    if (delimit != null)
    {
      int lineLength = document.getLineLength(line) - delimit.length();
      int whiteEnd = lineStart + lineLength;
      int textEnd = whiteEnd;

      while (Character.isWhitespace(document.getChar(textEnd))
          && textEnd > lineStart)
        textEnd--;

      textEnd++;

      if (textEnd != whiteEnd && whiteEnd - textEnd > 0 && textEnd >= 0)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Will trim '"
              + document.get(textEnd, whiteEnd - textEnd) + "'");
        return new DeleteEdit(textEnd, whiteEnd - textEnd);
      }
    }
    return null;
  }

  private TextEdit formatLine(IDocument document, int line)
      throws BadLocationException
  {
    int lineStart = document.getLineOffset(line);
    int lineLength = document.getLineLength(line);

    int leadingWhite = getWhitespaces(document, lineStart, lineStart
        + lineLength);

    if (leadingWhite == lineLength)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Line is all white");
      return null;
    }

    /*
     * only going to format lines that start w/ < and end with >
     */
    int startOfLinePos = lineStart + leadingWhite;
    char startOfLine = document.getChar(startOfLinePos);
    int endOfLinePos = DocumentSeeker.consumeUntil('>', document, lineStart
        + lineLength - 1, startOfLinePos, false, false);
    char endOfLine = document.getChar(endOfLinePos);
    if (startOfLine != '<' || endOfLine != '>')
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Line not element " + startOfLine + " " + endOfLine + " ["
            + startOfLinePos + "," + endOfLinePos + "]");
      return null;
    }
    
    /*
     * let's make sure it's not a single line comment, cdata or meta..
     */
    char startOneIn = document.getChar(startOfLinePos + 1);
    char endOneIn = document.getChar(endOfLine - 1);
    if (startOneIn == '!' || startOneIn == '?' || endOneIn == '-'
        || endOneIn == ']' || endOneIn == '?')
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Line is comment, meta or cdata, ignoring");
      return null;
    }
      

    int slashPos = DocumentSeeker.consumeUntil('/', document, lineStart,
        lineStart + lineLength, false, true);
    boolean isClosed = document.getChar(slashPos) == '/';
    boolean isSingle = false;

    if (isClosed)
    {
      /*
       * there are two types of singleton lines, basic and compound. basic :
       * <xxxx /> compound <xxxx> adfasdfsdf </xxxx>
       */
      isSingle = slashPos + 1 < document.getLength()
          && document.getChar(slashPos + 1) == '>';
      /*
       * cheap hack will be confused by '< /xxxx>'
       */
      if (!isSingle) isSingle = startOfLinePos + 1 < slashPos;

    }

    String currentLine = document.get(lineStart, lineLength);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Formatting line " + line + "(" + lineStart + ":"
          + lineLength + ") '" + currentLine + "'");
    /*
     * not an empty line
     */
    StringBuilder newIndent = getRecommendedIndentation(document, lineStart,
        !isClosed, isSingle);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("recommended indent '" + newIndent + "'");

    if (newIndent.length() == 0) return null;

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Replacing " + lineStart + ":" + leadingWhite + " with '"
          + newIndent + "'");

    // no change
    if (newIndent.length() == leadingWhite) return null;

    return new ReplaceEdit(lineStart, leadingWhite, newIndent.toString());
  }

  private int skipBack(IDocument document, int startOffset)
      throws BadLocationException
  {
    while ((startOffset = DocumentSeeker.consumeUntil('>', document,
        startOffset, false, false)) > 0)
    {
      /*
       * if it is any of the ignore pairs, we skip
       */
      boolean foundIgnore = false;
      for (String[] ignorePair : IGNORE_PATTERNS)
        if (DocumentSeeker.matches(ignorePair[1], document, startOffset, false,
            false))
        {
          startOffset -= ignorePair[1].length();

          startOffset = DocumentSeeker.consumeUntil(ignorePair[0], document,
              startOffset, false, false);

          foundIgnore = true;
          break;
        }

      if (foundIgnore) continue;

      if (document.getChar(startOffset) == '>') break;

      startOffset--;
    }
    return startOffset;
  }

  private StringBuilder getRecommendedIndentation(IDocument document,
      int startOffset, boolean lineIsOpen, boolean lineIsSingle)
      throws BadLocationException
  {
    StringBuilder rtn = null;

    /*
     * if this is not the first run, we reuse the result of the last run to
     * compute the indentation.
     */
    LineIndentInfo previousLine = _previousLine.get();
    if (previousLine != null)
    {
      rtn = previousLine.indentation;
      /*
       * the line <>, so we indent
       */
      if (previousLine.isOpen) rtn.append(_indentation);
    }
    else
    {
      /*
       * cant recycle. First we determine the indentation of the previous line
       * that is not a comment.
       */
      rtn = new StringBuilder();

      /*
       * find '>' that is not a comment.
       */
      int gtOffset = skipBack(document, startOffset);

      /*
       * we've found the first '>' that is not ignored before the desired line.
       * if '/' is on the reference line (i.e. '/>' or '</..>') we use that
       * reference line's indentation. if '/' is on a different line, take
       * reference line's indentation and indent
       */
      if (document.getChar(gtOffset) == '>')
      {
        int referenceLine = document.getLineOfOffset(gtOffset);

        int referenceLineStartOffset = document.getLineOffset(referenceLine);

        int firstNonWhitespace = DocumentSeeker.consumeWhitespace(document,
            referenceLineStartOffset, true);

        rtn.append(document.get(referenceLineStartOffset, firstNonWhitespace
            - referenceLineStartOffset));

        /*
         * seek back for the '/'
         */
        int slashOffset = DocumentSeeker.consumeUntil('/', document, gtOffset,
            false, false);
        /*
         * '/' was on a different line (i.e. it was open)
         */
        if (slashOffset < referenceLineStartOffset) rtn.append(_indentation);

        if (LOGGER.isDebugEnabled())
          LOGGER.debug("prior indent : gtOff:" + gtOffset + " refLine:"
              + referenceLine + " refOff:" + referenceLineStartOffset
              + " firstNon:" + firstNonWhitespace + " slashOff:" + slashOffset);
      }
    }

    /*
     * now we need to consider that relevant line actually looks like. is it a
     * closed node? (suggesting that we need to unindent..) '</', we undo an
     * indentation.
     */

    if (!lineIsOpen && !lineIsSingle)
      if (rtn.length() >= _indentation.length())
        rtn.delete(rtn.length() - _indentation.length(), rtn.length());

    _previousLine.set(new LineIndentInfo(rtn, lineIsOpen));
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("stashed indentinfo :" + lineIsOpen + " : '" + rtn + "'");

    return rtn;
  }

  /**
   * how much whitespace between start and end the first non-whitespace.
   * 
   * @param document
   * @param offset
   * @return
   */
  private int getWhitespaces(IDocument document, int startOffset, int endOffset)
      throws BadLocationException
  {
    int rtn = 0;
    for (int i = startOffset; i < endOffset; i++)
      if (Character.isWhitespace(document.getChar(i)))
        rtn++;
      else
        break;

    return rtn;
  }

  private int getWhitespaces(IDocument document, int endOffset)
      throws BadLocationException
  {
    return getWhitespaces(document, document.getLineOffset(document
        .getLineOfOffset(endOffset)), endOffset);
  }

  /**
   * Formats the template buffer.
   * 
   * @param buffer
   * @param context
   * @return initial indentation
   * @throws BadLocationException
   */
  public String format(TemplateBuffer buffer, TemplateContext context,
      IDocument destinationDocument, int offset) throws BadLocationException
  {
    try
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Trying to format buffer " + buffer.getString());
      VariableTracker tracker = new VariableTracker(buffer);
      IDocument document = tracker.getDocument();
      
      int priorWhite = getWhitespaces(destinationDocument, offset);

      // reset..
      _previousLine.set(null);

      /*
       * first off, we insert the indention defined by destination document
       */
      String newIndent = getRecommendedIndentation(destinationDocument, offset,
          false, true).toString();
      /*
       * and apply the recommendation to the temporary document
       */
      new InsertEdit(0, newIndent).apply(document);

      format(document, new TypedPosition(0, document.getLength(),
          IDocument.DEFAULT_CONTENT_TYPE), false);

      /*
       * if the insertion point had indentation, we remove the recommended
       * indentation, otherwise we'd get a double indent
       */
      if (priorWhite != 0) new DeleteEdit(0, priorWhite).apply(document);
      
      tracker.updateBuffer();

      return newIndent;
    }
    catch (MalformedTreeException e)
    {
      throw new BadLocationException("Malformed tree : " + e.getMessage());
    }
  }

  private class LineIndentInfo
  {
    public StringBuilder indentation;

    public boolean       isOpen;

    public LineIndentInfo(StringBuilder indentation, boolean isOpen)
    {
      this.indentation = indentation;
      this.isOpen = isOpen;
    }
  }
}
