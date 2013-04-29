package org.jactr.eclipse.ui.editor.assist;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class CommentAssistProcessor implements IContentAssistProcessor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(CommentAssistProcessor.class);

  public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
      int offset)
  {
    String[] tags = new String[] { "previous", "previous-sub", "next",
        "next-sub", "alternate" };
    ICompletionProposal[] proposals = new ICompletionProposal[tags.length];
    for (int i = 0; i < proposals.length; i++)
      proposals[i] = new CompletionProposal(tags[i], offset, 0,
          offset + tags[i].length());
    return proposals;
  }

  public IContextInformation[] computeContextInformation(ITextViewer viewer,
      int offset)
  {
    return null;
  }

  public char[] getCompletionProposalAutoActivationCharacters()
  {
    return new char[] { '@' };
  }

  public char[] getContextInformationAutoActivationCharacters()
  {
    return null;
  }

  public IContextInformationValidator getContextInformationValidator()
  {
    return null;
  }

  public String getErrorMessage()
  {
    return null;
  }

  private IRegion getPrefixRegion(ITextViewer viewer, int offset)
      throws BadLocationException
  {
    IDocument doc = viewer.getDocument();
    if (doc == null || offset > doc.getLength()) return null;

    int length = 0;
    char current = 0;
    while (--offset >= 0
        && (current = doc.getChar(offset)) != 0
        && (Character.isJavaIdentifierPart(current) || current == '-' || current == '='))
      length++;

    return new Region(offset + 1, length);
  }

  private String getPrefix(ITextViewer viewer, IRegion region)
      throws BadLocationException
  {
    return viewer.getDocument().get(region.getOffset(), region.getLength());
  }

}
