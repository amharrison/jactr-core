package org.jactr.eclipse.ui.editor.config;

import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;
import org.eclipse.ui.texteditor.spelling.SpellingService;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.editor.formatting.JACTRAutoCommentAndString;
import org.jactr.eclipse.ui.editor.formatting.JACTRCloseTagEdityStrategy;
import org.jactr.eclipse.ui.editor.formatting.JACTRFormattingStrategy;
import org.jactr.eclipse.ui.editor.partioner.JACTRPartitions;
import org.jactr.eclipse.ui.editor.preconciler.JACTRCodeScanner;
import org.jactr.eclipse.ui.editor.template.JACTRTemplateCompletionProcessor;
import org.jactr.eclipse.ui.preferences.UIPreferences;
import org.jactr.eclipse.ui.reconciler.ACTRReconciler;

/**
 * source viewer for ACT-R support syntax highlighting currently.
 * 
 * @author developer
 */
public class JACTRSourceViewerConfiguration extends
    ACTRSourceViewerConfiguration
{
  public JACTRSourceViewerConfiguration(ACTRModelEditor editor)
  {
    super(editor);
  }

  @Override
  protected ITokenScanner createTokenScanner()
  {
    return new JACTRCodeScanner();
  }

  @Override
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
  {
    return JACTRPartitions.ALL_PARTITIONS;
  }

  @Override
  protected IFormattingStrategy createFormattingStrategy()
  {
    boolean useSpaces = _editor.getInternalPreferenceStore().getBoolean(
        AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);
    int tabWidth = _editor.getInternalPreferenceStore().getInt(
        AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
    return new JACTRFormattingStrategy(useSpaces, tabWidth);
  }

  @Override
  protected TemplateCompletionProcessor createTemplateCompletionProcessor()
  {
    boolean useSpaces = _editor.getInternalPreferenceStore().getBoolean(
        AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);
    int tabWidth = _editor.getInternalPreferenceStore().getInt(
        AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);

    JACTRTemplateCompletionProcessor processor = new JACTRTemplateCompletionProcessor(
        _editor, useSpaces, tabWidth);

    return processor;
  }

  @Override
  protected String getContentAssistPartition()
  {
    return JACTRPartitions.IDENTIFIER;
  }

  @Override
  public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer,
      String contentType)
  {
    IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
    return new IAutoEditStrategy[] {
        new DefaultIndentLineAutoEditStrategy(),
        new JACTRAutoCommentAndString(
            store.getBoolean(UIPreferences.ENABLE_STRING_COMPLETION),
            store.getBoolean(UIPreferences.ENABLE_COMMENT_COMPLETION),
            store.getBoolean(UIPreferences.ENABLE_CARRET_COMPLETION)),
        new JACTRCloseTagEdityStrategy(
            store.getBoolean(UIPreferences.ENABLE_CLOSE_COMPLETION)) };
  }

  @Override
  public IReconciler getReconciler(ISourceViewer sourceViewer)
  {
    if (!UIPlugin.getDefault().getPreferenceStore()
        .getBoolean(UIPreferences.ENABLE_RECONCILER_PREF))
      return super.getReconciler(sourceViewer);

    // IReconcilingStrategy strat = new ACTRReconcilingStrategy(_editor);
    IReconcilingStrategy strat = new ACTRReconciler(_editor);

    SpellingService spellingService = EditorsUI.getSpellingService();
    IReconcilingStrategy spellingStrategy = null;
    if (spellingService.getActiveSpellingEngineDescriptor(fPreferenceStore) != null)
      spellingStrategy = new SpellingReconcileStrategy(sourceViewer,
          spellingService);

    Reconciler reconciler = new Reconciler();
    reconciler.setReconcilingStrategy(strat, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setReconcilingStrategy(strat, JACTRPartitions.IDENTIFIER);

    if (spellingStrategy != null)
      reconciler.setReconcilingStrategy(spellingStrategy,
          JACTRPartitions.COMMENT);

    reconciler.setIsIncrementalReconciler(false);
    reconciler.install(sourceViewer);
    reconciler.setDelay(350);

    return reconciler;
  }

  @Override
  protected String getCommentAssistPartition()
  {
    return JACTRPartitions.COMMENT;
  }
}