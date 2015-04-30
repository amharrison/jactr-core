package org.jactr.eclipse.ui.editor.highlighter;

/*
 * default logging
 */
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.ui.progress.UIJob;
import org.jactr.eclipse.core.ast.Support;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.editor.markers.ASTPosition;
import org.jactr.eclipse.ui.editor.markers.PositionMarker;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;

public class ReferenceHighlighter
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER                = LogFactory
                                                               .getLog(ReferenceHighlighter.class);

  static private final String        HIGHLIGHT_POSITION_ID = "org.jactr.highlight.positions";

  private ACTRModelEditor            _editor;

  private int                        _lastOffset           = 0;

  private CommonTree                 _lastNode;

  private Annotation[]               _lastAnnotations      = new Annotation[0];

  private UIJob                      _job;

  private volatile int               _caretOffset;

  public ReferenceHighlighter(ACTRModelEditor editor)
  {
    _editor = editor;
  }

  public void highlight(int caretOffset)
  {
    if (_lastOffset == caretOffset) return;

    if (_job != null) _job.cancel();

    _caretOffset = caretOffset;

    if (_job == null)
    {
      _job = new UIJob("Highlighting references") {

        @Override
        public IStatus runInUIThread(IProgressMonitor monitor)
        {
          try
          {
            highlightInternal(_caretOffset);
          }
          catch (Exception e)
          {
            LOGGER
                .error("Failed to highlight references at " + _caretOffset, e);
          }
          return Status.OK_STATUS;
        }

      };
      _job.setSystem(true);
    }

    _job.schedule(350);
  }

  private ASTPosition getContainer(IDocument document, URL base, int caretOffset)
  {
    /*
     * this will be production,chunk,chunktype,or buffer.. and I can inspect the
     * children more closely
     */
    ASTPosition container = PositionMarker.getPosition(document, base,
        caretOffset);

    while (container != null
        && container.getNode().getType() != JACTRBuilder.PRODUCTION
        && container.getNode().getType() != JACTRBuilder.CHUNK
        && container.getNode().getType() != JACTRBuilder.CHUNK_TYPE)
      container = container.getParent();

    if (container == null) return null;

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("ASTPosition @ " + caretOffset + " : " + container);

    return container;
  }

  protected void removeAnnotations(IDocument document)
  {
    /*
     * clear out the old positions
     */
    try
    {
      document.removePositionCategory(HIGHLIGHT_POSITION_ID);
    }
    catch (BadPositionCategoryException e1)
    {
    }

    try
    {
      ((IAnnotationModelExtension) _editor.getAnnotationModel())
          .replaceAnnotations(_lastAnnotations, Collections.EMPTY_MAP);
    }
    catch (Exception e)
    {
      // there may not be an annotation model
    }

    _lastAnnotations = new Annotation[0];
  }

  protected void addAnnotations(IDocument document,
      Map<Annotation, Position> newAnnotations)
  {
    if (newAnnotations.size() != 0)
    {
      document.addPositionCategory(HIGHLIGHT_POSITION_ID);

      if (LOGGER.isDebugEnabled()) LOGGER.debug("Adding positions");
      /*
       * add the new
       */
      for (Position position : newAnnotations.values())
        try
        {
          document.addPosition(HIGHLIGHT_POSITION_ID, position);
        }
        catch (BadLocationException e)
        {
          LOGGER
              .error(
                  "ReferenceHighlighter.highlight threw BadLocationException : ",
                  e);
        }
        catch (BadPositionCategoryException e)
        {
          LOGGER
              .error(
                  "ReferenceHighlighter.highlight threw BadPositionCategoryException : ",
                  e);
        }

      if (LOGGER.isDebugEnabled()) LOGGER.debug("Adding annotations");

      ((IAnnotationModelExtension) _editor.getAnnotationModel())
      // .replaceAnnotations(_lastAnnotations, newAnnotations);
          .replaceAnnotations(new Annotation[0], newAnnotations);

      _lastAnnotations = newAnnotations.keySet().toArray(_lastAnnotations);
    }
  }

  protected void highlightInternal(int caretOffset)
  {

    /*
     * we use base to constrain our search to ASTs that are directly contained
     * in this file..
     */
    URL base = _editor.getBase();
    IDocument document = _editor.getDocumentProvider().getDocument(
        _editor.getEditorInput());

    if (document == null) return;

    ASTPosition container = getContainer(document, base, caretOffset);
    if (container == null)
    {
      removeAnnotations(document);
      return;
    }

    CommonTree containerNode = container.getNode();
    CommonTree actualNode = Support.getNodeOfOffset(containerNode, caretOffset,
        base);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Actual node : " + actualNode + "."
          + (actualNode != null ? actualNode.getType() : -1));

    if (_lastNode == actualNode) return;

    Map<Annotation, Position> newAnnotations = highlightReferences(
        containerNode, actualNode);

    removeAnnotations(document);
    addAnnotations(document, newAnnotations);

    _lastNode = actualNode;
    _lastOffset = caretOffset;
  }

  private Map<Annotation, Position> highlightReferences(
      CommonTree containerNode, CommonTree actualNode)
  {
    Map<Annotation, Position> newAnnotations = Collections.emptyMap();

    if (actualNode == null) return newAnnotations;

    switch (actualNode.getType())
    {
      case JACTRBuilder.VARIABLE:
        newAnnotations = highlightVariables(containerNode, actualNode.getText());
        break;

      case JACTRBuilder.IDENTIFIER:
      case JACTRBuilder.CHUNK_IDENTIFIER:
        newAnnotations = highlightChunks(_editor.getCompilationUnit(),
            actualNode.getText());
        break;

      case JACTRBuilder.PARENT:
      case JACTRBuilder.CHUNK_TYPE_IDENTIFIER:
        newAnnotations = highlightChunkTypes(_editor.getCompilationUnit(),
            actualNode.getText());
        break;

      case JACTRBuilder.NAME:
        switch (containerNode.getType())
        {
          case JACTRBuilder.CHUNK:
            newAnnotations = highlightChunks(_editor.getCompilationUnit(),
                actualNode.getText());
            break;
          case JACTRBuilder.CHUNK_TYPE:
            newAnnotations = highlightChunkTypes(_editor.getCompilationUnit(),
                actualNode.getText());
            break;
        }
        break;
    }

    return newAnnotations;
  }

  private Map<Annotation, Position> highlightVariables(CommonTree production,
      String variableName)
  {
    /*
     * find all the variable elements
     */
    HashMap<Annotation, Position> rtn = new HashMap<Annotation, Position>();
    Collection<CommonTree> variables = ASTSupport.getAllDescendantsWithType(
        production, JACTRBuilder.VARIABLE);
    for (CommonTree variable : variables)
    {
      String tmp = variable.getText();
      if (variableName.equalsIgnoreCase(tmp))
      {
        Region span = PositionMarker.getNodeSpan(variable, null);
        if (LOGGER.isDebugEnabled()) LOGGER.debug("variable @ " + span);
        Position position = new Position(span.getOffset(), span.getLength());
        Annotation annotation = new Annotation(
            HighlightAnnotations.VARIABLE_ID, false, variableName);
        rtn.put(annotation, position);
      }
      else if (LOGGER.isDebugEnabled())
        LOGGER.debug(tmp + " doesn't match " + variableName);
    }

    /*
     * ideally we also want to search out any strings and see if they contain
     * the variablename
     */

    return rtn;
  }

  private Map<Annotation, Position> highlightChunks(
      ICompilationUnit modelDescriptor, String chunkName)
  {
    Map<Annotation, Position> map = highlightNamable(modelDescriptor,
        chunkName, JACTRBuilder.CHUNK, JACTRBuilder.CHUNK_IDENTIFIER,
        HighlightAnnotations.CHUNK_ID, _editor.getBase());

    map.putAll(highlightNamable(modelDescriptor, chunkName, -1,
        JACTRBuilder.IDENTIFIER, HighlightAnnotations.CHUNK_ID,
        _editor.getBase()));

    return map;
  }

  private Map<Annotation, Position> highlightChunkTypes(
      ICompilationUnit modelDescriptor, String chunkTypeName)
  {
    Map<Annotation, Position> map = highlightNamable(modelDescriptor,
        chunkTypeName, JACTRBuilder.CHUNK_TYPE,
        JACTRBuilder.CHUNK_TYPE_IDENTIFIER, HighlightAnnotations.CHUNK_TYPE_ID,
        _editor.getBase());

    map.putAll(highlightNamable(modelDescriptor, chunkTypeName, -1,
        JACTRBuilder.PARENT, HighlightAnnotations.CHUNK_TYPE_ID,
        _editor.getBase()));

    return map;
  }

  private Map<Annotation, Position> highlightNamable(
      ICompilationUnit modelDescriptor, String name, int baseType,
      int identifierType, String annotationType, URL source)
  {
    HashMap<Annotation, Position> rtn = new HashMap<Annotation, Position>();
    if (baseType >= 0)
    {
      // get all the chunks so we can mark the actual instance of the chunk..
      Map<String, CommonTree> allOfBaseType = modelDescriptor
          .getNamedContents(baseType);
      CommonTree definition = allOfBaseType.get(name);
      if (definition != null)
      {
        definition = ASTSupport.getFirstDescendantWithType(definition,
            JACTRBuilder.NAME);
        Region span = PositionMarker.getNodeSpan(definition, source);
        if (span != null)
          rtn.put(new Annotation(annotationType, false, name), new Position(
              span.getOffset(), span.getLength()));
      }
    }

    // now we get all chunk_id so that we can mark all references to chunk..
    for (CommonTree identifier : modelDescriptor.getContents(identifierType))
      if (name.equalsIgnoreCase(identifier.getText()))
      {
        Region span = PositionMarker.getNodeSpan(identifier, source);
        if (span != null)
          rtn.put(new Annotation(annotationType, false, name), new Position(
              span.getOffset(), span.getLength()));
      }

    return rtn;
  }
}
