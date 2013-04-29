package org.jactr.eclipse.production.view;

/*
 * default logging
 */
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jactr.tools.analysis.production.ProductionAnalyzer;

public class AnalyzerJob extends Job
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AnalyzerJob.class);

  private CommonTree                 _modelDescriptor;

  private ProductionAnalyzer         _analyzer;

  private ProductionSequenceView     _view;

  public AnalyzerJob(ProductionSequenceView view, CommonTree modelDescriptor)
  {
    super("Production Sequence Analyzer");
    _modelDescriptor = modelDescriptor;
    _analyzer = new ProductionAnalyzer();
    _view = view;
  }

  @Override
  protected IStatus run(IProgressMonitor monitor)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Analzing " + _modelDescriptor);

    _analyzer.setModelDescriptor(_modelDescriptor);

    if (monitor.isCanceled()) return Status.CANCEL_STATUS;

    _view.setProductionRelationships(_analyzer.getAnalyzer()
        .getAllRelationships());

    return Status.OK_STATUS;
  }

}
