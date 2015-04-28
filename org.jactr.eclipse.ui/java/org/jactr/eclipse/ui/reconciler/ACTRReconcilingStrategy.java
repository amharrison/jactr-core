/*
 * Created on Apr 18, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.ui.reconciler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.core.concurrent.QueueingJob;
import org.jactr.eclipse.core.parser.ProjectSensitiveParserImportDelegate;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.editor.assist.CodeAssistMarkerParticipant;
import org.jactr.eclipse.ui.editor.markers.FoldingMarkerParticipant;
import org.jactr.eclipse.ui.editor.markers.IPositionMarkerParticipant;
import org.jactr.eclipse.ui.editor.markers.PositionMarker;
import org.jactr.eclipse.ui.preferences.UIPreferences;
import org.jactr.io.antlr3.parser.AbstractModelParser;
import org.jactr.io.parser.CanceledException;
import org.jactr.io.parser.IModelParser;
import org.jactr.io.parser.ModelParserFactory;

public class ACTRReconcilingStrategy implements IReconcilingStrategy,
    IReconcilingStrategyExtension
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER     = LogFactory
                                                    .getLog(ACTRReconcilingStrategy.class);

  private IModelParser               _modelParser;

  private ICompilationUnit           _workingCopy;

  private final ACTRModelEditor      _editor;

  private IResource                  _resource;


  private IDocument                  _document;

  private ANTLRDocumentStream        _documentStream;

  private boolean                    _isInitial = true;

  // private boolean _shouldCompile = true;

  private ParseJob                   _parseJob;
  
  private IProgressMonitor           _nullProgress = new NullProgressMonitor();

  // private CompileJob _compileJob;

  public ACTRReconcilingStrategy(ACTRModelEditor editor)
  {
    _editor = editor;
  }

  public void initialReconcile()
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Initial reconcile");
    if (_parseJob != null) _parseJob.queue();
  }

  public void setProgressMonitor(IProgressMonitor monitor)
  {

  }

  public void reconcile(IRegion partition)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("reconcile " + partition);
    if (_parseJob != null) _parseJob.queue();
  }

  public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("reconcile dirty " + dirtyRegion.getOffset() + ":"
          + dirtyRegion.getLength() + "[" + dirtyRegion.getType() + "] of "
          + subRegion.getOffset() + ":" + subRegion.getLength());

    if (_parseJob != null) _parseJob.queue();
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

    _modelParser.addTreeTracker(marker);
  }

  public void setDocument(IDocument document)
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Setting document " + _document);

    /*
     * we dont reuse reconcilers
     */
    if (_resource != null) return;

    _document = document;
    _resource = getResource();

    if (_resource != null)
      try
      {
        _modelParser = ModelParserFactory.instantiateParser(_resource
            .getFileExtension());
        
        ProjectSensitiveParserImportDelegate delegate = new ProjectSensitiveParserImportDelegate();
        delegate.setProject(_resource.getProject());

        _modelParser.setImportDelegate(delegate);

        /*
         * set the base url so that relative imports will work
         */
        ((AbstractModelParser) _modelParser).setBaseURL(_resource
            .getLocationURI().toURL());

        _documentStream = new ANTLRDocumentStream(_document, null);

        ((AbstractModelParser) _modelParser).setInput(_documentStream);

        installPositionMarkers(_modelParser, document);

        // _builder = new ACTRModelBuilder();
      }
      catch (Exception e)
      {
        LOGGER.error("Could not set up parser ", e);
      }

    if (_modelParser != null) //      _workingCopy = CompilationUnitManager.getWorkingCopy(_resource);
    _parseJob = new ParseJob("Incremental Compile");
  }

  private IResource getResource()
  {
    IEditorInput input = _editor.getEditorInput();
    if (input instanceof IFileEditorInput)
      return ((IFileEditorInput) input).getFile();
    return null;
  }

  private boolean parse(IProgressMonitor monitor)
  {
    if (monitor != null) _documentStream.setProgressMonitor(monitor);
    
    /*
     * we only need to compile if it is initial, or there were previously errors
     * and the parse was clean. If the parse was dirty - who cares.
     */
    try
    {
      // boolean hadErrors = _workingCopy.getCompileErrors().size() != 0;
      // boolean cleanParse = _builder.parse(_workingCopy, _modelParser,
      // _nullProgress);
      // boolean shouldCompile = _isInitial || hadErrors && cleanParse;

      return false;
    }
    catch (CanceledException pce)
    {
      return false;
    }
    finally
    {
      _isInitial = false;

      _modelParser.reset();
    }
  }

  private void compile(boolean shouldCompile)
  {
    // if (shouldCompile) _builder.compile(_workingCopy, _nullProgress);
    //
    // _builder.updateMarkers(_workingCopy, _nullProgress);
  }

  private void dispose()
  {
    IDocument doc = _editor.getDocumentProvider().getDocument(
        _editor.getEditorInput());
    for (IPositionUpdater updater : doc.getPositionUpdaters())
      doc.removePositionUpdater(updater);

    _workingCopy = null;
    // _builder = null;
    _modelParser = null;
    // _editor.close(false);
    _resource = null;

    // System.gc();
  }

  // private class CompilationUnitRule implements ISchedulingRule
  // {
  //
  // private ACTRCompilationUnit _compilationUnit;
  //
  // public CompilationUnitRule(ACTRCompilationUnit unit)
  // {
  // _compilationUnit = unit;
  // }
  //
  // public boolean contains(ISchedulingRule rule)
  // {
  // return false;
  // }
  //
  // public boolean isConflicting(ISchedulingRule rule)
  // {
  // if (rule instanceof CompilationUnitRule)
  // return _compilationUnit == ((CompilationUnitRule) rule)._compilationUnit;
  // return false;
  // }
  //
  // }

  /**
   * the parse job uses an adaptive scheduling time based on the average of
   * reconciliation time
   */
  private class ParseJob extends QueueingJob
  {

    private long _reconcileAverage = 400;

    private long _reconcileCount   = 0;

    private long _sleepDuration    = 400;

    public ParseJob(String name)
    {
      super(name);
      setPriority(Job.LONG);
    }

    synchronized public void queue()
    {
      if (cancel())
      {

        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Canceling scheduling and resetting for "
              + _sleepDuration + "ms ("
              + (System.currentTimeMillis() + _sleepDuration) + ")");
        schedule(_sleepDuration);
      }
      else if (LOGGER.isDebugEnabled())
        LOGGER.debug("Letting it play through..");
    }

    @Override
    synchronized protected IStatus run(IProgressMonitor monitor)
    {
      try
      {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Starting actual reconcile");

        long start = System.currentTimeMillis();

        boolean shouldCompile = parse(monitor);

        compile(shouldCompile);

        _reconcileAverage = (_reconcileAverage * _reconcileCount
            + System.currentTimeMillis() - start)
            / ++_reconcileCount;

        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Average reconciliation time " + _reconcileAverage);

        return Status.OK_STATUS;
      }
      catch (Exception e)
      {
        dispose();
        _editor.close(false);

        return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
            "Parsing snafu on " + _resource.getName() + " forcing closed", e);
      }
    }
  }
}
