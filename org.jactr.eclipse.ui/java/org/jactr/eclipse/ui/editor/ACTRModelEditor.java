/*
 * Created on Mar 27, 2004 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jactr.eclipse.ui.editor;

import java.net.MalformedURLException;
import java.net.URL;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistantExtension2;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jactr.eclipse.core.comp.CompilationUnitManager;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.config.ACTRSourceViewerConfiguration;
import org.jactr.eclipse.ui.editor.highlighter.HighlightAnnotations;
import org.jactr.eclipse.ui.editor.highlighter.ReferenceHighlighter;
import org.jactr.eclipse.ui.editor.markers.ASTPosition;
import org.jactr.eclipse.ui.editor.markers.PositionMarker;
import org.jactr.eclipse.ui.messages.JACTRMessages;
import org.jactr.eclipse.ui.outline.ACTRContentOutline;
import org.jactr.eclipse.ui.preferences.UIPreferences;

/**
 * @author harrison To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class ACTRModelEditor extends AbstractDecoratedTextEditor
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER               = LogFactory
                                                              .getLog(ACTRModelEditor.class);

  static private final String        TEMPLATE_PROPOSALS   = "org.jactr.eclipse.ui.template.action";

  static private final String        FORMAT_PREFIX        = "org.jactr.eclipse.ui.format";

  static public final String         ACTR_CONTEXT_MENU    = "org.jactr.eclipse.ui.editor.context";

  static public final String         ACTR_CONTEXT_GROUP   = "jactr";

  ACTRContentOutline                 _outliner;

  ACTRSourceViewerConfiguration      _sourceViewer;

  ICompilationUnit                   _compilationUnit;

  private ProjectionSupport          _projectionSupport;

  private ProjectionAnnotationModel  _projectionAnnotationModel;

  private boolean                    _formatOnSave        = false;

  private URL                        _baseURL;

  private boolean                    _highlightReferences = true;

  private ReferenceHighlighter       _highlighter;

  private IResourceChangeListener    _resourceListener    = new IResourceChangeListener() {

                                                            public void resourceChanged(
                                                                IResourceChangeEvent event)
                                                            {
                                                              IResource resource = getResource();
                                                              if (resource == null)
                                                                return;

                                                              if (resource
                                                                  .equals(event
                                                                      .getResource()))
                                                                if (event
                                                                    .getType() == IResourceChangeEvent.PRE_CLOSE
                                                                    || event
                                                                        .getType() == IResourceChangeEvent.PRE_DELETE)

                                                                  close(event
                                                                      .getType() == IResourceChangeEvent.PRE_CLOSE);
                                                            }
                                                          };

  static public ACTRModelEditor getActiveEditor()
  {
    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
        .getActivePage();
    IEditorPart editor = page.getActiveEditor();

    if (editor instanceof ACTRModelEditor) return (ACTRModelEditor) editor;

    for (IEditorReference ref : page.getEditorReferences())
    {
      editor = ref.getEditor(false);
      if (editor instanceof ACTRModelEditor) return (ACTRModelEditor) editor;
    }
    return null;
  }

  /**
   * 
   */
  public ACTRModelEditor()
  {
    super();
    _sourceViewer = createSourceViewerConfiguration();
    setSourceViewerConfiguration(_sourceViewer);
    _outliner = new ACTRContentOutline(this, true);

    _formatOnSave = UIPlugin.getDefault().getPluginPreferences().getBoolean(
        UIPreferences.ENABLE_FORMAT_PREF);

    setEditorContextMenuId(ACTR_CONTEXT_MENU);

  }

  public IPreferenceStore getInternalPreferenceStore()
  {
    return super.getPreferenceStore();
  }

  abstract protected ACTRSourceViewerConfiguration createSourceViewerConfiguration();

  @Override
  protected void createActions()
  {
    super.createActions();

    IAction action = new TextOperationAction(JACTRMessages.getResourceBundle(),
        TEMPLATE_PROPOSALS + ".", //$NON-NLS-1$ //$NON-NLS-2$
        this, ISourceViewer.CONTENTASSIST_PROPOSALS);
    action
        .setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
    setAction(TEMPLATE_PROPOSALS, action);
    markAsStateDependentAction(TEMPLATE_PROPOSALS, true);

    /*
     * format operation
     */
    action = new TextOperationAction(JACTRMessages.getResourceBundle(),
        FORMAT_PREFIX + ".", //$NON-NLS-1$ //$NON-NLS-2$
        this, ISourceViewer.FORMAT);
    action.setActionDefinitionId(FORMAT_PREFIX);
    setAction(FORMAT_PREFIX, action);
    // markAsStateDependentAction(FORMAT_PREFIX, true);

    /*
     * replace paste with retargeted.. Im not pleased with this impl as it
     * reformats the entire document on paste. I want to just format insertion
     */
    // action = new FormattingTextOperationAction(JACTRMessages
    //        .getResourceBundle(), "Editor.Paste.", this, ITextOperationTarget.PASTE); //$NON-NLS-1$
    // action.setActionDefinitionId(IWorkbenchActionDefinitionIds.PASTE);
    // setAction(ITextEditorActionConstants.PASTE, action);
  }

  @Override
  protected void handleCursorPositionChanged()
  {
    super.handleCursorPositionChanged();
    if (_highlightReferences)
    {
      if (_highlighter == null) _highlighter = new ReferenceHighlighter(this);

      ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();

      int caretPosition = viewer.getTextWidget().getCaretOffset();
      caretPosition = viewer.widgetOffset2ModelOffset(caretPosition);

      _highlighter.highlight(caretPosition);
    }
  }

  public int getCursorOffset()
  {
    ISourceViewer sv = getSourceViewer();
    StyledText styledText = sv.getTextWidget();
    int caret = widgetOffset2ModelOffset(sv, styledText.getCaretOffset());
    return caret;
  }

  @Override
  protected void editorContextMenuAboutToShow(IMenuManager menu)
  {
    super.editorContextMenuAboutToShow(menu);

    menu.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, new Separator(
        ACTR_CONTEXT_GROUP));
  }

  /**
   * set the input and, if necessary, compile it into an ACTRCompilationUnit
   */
  @Override
  protected void doSetInput(IEditorInput input) throws CoreException
  {
    super.doSetInput(input);
    if (input instanceof IURIEditorInput)
    {
      try
      {
        _baseURL = ((IURIEditorInput) input).getURI().toURL();
      }
      catch (MalformedURLException e)
      {
        // TODO Auto-generated catch block
        LOGGER.error(
            "ACTRModelEditor.doSetInput threw MalformedURLException : ", e);
      }

      if (input instanceof IFileEditorInput)
      {
        IResource file = ((IFileEditorInput) input).getFile();
        _compilationUnit = CompilationUnitManager.acquire(file);

        ResourcesPlugin.getWorkspace().addResourceChangeListener(
            _resourceListener,
            IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE);
      }
      else if (input instanceof FileStoreEditorInput)
        try
        {
          _baseURL = FileLocator.toFileURL(_baseURL);

          IFileStore fileStore = EFS.getLocalFileSystem().getStore(
              new Path(_baseURL.getPath()));

          _compilationUnit = CompilationUnitManager.acquire(fileStore);
        }
        catch (Exception e)
        {
          UIPlugin.log("Could not acquire filestore location for " + _baseURL,
              e);
        }
    }
  }

  /**
   * return the resource being edited or null if the editor doesn't point to an
   * IResource.
   * 
   * @return
   */
  public IResource getResource()
  {
    IEditorInput input = getEditorInput();
    if (input instanceof IFileEditorInput)
      return ((IFileEditorInput) input).getFile();
    return null;
  }

  public ICompilationUnit getCompilationUnit()
  {
    return _compilationUnit;
  }

  /**
   * returns the production that is closest to the current cursor
   * 
   * @return
   */
  public CommonTree getNearestAST(int type)
  {
    IDocument document = getDocumentProvider().getDocument(getEditorInput());

    TextSelection selection = (TextSelection) getSelectionProvider()
        .getSelection();
    int cursor = selection.getOffset();
    try
    {
      int distance = Integer.MAX_VALUE;
      CommonTree best = null;
      Position[] positions = document.getPositions(PositionMarker.POSITION_ID);
      for (Position position : positions)
        if (position != null && position instanceof ASTPosition)
        {
          ASTPosition astPos = (ASTPosition) position;
          CommonTree node = astPos.getNode();
          if (node.getType() != type) continue;

          if (astPos.contains(cursor)) return node;

          int offsetDistance = Math.abs(astPos.getOffset() - cursor);
          int lenDistance = Math.abs(astPos.getOffset() + astPos.getLength()
              - cursor);

          if (offsetDistance < distance)
          {
            distance = offsetDistance;
            best = node;
          }
          else if (lenDistance < distance)
          {
            distance = lenDistance;
            best = node;
          }
        }

      return best;
    }
    catch (BadPositionCategoryException e)
    {
      // TODO Auto-generated catch block
      LOGGER
          .error(
              "ACTRModelEditor.getNearestProduction threw BadPositionCategoryException : ",
              e);
    }

    return null;
  }

  public URL getBase()
  {
    return _baseURL;
  }

  @Override
  public void dispose()
  {
    if (_compilationUnit != null)
      CompilationUnitManager.release(_compilationUnit);

    if (_outliner != null)
    {
      _outliner.dispose();
      _outliner = null;
    }

    /*
     * remove listener
     */
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(
        _resourceListener);

    super.dispose();
  }

  public ProjectionAnnotationModel getProjectionAnnotationModel()
  {
    return _projectionAnnotationModel;
  }

  /**
   * open up access for the highlighter
   * 
   * @return
   */
  public IAnnotationModel getAnnotationModel()
  {
    return getSourceViewer().getAnnotationModel();
  }

  @Override
  public Object getAdapter(Class required)
  {
    if (IContentOutlinePage.class.equals(required))
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Returning outliner " + _outliner);
      return _outliner;
    }
    else if (ACTRModelEditor.class.equals(required)) return this;

    return super.getAdapter(required);
  }

  /**
   * modification to enable folding in the editor
   * 
   * @see http
   *      ://www.eclipse.org/articles/Article-Folding-in-Eclipse-Text-Editors
   *      /folding.html
   */
  @Override
  public void createPartControl(Composite parent)
  {
    super.createPartControl(parent);
    ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();

    _projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(),
        getSharedColors());
    _projectionSupport
        .addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error");
    _projectionSupport
        .addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning");
    _projectionSupport
        .addSummarizableAnnotationType(HighlightAnnotations.VARIABLE_ID);
    _projectionSupport
        .addSummarizableAnnotationType(HighlightAnnotations.CHUNK_ID);
    _projectionSupport
        .addSummarizableAnnotationType(HighlightAnnotations.CHUNK_TYPE_ID);
    _projectionSupport.install();

    // turn projection mode on
    viewer.doOperation(ProjectionViewer.TOGGLE);

    _projectionAnnotationModel = viewer.getProjectionAnnotationModel();
  }

  /**
   * modification to enable folding in the editor
   * 
   * @see http
   *      ://www.eclipse.org/articles/Article-Folding-in-Eclipse-Text-Editors
   *      /folding.html
   */
  @Override
  protected ISourceViewer createSourceViewer(Composite parent,
      IVerticalRuler ruler, int styles)
  {
    /*
     * using some crazy mojo to format all edits..
     */
    ISourceViewer viewer = new ProjectionViewer(parent, ruler,
        getOverviewRuler(), isOverviewRulerVisible(), styles) {

      private volatile Point      _spoofSelection   = null;

      private volatile boolean    _isFormatting     = false;

      private volatile boolean    _isProposing      = false;

      private volatile boolean    _wasProposed      = false;

      private ICompletionListener fProposalListener = new ICompletionListener() {

                                                      public void assistSessionEnded(
                                                          ContentAssistEvent event)
                                                      {
                                                        _isProposing = false;
                                                        _wasProposed = true;
                                                        if (LOGGER
                                                            .isDebugEnabled())
                                                          LOGGER.debug("ended");
                                                      }

                                                      public void assistSessionStarted(
                                                          ContentAssistEvent event)
                                                      {
                                                        _wasProposed = false;
                                                        _isProposing = true;
                                                        if (LOGGER
                                                            .isDebugEnabled())
                                                          LOGGER
                                                              .debug("started");
                                                      }

                                                      public void selectionChanged(
                                                          ICompletionProposal proposal,
                                                          boolean smartToggle)
                                                      {
                                                        if (LOGGER
                                                            .isDebugEnabled())
                                                          LOGGER
                                                              .debug("changed");
                                                      }

                                                    };

      private ITextListener       fTextListener     = new ITextListener() {

                                                      public void textChanged(
                                                          TextEvent event)
                                                      {
                                                        if (!canDoOperation(ISourceViewer.FORMAT))
                                                          return;
                                                        if (_isFormatting)
                                                          return;
                                                        if (_isProposing)
                                                          return;

                                                        if (_wasProposed)
                                                          return;

                                                        DocumentEvent dEvent = event
                                                            .getDocumentEvent();
                                                        // actual event
                                                        // if
                                                        // (event.getViewerRedrawState()
                                                        // &&
                                                        // dEvent == null)
                                                        // return;
                                                        // if(!event.getViewerRedrawState()
                                                        // &&
                                                        // dEvent==null) return;
                                                        if (dEvent == null)
                                                          return;
                                                        // delete, ignore
                                                        if (event.getText()
                                                            .length() == 0)
                                                          return;

                                                        String replaced = event
                                                            .getReplacedText();
                                                        String text = event
                                                            .getText();

                                                        if (text
                                                            .equals(replaced))
                                                          return;

                                                        // if
                                                        // (replaced.trim().length()
                                                        // == 0
                                                        // &&
                                                        // text.trim().length()
                                                        // ==
                                                        // 0)
                                                        // return;

                                                        /*
                                                         * let's format..
                                                         */
                                                        final Point fPoint = new Point(
                                                            event.getOffset(),
                                                            event.getLength());
                                                        Display.getDefault()
                                                            .asyncExec(
                                                                new Runnable() {

                                                                  public void run()
                                                                  {
                                                                    _spoofSelection = fPoint;
                                                                    ITextOperationTarget target = getSourceViewer()
                                                                        .getTextOperationTarget();
                                                                    if (target == null)
                                                                      return;
                                                                    if (target
                                                                        .canDoOperation(ISourceViewer.FORMAT))
                                                                      target
                                                                          .doOperation(ISourceViewer.FORMAT);
                                                                    _spoofSelection = null;
                                                                  }

                                                                });
                                                      }
                                                    };

      @Override
      protected void inputChanged(Object newInput, Object oldInput)
      {
        removeTextListener(fTextListener);
        if (fContentAssistant instanceof IContentAssistantExtension2)
          ((IContentAssistantExtension2) fContentAssistant)
              .removeCompletionListener(fProposalListener);

        super.inputChanged(newInput, oldInput);

        addTextListener(fTextListener);
        if (fContentAssistant instanceof IContentAssistantExtension2)
          ((IContentAssistantExtension2) fContentAssistant)
              .addCompletionListener(fProposalListener);
      }

      @Override
      protected Point rememberSelection()
      {
        if (_spoofSelection != null) return _spoofSelection;
        return super.rememberSelection();
      }

      @Override
      public void doOperation(int operation)
      {
        _isFormatting = operation == ISourceViewer.FORMAT;
        super.doOperation(operation);
        _isFormatting = false;
      }
    };

    // ensure decoration support has been created and configured.
    getSourceViewerDecorationSupport(viewer);
    return viewer;
  }

  @Override
  public void doSave(IProgressMonitor progressMonitor)
  {
    /**
     * format first..
     */
    ITextOperationTarget tot = getSourceViewer().getTextOperationTarget();

    if (tot.canDoOperation(ISourceViewer.FORMAT) && _formatOnSave)
      tot.doOperation(ISourceViewer.FORMAT);

    super.doSave(progressMonitor);
  }

}
