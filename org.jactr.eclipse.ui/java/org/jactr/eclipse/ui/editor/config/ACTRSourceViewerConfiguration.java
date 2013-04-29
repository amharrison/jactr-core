package org.jactr.eclipse.ui.editor.config;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.editor.assist.ACTRContentAssistProposer;
import org.jactr.eclipse.ui.editor.assist.CommentAssistProcessor;
import org.jactr.eclipse.ui.editor.assist.MergedContentAssistProcessor;
import org.jactr.eclipse.ui.editor.hover.ACTRTextHover;
import org.jactr.eclipse.ui.editor.link.ACTRHyperlinkDetector;
import org.jactr.eclipse.ui.preferences.UIPreferences;

public abstract class ACTRSourceViewerConfiguration extends
    TextSourceViewerConfiguration
{

  private static Color DEFAULT_COLOR     = new Color(Display.getCurrent(),
                                             new RGB(0, 0, 0));

  private static Color PUNCTUATION_COLOR = new Color(Display.getCurrent(),
                                             new RGB(64, 64, 64));

  private static Color COMMENT_COLOR;

  private static Color STRING_COLOR;

  private static Color KEYWORD_COLOR;

  public static void setKeywordColor(Color color)
  {
    KEYWORD_COLOR = color;
  }

  public static void setCommentColor(Color color)
  {
    COMMENT_COLOR = color;
  }

  public static void setStringColor(Color color)
  {
    STRING_COLOR = color;
  }

  public static Color getPunctuationColor()
  {
    return PUNCTUATION_COLOR;
  }

  public static Color getDefaultColor()
  {
    return DEFAULT_COLOR;
  }

  public static Color getKeywordColor()
  {
    synchronized (JACTRSourceViewerConfiguration.class)
    {
      if (KEYWORD_COLOR == null)
        KEYWORD_COLOR = new Color(Display.getCurrent(), PreferenceConverter
            .getColor(UIPlugin.getDefault().getPreferenceStore(),
                UIPreferences.KEYWORD_COLOR_PREF));
    }
    return KEYWORD_COLOR;
  }

  public static Color getCommentColor()
  {
    synchronized (JACTRSourceViewerConfiguration.class)
    {
      if (COMMENT_COLOR == null)
        COMMENT_COLOR = new Color(Display.getCurrent(), PreferenceConverter
            .getColor(UIPlugin.getDefault().getPreferenceStore(),
                UIPreferences.COMMENT_COLOR_PREF));
    }
    return COMMENT_COLOR;
  }

  public static Color getStringColor()
  {
    synchronized (JACTRSourceViewerConfiguration.class)
    {
      if (STRING_COLOR == null)
        STRING_COLOR = new Color(Display.getCurrent(), PreferenceConverter
            .getColor(UIPlugin.getDefault().getPreferenceStore(),
                UIPreferences.STRING_COLOR_PREF));
    }
    return STRING_COLOR;
  }

  static protected void clearPrefs()
  {
    /*
     * ideally we should be using a color registry, probably at the plugin level
     * so that these can be disposed.. but for now, just null
     */
    STRING_COLOR = null;
    COMMENT_COLOR = null;
    KEYWORD_COLOR = null;
  }

  protected final ACTRModelEditor _editor;

  public ACTRSourceViewerConfiguration(ACTRModelEditor editor)
  {
    super(UIPlugin.getDefault().getPreferenceStore());
    _editor = editor;

    UIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(
        new IPropertyChangeListener() {

          public void propertyChange(PropertyChangeEvent event)
          {
            String propertyName = event.getProperty();
            if (propertyName.equals(UIPreferences.COMMENT_COLOR_PREF)
                || propertyName.equals(UIPreferences.STRING_COLOR_PREF)
                || propertyName.equals(UIPreferences.KEYWORD_COLOR_PREF))
              clearPrefs();
          }
        });
  }

  @Override
  public IPresentationReconciler getPresentationReconciler(
      ISourceViewer sourceViewer)
  {
    PresentationReconciler reconciler = new PresentationReconciler();

    DefaultDamagerRepairer dr = new DefaultDamagerRepairer(createTokenScanner());
    for (String type : getConfiguredContentTypes(sourceViewer))
    {
      reconciler.setDamager(dr, type);
      reconciler.setRepairer(dr, type);
    }

    return reconciler;
  }

  /**
   * create and return the token scanner that will be used for the presentation
   * reconciliation
   * 
   * @return
   */
  abstract protected ITokenScanner createTokenScanner();

  @Override
  public IContentFormatter getContentFormatter(ISourceViewer sourceViewer)
  {
    IFormattingStrategy strat = createFormattingStrategy();
    if (strat == null) return null;

    MultiPassContentFormatter formatter = new MultiPassContentFormatter(
        getConfiguredDocumentPartitioning(sourceViewer),
        IDocument.DEFAULT_CONTENT_TYPE);

    formatter.setMasterStrategy(strat);
    return formatter;
  }

  /**
   * create and return the formatting strategy for default content type
   * 
   * @return
   */
  abstract protected IFormattingStrategy createFormattingStrategy();

  @Override
  public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
  {
    if (!UIPlugin.getDefault().getPreferenceStore().getBoolean(
        UIPreferences.ENABLE_ASSIST_PREF)) return null;

    ContentAssistant assistant = new ContentAssistant();
    assistant
        .setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

    TemplateCompletionProcessor template = createTemplateCompletionProcessor();
    ACTRContentAssistProposer actr = new ACTRContentAssistProposer(_editor);

    String actrAssistPartition = getContentAssistPartition();
    String commentPartition = getCommentAssistPartition();

    if (template != null)
    {
      /*
       * there is no specialized document partitioning to catch content
       * proposals. this means the same partition must hadnle content and
       * templates s we use a merged content assistant
       */

      if (IDocument.DEFAULT_CONTENT_TYPE.equals(actrAssistPartition))
      {
        MergedContentAssistProcessor merged = new MergedContentAssistProcessor();
        merged.add(template);
        merged.add(actr);
        assistant.setContentAssistProcessor(merged,
            IDocument.DEFAULT_CONTENT_TYPE);
      }
      else
      {
        assistant.setContentAssistProcessor(template,
            IDocument.DEFAULT_CONTENT_TYPE);
        assistant.setContentAssistProcessor(actr, actrAssistPartition);
      }
    }
    else
      assistant.setContentAssistProcessor(actr, actrAssistPartition);

    if (commentPartition != null)
      assistant.setContentAssistProcessor(new CommentAssistProcessor(),
          commentPartition);

    assistant
        .setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
    assistant
        .setInformationControlCreator(getInformationControlCreator(sourceViewer));

    assistant.enablePrefixCompletion(true);

    if (UIPlugin.getDefault().getPluginPreferences().getBoolean(
        UIPreferences.ENABLE_AUTO_ACTIVATE_PREF))
      assistant.enableAutoActivation(true);

    assistant.enableAutoInsert(true);

    assistant.setAutoActivationDelay(250); // slower than the parser..

    return assistant;
  }

  /**
   * return the template completion processor
   * 
   * @return
   */
  abstract protected TemplateCompletionProcessor createTemplateCompletionProcessor();

  /**
   * return the partition within which content proposals will be available
   * 
   * @return
   */
  abstract protected String getContentAssistPartition();

  abstract protected String getCommentAssistPartition();

  @Override
  public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer)
  {
    if (!UIPlugin.getDefault().getPreferenceStore().getBoolean(
        UIPreferences.ENABLE_HYPERLINK_PREF)) return null;

    IHyperlinkDetector[] defaultDetectors = super
        .getHyperlinkDetectors(sourceViewer);
    ArrayList<IHyperlinkDetector> detectors = new ArrayList<IHyperlinkDetector>();

    if (defaultDetectors != null)
      detectors.addAll(Arrays.asList(defaultDetectors));
    else
      detectors.add(new URLHyperlinkDetector());

    detectors.add(new ACTRHyperlinkDetector(_editor));
    return detectors.toArray(new IHyperlinkDetector[detectors.size()]);
  }

  /**
   * so that you always have to press modifier
   */
  @Override
  public int getHyperlinkStateMask(ISourceViewer sourceViewer)
  {
    return SWT.MOD1;
  }

  @Override
  public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType)
  {
    if (!UIPlugin.getDefault().getPreferenceStore().getBoolean(
        UIPreferences.ENABLE_HOVER_PREF)) return null;

    return new ACTRTextHover(_editor);
  }

}