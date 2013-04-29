package org.jactr.eclipse.ui.reconciler;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.jactr.eclipse.core.parser.ParseRunnable;

public class ParseJob extends CompilationUnitJob
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory.getLog(ParseJob.class);


  private ANTLRDocumentStream        _documentStream;

  public ParseJob(ParseRunnable runnable, ANTLRDocumentStream stream)
  {
    super("Parsing", runnable);
    _documentStream = stream;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor)
  {
    _documentStream.setProgressMonitor(monitor);
    return super.run(monitor);
  }

}
