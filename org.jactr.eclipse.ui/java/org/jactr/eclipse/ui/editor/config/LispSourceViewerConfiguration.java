package org.jactr.eclipse.ui.editor.config;

import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.editor.partioner.LispPartitions;
import org.jactr.eclipse.ui.editor.preconciler.LispCodeScanner;
import org.jactr.eclipse.ui.editor.template.LispTemplateCompletionProcessor;
import org.jactr.eclipse.ui.preferences.UIPreferences;
import org.jactr.eclipse.ui.reconciler.ACTRReconciler;

/**
 * source viewer for ACT-R support syntax highlighting currently.
 * 
 * @author developer
 */
public class LispSourceViewerConfiguration extends
    ACTRSourceViewerConfiguration
{
  public LispSourceViewerConfiguration(ACTRModelEditor editor)
  {
    super(editor);
  }

  @Override
  protected ITokenScanner createTokenScanner()
  {
    return new LispCodeScanner();
  }

  @Override
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
  {
    return LispPartitions.ALL_PARTITIONS;
  }

  @Override
  protected IFormattingStrategy createFormattingStrategy()
  {
    return null;
  }

  @Override
  protected TemplateCompletionProcessor createTemplateCompletionProcessor()
  {
    LispTemplateCompletionProcessor processor = new LispTemplateCompletionProcessor(
        _editor);

    return processor;
  }

  @Override
  protected String getContentAssistPartition()
  {
    return IDocument.DEFAULT_CONTENT_TYPE;
  }

  @Override
  public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer,
      String contentType)
  {
    return new IAutoEditStrategy[] { new DefaultIndentLineAutoEditStrategy() };
  }

  @Override
  public IReconciler getReconciler(ISourceViewer sourceViewer)
  {
    if (!UIPlugin.getDefault().getPreferenceStore().getBoolean(
        UIPreferences.ENABLE_RECONCILER_PREF))
      return super.getReconciler(sourceViewer);

    // IReconcilingStrategy strat = new ACTRReconcilingStrategy(_editor);
    //
    // Reconciler reconciler = new Reconciler();
    //
    // reconciler.setReconcilingStrategy(strat, IDocument.DEFAULT_CONTENT_TYPE);
    // reconciler.setIsIncrementalReconciler(false);
    // reconciler.install(sourceViewer);
    // reconciler.setDelay(200);

    IReconcilingStrategy strat = new ACTRReconciler(_editor);

    SpellingService spellingService = EditorsUI.getSpellingService();
    IReconcilingStrategy spellingStrategy = null;
    if (spellingService.getActiveSpellingEngineDescriptor(fPreferenceStore) != null)
      spellingStrategy = new SpellingReconcileStrategy(sourceViewer,
          spellingService);

    Reconciler reconciler = new Reconciler();
    reconciler.setReconcilingStrategy(strat, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setReconcilingStrategy(strat, LispPartitions.ID);

    if (spellingStrategy != null)
      reconciler.setReconcilingStrategy(spellingStrategy,
          LispPartitions.COMMENT);

    reconciler.setIsIncrementalReconciler(false);
    reconciler.install(sourceViewer);
    reconciler.setDelay(350);

    return reconciler;
  }

  @Override
  protected String getCommentAssistPartition()
  {
    return LispPartitions.COMMENT;
  }
}