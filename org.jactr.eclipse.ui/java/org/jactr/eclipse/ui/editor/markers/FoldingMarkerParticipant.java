/*
 * Created on Apr 20, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.ui.editor.markers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.ui.UIPlugin;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.io.antlr3.builder.JACTRBuilder;

public class FoldingMarkerParticipant implements IPositionMarkerParticipant
{

  static private final transient Log      LOGGER              = LogFactory
                                                                  .getLog(FoldingMarkerParticipant.class);

  static private Set<Integer>             _relevantTypes      = new TreeSet<Integer>();

  static
  {
    _relevantTypes.add(JACTRBuilder.CONDITIONS);
    _relevantTypes.add(JACTRBuilder.ACTIONS);
    _relevantTypes.add(JACTRBuilder.MODULES);
    _relevantTypes.add(JACTRBuilder.EXTENSIONS);
    _relevantTypes.add(JACTRBuilder.DECLARATIVE_MEMORY);
    _relevantTypes.add(JACTRBuilder.PROCEDURAL_MEMORY);
    _relevantTypes.add(JACTRBuilder.PRODUCTION);
    _relevantTypes.add(JACTRBuilder.CHUNK);
    _relevantTypes.add(JACTRBuilder.CHUNK_TYPE);
    _relevantTypes.add(JACTRBuilder.PARAMETERS);
    _relevantTypes = Collections.unmodifiableSet(_relevantTypes);

  }

  private final ACTRModelEditor           _editor;

  private final Map<Annotation, Position> _oldAnnotations     = new LinkedHashMap<Annotation, Position>();

  private final Map<Annotation, Position> _currentAnnotations = new LinkedHashMap<Annotation, Position>();

  /*
   * we use this to set the expanded states..
   */
  // private Iterator<Annotation> _orderedIterator;
  private Iterator<Boolean>               _initialFolding;

  public FoldingMarkerParticipant(ACTRModelEditor editor)
  {
    _editor = editor;
  }

  public Collection<Integer> getRelevantTypes()
  {
    return _relevantTypes;
  }

  public void positionCreated(ASTPosition position)
  {
    if (position == null)
    {
      CorePlugin.error("Null position received ", new RuntimeException());
      return;
    }

    ProjectionAnnotation newAnnotation = new ProjectionAnnotation();

    if (_initialFolding.hasNext())
      if (_initialFolding.next()) newAnnotation.markCollapsed();

    _currentAnnotations.put(newAnnotation, position);
  }

  public void positionsCleared(Position[] positions)
  {
  }

  public void preParse()
  {
    /*
     * save the current folding before we reparse
     */
    saveFolding();

    if (_initialFolding == null || !_initialFolding.hasNext())
      _initialFolding = loadFolding();

    // _editor.getProjectionAnnotationModel().modifyAnnotations(_oldAnnotations,
    // Collections.EMPTY_MAP, new Annotation[0]);
    _oldAnnotations.clear();
    _oldAnnotations.putAll(_currentAnnotations);
    _currentAnnotations.clear();

    // _orderedIterator = _oldAnnotations.keySet().iterator();
  }

  public void postParse()
  {
    final Annotation[] oldAnnotations = _oldAnnotations.keySet().toArray(
        new Annotation[_oldAnnotations.size()]);
    final Map<Annotation, Position> newAnnotations = new HashMap<Annotation, Position>(
        _currentAnnotations);

    Runnable updater = new Runnable() {
      public void run()
      {
        try
        {
          Iterator<Map.Entry<Annotation, Position>> itr = newAnnotations
              .entrySet().iterator();
          while (itr.hasNext())
          {
            Map.Entry<Annotation, Position> entry = itr.next();
            if (entry.getValue() == null)
            {
              CorePlugin.error("Null position was detected! Removing");
              itr.remove();
            }
          }

          _editor.getProjectionAnnotationModel().modifyAnnotations(
              oldAnnotations, newAnnotations, new Annotation[0]);
        }
        catch (Exception e)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("Could not update folding annotations ", e);
        }
      }
    };

    UIPlugin.getStandardDisplay().asyncExec(updater);
  }

  private void saveFolding()
  {
    if (_currentAnnotations.size() == 0) return;

    IEditorInput input = _editor.getEditorInput();
    if (input instanceof IFileEditorInput)
    {
      IFileEditorInput fileInput = (IFileEditorInput) input;

      StringBuilder sb = new StringBuilder();
      Collection<Boolean> initial = new ArrayList<Boolean>(_currentAnnotations
          .size());
      for (Annotation annotation : _currentAnnotations.keySet())
        if (((ProjectionAnnotation) annotation).isCollapsed())
        {
          sb.append("1");
          initial.add(Boolean.TRUE);
        }
        else
        {
          sb.append("0");
          initial.add(Boolean.FALSE);
        }

      if (LOGGER.isDebugEnabled()) LOGGER.debug("Saved Folding : " + initial);

      _initialFolding = initial.iterator();

      try
      {
        fileInput.getFile().setPersistentProperty(
            new QualifiedName(getClass().getName(), "folding"), sb.toString());
      }
      catch (CoreException e)
      {
      }
    }
  }

  private Iterator<Boolean> loadFolding()
  {
    Collection<Boolean> rtn = new ArrayList<Boolean>();
    IEditorInput input = _editor.getEditorInput();
    if (input instanceof IFileEditorInput)
    {
      IFileEditorInput fileInput = (IFileEditorInput) input;
      String folding = null;

      try
      {
        folding = fileInput.getFile().getPersistentProperty(
            new QualifiedName(getClass().getName(), "folding"));
      }
      catch (CoreException e)
      {
      }

      if (folding != null) for (int i = 0; i < folding.length(); i++)
        if (folding.charAt(i) == '1')
          rtn.add(Boolean.TRUE);
        else
          rtn.add(Boolean.FALSE);
    }

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Loaded Folding : " + rtn);

    return rtn.iterator();
  }
}
