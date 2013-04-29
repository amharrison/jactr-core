package org.jactr.eclipse.ui.editor.command;

/*
 * default logging
 */
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.jactr.eclipse.ui.editor.partioner.JACTRPartitions;

public class JACTRComment extends TextEditorAction
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(JACTRComment.class);

  public JACTRComment(ResourceBundle bundle, String prefix, ITextEditor editor)
  {
    super(bundle, prefix, editor);
  }

  @Override
  public void run()
  {
    ITextEditor editor = getTextEditor();
    if (editor == null) return;

    if (!validateEditorInputState()) return;

    IDocument document = getDocument(editor);
    if (document == null) return;

    ITextSelection selection = getSelection(editor);
    if (selection == null) return;

    ITypedRegion region = getRegion(document, selection);

    if (!JACTRPartitions.COMMENT.equals(region.getType()))
      comment(document, selection, region);
    else
      uncomment(document, region);
  }

  private ITypedRegion getRegion(IDocument document,
      ITextSelection textSelection)
  {
    try
    {
      return document.getPartition(textSelection.getOffset());
    }
    catch (Exception e)
    {
      return null;
    }
  }

  protected void comment(IDocument document, ITextSelection textSelection,
      ITypedRegion region)
  {
    int start = textSelection.getOffset();
    int end = start + textSelection.getLength();
    if (end == start) try
    {
      // empty selection
      start = document.getLineOffset(textSelection.getStartLine());
      end = start + document.getLineLength(textSelection.getStartLine()) - 1;
    }
    catch (BadLocationException e)
    {

    }
    try
    {
      MultiTextEdit edit = new MultiTextEdit();
      edit.addChild(new ReplaceEdit(end, 0, "\n-->"));
      edit.addChild(new ReplaceEdit(start, 0, "<!--\n"));
      edit.apply(document);
    }
    catch (BadLocationException e)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Failed to comment ", e);
    }
  }

  protected void uncomment(IDocument document, ITypedRegion region)
  {
    try
    {
      int openComment = region.getOffset();
      int closeComment = region.getLength() + openComment;


      MultiTextEdit edit = new MultiTextEdit();
      edit.addChild(new ReplaceEdit(closeComment - 3, 3, ""));
      edit.addChild(new ReplaceEdit(openComment, 4, ""));
      edit.apply(document);
    }
    catch (Exception e)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Failed to uncomment ", e);
    }
  }

  @Override
  public void update()
  {
    super.update();
    if (!isEnabled()) return;

    if (!canModifyEditor())
    {
      setEnabled(false);
      return;
    }
  }

  /**
   * Returns the editor's document.
   * 
   * @param editor
   *          the editor
   * @return the editor's document
   */
  private static IDocument getDocument(ITextEditor editor)
  {

    IDocumentProvider documentProvider = editor.getDocumentProvider();
    if (documentProvider == null) return null;

    IDocument document = documentProvider.getDocument(editor.getEditorInput());
    if (document == null) return null;

    return document;
  }

  /**
   * Returns the editor's selection.
   * 
   * @param editor
   *          the editor
   * @return the editor's selection
   */
  private static ITextSelection getSelection(ITextEditor editor)
  {

    ISelectionProvider selectionProvider = editor.getSelectionProvider();
    if (selectionProvider == null) return null;

    ISelection selection = selectionProvider.getSelection();
    if (!(selection instanceof ITextSelection)) return null;

    return (ITextSelection) selection;
  }
}
