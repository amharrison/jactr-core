package org.jactr.eclipse.core.compiler;

/*
 * default logging
 */
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jactr.io.parser.CanceledException;

public class CancelableTreeNodeStream extends CommonTreeNodeStream
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(CancelableTreeNodeStream.class);
  
  private final IProgressMonitor     _monitor;

  public CancelableTreeNodeStream(CommonTree arg0, IProgressMonitor monitor)
  {
    super(arg0);
    _monitor = monitor;
  }

  
  @Override
  public int LA(int node)
  {
    if (_monitor.isCanceled()) throw new CanceledException();

    return super.LA(node);
  }

  @Override
  public Object LT(int node)
  {
    if (_monitor.isCanceled()) throw new CanceledException();
    return super.LT(node);
  }
}
