package org.jactr.eclipse.ui.editor.assist;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class MergedContentAssistProcessor implements IContentAssistProcessor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(MergedContentAssistProcessor.class);
  
  
  private Collection<IContentAssistProcessor> _processors;
  
  public MergedContentAssistProcessor()
  {
    _processors = new ArrayList<IContentAssistProcessor>();
  }
  
  public void add(IContentAssistProcessor processor)
  {
    _processors.add(processor);
  }

  public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
      int offset)
  {
    Collection<ICompletionProposal> proposals = null;
    ICompletionProposal[] props = null;
    for(IContentAssistProcessor proc : _processors)
      if((props=proc.computeCompletionProposals(viewer, offset))!=null)
      {
        if(proposals==null)
          proposals = new ArrayList<ICompletionProposal>(props.length);
        for(ICompletionProposal prop : props)
          proposals.add(prop);
      }
    
    if(proposals!=null)
      return proposals.toArray(new ICompletionProposal[proposals.size()]);
    
    return null;
  }

  public IContextInformation[] computeContextInformation(ITextViewer viewer,
      int offset)
  {
    Collection<IContextInformation> information = null;
    IContextInformation[] contexts = null;
    for(IContentAssistProcessor proc : _processors)
      if((contexts=proc.computeContextInformation(viewer, offset))!=null)
      {
        if(information==null)
          information = new ArrayList<IContextInformation>(contexts.length);
        for(IContextInformation info : contexts)
          information.add(info);
      }
    if(information!=null)
      return information.toArray(new IContextInformation[information.size()]);
    
    return null;
  }

  public char[] getCompletionProposalAutoActivationCharacters()
  {
    char[] candidate = null;
    for(IContentAssistProcessor proc : _processors)
      if((candidate=proc.getCompletionProposalAutoActivationCharacters())!=null)
        return candidate;
    
    return null;
  }

  public char[] getContextInformationAutoActivationCharacters()
  {
    char[] candidate = null;
    for(IContentAssistProcessor proc : _processors)
      if((candidate=proc.getContextInformationAutoActivationCharacters())!=null)
        return candidate;
    
    return null;
  }

  public IContextInformationValidator getContextInformationValidator()
  {
    IContextInformationValidator validator = null;
    for(IContentAssistProcessor proc : _processors)
      if((validator=proc.getContextInformationValidator())!=null)
        return validator;
    return null;
  }

  public String getErrorMessage()
  {
    String validator = null;
    for(IContentAssistProcessor proc : _processors)
      if((validator=proc.getErrorMessage())!=null)
        return validator;
    return null;
  }

}
