package org.jactr.eclipse.ui.editor.template;

/*
 * default logging
 */
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.editor.markers.ASTPosition;
import org.jactr.eclipse.ui.editor.markers.PositionMarker;
import org.jactr.io.antlr3.builder.JACTRBuilder;

public abstract class ACTRTemplateCompletionProcessor extends
    TemplateCompletionProcessor
{
  /**
   * Logger definition
   */
  static private final transient Log        LOGGER           = LogFactory
                                                                 .getLog(ACTRTemplateCompletionProcessor.class);

  static public final String                RHS              = "rhs";

  static public final String                LHS              = "lhs";

  static public final String                SLOT             = "slot";

  static public final String                CONDITIONAL_SLOT = "conditionals";

  static public final String                PROCEDURAL       = "procedural";

  static public final String                DECLARATIVE      = "declarative";

  static public final String                PARAMETERS       = "parameters";

  static public final String                PARAMETER        = "parameter";

  static public final String                MODULE           = "module";

  static public final String                EXTENSION        = "extension";

  static public final String                MODEL_CONTENTS   = "modelContents";

  private Map<Integer, TemplateContextType> _contexts;

  private ACTRModelEditor                   _editor;

  private Map<String, Integer>              _relevances;

  private int                               _maxRelevance    = 0;

  private Set<Integer>                      _suspendTypes;

  protected ACTRTemplateCompletionProcessor(ACTRModelEditor editor,
      String idPrefix)
  {
    _editor = editor;
    _suspendTypes = new TreeSet<Integer>();
    _contexts = new TreeMap<Integer, TemplateContextType>();
    _relevances = new TreeMap<String, Integer>();

    addContextType(JACTRBuilder.DECLARATIVE_MEMORY, idPrefix, DECLARATIVE);
    addContextType(JACTRBuilder.PROCEDURAL_MEMORY, idPrefix, PROCEDURAL);
    addContextType(JACTRBuilder.ACTIONS, idPrefix, RHS);
    addContextType(JACTRBuilder.CONDITIONS, idPrefix, LHS);
    addContextType(JACTRBuilder.CHUNK_TYPE, idPrefix, SLOT);
    addContextType(JACTRBuilder.MATCH_CONDITION, idPrefix, CONDITIONAL_SLOT);
    addContextType(JACTRBuilder.QUERY_CONDITION, idPrefix, CONDITIONAL_SLOT);
    addContextType(JACTRBuilder.ADD_ACTION, idPrefix, CONDITIONAL_SLOT);
    addContextType(JACTRBuilder.PROXY_CONDITION, idPrefix, SLOT);
    addContextType(JACTRBuilder.REMOVE_ACTION, idPrefix, SLOT);
    addContextType(JACTRBuilder.MODIFY_ACTION, idPrefix, SLOT);
    addContextType(JACTRBuilder.PROXY_ACTION, idPrefix, SLOT);
    addContextType(JACTRBuilder.PRODUCTION, idPrefix, PARAMETERS);
    addContextType(JACTRBuilder.CHUNK, idPrefix, SLOT);
    // for the moment this is requires a simple implementation.. we can't have
    // two different contexts
    // addContextType(JACTRBuilder.CHUNK, idPrefix, PARAMETERS);
    addContextType(JACTRBuilder.BUFFER, idPrefix, PARAMETERS);
    addContextType(JACTRBuilder.PARAMETERS, idPrefix, PARAMETER);
    addContextType(JACTRBuilder.MODEL, idPrefix, MODEL_CONTENTS);

    addContextType(JACTRBuilder.MODULE, idPrefix, PARAMETERS);
    addContextType(JACTRBuilder.MODULES, idPrefix, MODULE);
    addContextType(JACTRBuilder.EXTENSION, idPrefix, PARAMETERS);
    addContextType(JACTRBuilder.EXTENSIONS, idPrefix, EXTENSION);

    addSuspendType(JACTRBuilder.SCRIPTABLE_ACTION);
    addSuspendType(JACTRBuilder.SCRIPTABLE_CONDITION);
  }

  protected void addSuspendType(int jactrBuilderType)
  {
    _suspendTypes.add(jactrBuilderType);
  }

  @Override
  protected String extractPrefix(ITextViewer viewer, int offset)
  {
    int i = offset;
    IDocument document = viewer.getDocument();
    if (i > document.getLength()) return ""; //$NON-NLS-1$

    try
    {
      while (i > 0)
      {
        char ch = document.getChar(i - 1);
        if (!(Character.isJavaIdentifierPart(ch) || ch == '-' || ch == '-'
            || ch == '+' || ch == '=' || ch == '!')) break;
        i--;
      }

      return document.get(i, offset - i);
    }
    catch (BadLocationException e)
    {
      return ""; //$NON-NLS-1$
    }
  }

  protected void addContextType(int nodeType, String prefix, String suffix)
  {
    String id = String.format("%1$s.%2$s", prefix, suffix);

    ContributionContextTypeRegistry registry = UIPlugin.getDefault()
        .getContextTypeRegistry();

    registry.addContextType(id);
    _contexts.put(nodeType, registry.getContextType(id));
  }

  @Override
  protected TemplateContextType getContextType(ITextViewer viewer,
      IRegion region)
  {
    /*
     * this assumes perfect position tracking and parsing with a properly closed
     * position. we don't always get that. need to deal with 1) completely
     * invalid position (seek back to find valid position) 2)??
     */
    ASTPosition position = PositionMarker.getPosition(viewer.getDocument(),
        _editor.getBase(), region.getOffset());
    TemplateContextType contextType = null;
    while (position != null && contextType == null)
    {
      int type = position.getNode().getType();

      if (_suspendTypes.contains(type)) return null;

      contextType = _contexts.get(type);
      position = position.getParent();
    }

    return contextType;
  }

  @Override
  protected Template[] getTemplates(String contextTypeId)
  {
    return UIPlugin.getDefault().getTemplateStore().getTemplates();
  }

  @Override
  protected int getRelevance(Template template, String prefix)
  {
    if (_relevances.containsKey(template.getName()))
      return _relevances.get(template.getName());

    // "" prefix is auto-triggered content assist..
    if (prefix.length() > 0) return super.getRelevance(template, prefix);

    return 0;
  }

  @Override
  protected ICompletionProposal createProposal(Template template,
      TemplateContext context, IRegion region, int relevance)
  {
    return new TemplateProposal(template, context, region, getImage(template),
        relevance) {
      @Override
      public void apply(ITextViewer viewer, char trigger, int stateMask,
          int offset)
      {
        super.apply(viewer, trigger, stateMask, offset);
        String templateName = getTemplate().getName();

        _maxRelevance++;
        if (_maxRelevance > 90)
        {
          _maxRelevance = 1;
          _relevances.clear();
        }

        _relevances.put(templateName, _maxRelevance);
      }

    };
  }

}
