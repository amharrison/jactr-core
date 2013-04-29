package org.jactr.eclipse.ui.reconciler;

/*
 * default logging
 */
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.core.comp.IProjectCompilationUnit;
import org.jactr.eclipse.core.comp.internal.IMutableCompilationUnit;
import org.jactr.eclipse.core.compiler.CompileRunnable;
import org.jactr.eclipse.core.compiler.IDECompiler;
import org.jactr.eclipse.core.compiler.MarkRunnable;
import org.jactr.eclipse.core.parser.IDEParserFactory;
import org.jactr.eclipse.core.parser.ParseRunnable;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.editor.assist.CodeAssistMarkerParticipant;
import org.jactr.eclipse.ui.editor.markers.FoldingMarkerParticipant;
import org.jactr.eclipse.ui.editor.markers.IPositionMarkerParticipant;
import org.jactr.eclipse.ui.editor.markers.PositionMarker;
import org.jactr.eclipse.ui.preferences.UIPreferences;
import org.jactr.io.antlr3.parser.AbstractModelParser;
import org.jactr.io.parser.IModelParser;

public class ACTRReconciler implements IReconcilingStrategyExtension,
    IReconcilingStrategy
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER         = LogFactory
                                                        .getLog(ACTRReconciler.class);

  private final ACTRModelEditor      _editor;

  private ICompilationUnit           _compilationUnit;

  private ParseJob                   _parseJob;

  private CompilationUnitJob         _compileJob;

  private CompilationUnitJob         _markJob;

  private IJobChangeListener         _jobListener;

  private long                       _maxChangeSize = 10;

  private long                       _changeSize    = 0;

  public ACTRReconciler(ACTRModelEditor editor)
  {
    _editor = editor;

    _jobListener = new IJobChangeListener() {

      public void aboutToRun(IJobChangeEvent event)
      {
      }

      public void awake(IJobChangeEvent event)
      {

      }

      public void done(IJobChangeEvent event)
      {
        boolean clean = event.getResult().getSeverity() == IStatus.OK;
        boolean canceled = event.getResult().getSeverity() == IStatus.CANCEL;

        if (event.getJob() == _parseJob && clean)
          compile();
        else if (!canceled) mark();
      }

      public void running(IJobChangeEvent event)
      {

      }

      public void scheduled(IJobChangeEvent event)
      {

      }

      public void sleeping(IJobChangeEvent event)
      {

      }

    };
  }

  private void clear()
  {
    _compilationUnit = null;
    _parseJob = null;
    _compileJob = null;
    _markJob = null;
  }

  public void setProgressMonitor(IProgressMonitor monitor)
  {

  }

  public void setDocument(IDocument document)
  {
    URI uri = getURI();
    if (uri == null)
    {
      clear();
      return;
    }

    AbstractModelParser modelParser = null;
    IResource resource = getResource();
    String name = uri.getPath();
    if (resource != null)
    {
      modelParser = (AbstractModelParser) IDEParserFactory.getParser(resource);
      name = resource.getName();
    }
    else
      modelParser = (AbstractModelParser) IDEParserFactory.getParser(uri);

    ANTLRDocumentStream documentStream = new ANTLRDocumentStream(document, name);

    /*
     * set the base url so that relative imports will work
     */
    try
    {
      modelParser.setBaseURL(uri.toURL());
    }
    catch (MalformedURLException e)
    {
      LOGGER.error("NewReconciler.setDocument threw MalformedURLException : ",
          e);
    }

    modelParser.setInput(documentStream);
    installPositionMarkers(modelParser, document);

    _compilationUnit = _editor.getCompilationUnit();

    if (_compilationUnit instanceof IProjectCompilationUnit)
      _markJob = new CompilationUnitJob("Marking", new MarkRunnable(
          (IProjectCompilationUnit) _compilationUnit));

    _parseJob = new ParseJob(new ParseRunnable(
        (IMutableCompilationUnit) _compilationUnit, modelParser),
        documentStream);
    _parseJob.addJobChangeListener(_jobListener);

    _compileJob = new CompilationUnitJob("Compiling", new CompileRunnable(
        (IMutableCompilationUnit) _compilationUnit, new IDECompiler()));
    _compileJob.addJobChangeListener(_jobListener);
  }

  private void installPositionMarkers(IModelParser parser, IDocument document)
  {
    PositionMarker marker = new PositionMarker();
    marker.setBase(_editor.getBase());
    marker.setDocument(document);

    IPreferenceStore prefs = UIPlugin.getDefault().getPreferenceStore();

    if (prefs.getBoolean(UIPreferences.ENABLE_ASSIST_PREF))
    {
      IPositionMarkerParticipant participant = new CodeAssistMarkerParticipant();
      marker.addParticipant(participant);
    }

    if (prefs.getBoolean(UIPreferences.ENABLE_FOLDING_PREF))
    {
      IPositionMarkerParticipant participant = new FoldingMarkerParticipant(
          _editor);
      marker.addParticipant(participant);
    }

    parser.addTreeTracker(marker);
  }

  private void parse(boolean force)
  {
    if (!force)
    {
      if (_parseJob.isScheduled() || _parseJob.isRunning())
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Parse scheduled or active, skipping");
        return;
      }
    }
    else
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Canceling..");

      if (_compileJob != null) _compileJob.cancel();
      if (_parseJob != null) _parseJob.cancel();
    }

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Scheduling parse");
    _parseJob.schedule(200);
  }

  private void compile()
  {
    if (_compileJob.isScheduled() || _compileJob.isRunning())
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Compile scheduled or active, skipping");
      return;
    }

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Scheduling compile");
    _compileJob.schedule();
  }

  private void mark()
  {
    if (_markJob != null)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Scheduling mark");
      _markJob.schedule();
    }
  }

  public void initialReconcile()
  {
    parse(true);
  }

  public void reconcile(IRegion partition)
  {
    _changeSize += partition.getLength();

    boolean force = _changeSize >= _maxChangeSize;
    parse(force);

    if (force) _changeSize = 0;
  }

  public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion)
  {
    _changeSize += dirtyRegion.getLength();

    boolean force = _changeSize >= _maxChangeSize;
    parse(force);

    if (force) _changeSize = 0;
  }

  private IResource getResource()
  {
    IEditorInput input = _editor.getEditorInput();
    if (input instanceof IFileEditorInput)
      return ((IFileEditorInput) input).getFile();
    return null;
  }

  private URI getURI()
  {
    IEditorInput input = _editor.getEditorInput();
    if (input instanceof IURIEditorInput)
      return ((IURIEditorInput) input).getURI();
    return null;
  }
}
