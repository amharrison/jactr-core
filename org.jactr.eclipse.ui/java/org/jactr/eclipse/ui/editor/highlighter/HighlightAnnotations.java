package org.jactr.eclipse.ui.editor.highlighter;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.source.Annotation;

public class HighlightAnnotations extends Annotation
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(HighlightAnnotations.class);
  
  static public final String         VARIABLE_ID   = "org.jactr.eclipse.ui.editor.highlighter.variable";

  static public final String         CHUNK_ID      = "org.jactr.eclipse.ui.editor.highlighter.chunk";

  static public final String         CHUNK_TYPE_ID = "org.jactr.eclipse.ui.editor.highlighter.chunktype";

  
}
