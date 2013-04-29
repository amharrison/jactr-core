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
package org.jactr.eclipse.ui.editor.hover;

import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.jactr.eclipse.core.ast.Support;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.editor.markers.ASTPosition;
import org.jactr.eclipse.ui.editor.markers.PositionMarker;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;

public class ACTRTextHover implements ITextHover
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(ACTRTextHover.class);

  ACTRModelEditor                    _editor;

  public ACTRTextHover(ACTRModelEditor editor)
  {
    _editor = editor;
  }

  public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
  {
    IResource resource = getResource();
    if (resource != null)
      try
      {
        IMarker[] markers = resource.findMarkers(IMarker.PROBLEM, true,
            IResource.DEPTH_INFINITE);
        for (IMarker marker : markers)
        {
          int start = marker.getAttribute(IMarker.CHAR_START, -1);
          int end = marker.getAttribute(IMarker.CHAR_END, -1);
          if (start == hoverRegion.getOffset()
              && end - start == hoverRegion.getLength())
          {
            String priority = "Error : ";
            if (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR) == IMarker.SEVERITY_WARNING)
              priority = "Warning : ";
            return priority + marker.getAttribute(IMarker.MESSAGE, "");
          }
        }

        ICompilationUnit compUnit = _editor.getCompilationUnit();
        Map<String, CommonTree> chunkTypes = compUnit
            .getNamedContents(JACTRBuilder.CHUNK_TYPE);
        Map<String, CommonTree> chunks = compUnit
            .getNamedContents(JACTRBuilder.CHUNK);

        String rtn = getHover(PositionMarker.getPosition(textViewer
            .getDocument(), _editor.getBase(), hoverRegion.getOffset()), hoverRegion.getOffset(),
            chunkTypes, chunks);

        return rtn;
      }
      catch (CoreException ce)
      {

      }
    return null;
  }

  protected String getHover(ASTPosition position, int offset,
      Map<String, CommonTree> chunkTypes, Map<String, CommonTree> chunks)
  {
    CommonTree posNode = position.getNode();

    CommonTree node = Support.getNodeOfOffset(posNode, offset, _editor
        .getBase());

    if (node == null) return null;

    String name = node.getText();
    int type = node.getType();

    if (type == JACTRBuilder.IDENTIFIER
        || type == JACTRBuilder.CHUNK_IDENTIFIER
        || type == JACTRBuilder.CHUNK_TYPE_IDENTIFIER
        || type == JACTRBuilder.PARENT)
    {
      /*
       * it's a chunk, snag the parent's name for below
       */
      if (chunks.containsKey(name))
      {
        node = ASTSupport.getFirstDescendantWithType(chunks.get(name),
            JACTRBuilder.PARENT);

        if (LOGGER.isDebugEnabled())
          LOGGER.debug(name + " is a chunk, shifting to parent " + node);

        name = node.getText();
      }

      /*
       * if it is a valid chunktype, snag the name and slots
       */
      if (chunkTypes.containsKey(name))
      {
        /*
         * references a valid chunk type..
         */
        StringBuilder sb = new StringBuilder("isa ");
        sb.append(name).append("\n");
        sb.append("\tslots:");
        Collection<String> slotNames = Support.getSlots(chunkTypes, name)
            .keySet();
        for (String slotName : slotNames)
        {
          if (sb.length() % 20 == 0) sb.append("\n\t\t");
          sb.append(slotName).append(", ");
        }

        if (slotNames.size() != 0) sb.delete(sb.length() - 2, sb.length());

        return sb.toString();
      }
    }

    return null;
  }

  public IRegion getHoverRegion(ITextViewer textViewer, int offset)
  {
    IResource resource = getResource();
    if (resource != null)
      try
      {
        IMarker[] markers = resource.findMarkers(IMarker.PROBLEM, true,
            IResource.DEPTH_INFINITE);
        for (IMarker marker : markers)
        {
          int start = marker.getAttribute(IMarker.CHAR_START, -1);
          int end = marker.getAttribute(IMarker.CHAR_END, -1);
          if (start <= offset && offset <= end)
            return new Region(start, end - start);
        }

        URL base = _editor.getBase();
        ASTPosition position = PositionMarker.getPosition(textViewer
            .getDocument(), base, offset);

        if (position != null)
        {
          CommonTree node = Support.getNodeOfOffset(position.getNode(),
              offset, base);
          if (node == null) return null;

          int type = node.getType();
          if (type == JACTRBuilder.IDENTIFIER
              || type == JACTRBuilder.CHUNK_IDENTIFIER
              || type == JACTRBuilder.CHUNK_TYPE_IDENTIFIER
              || type == JACTRBuilder.PARENT)
            return PositionMarker.getTreeSpan(node, base);

          /*
           * nope, a slot.. have to use a different location
           */
          CommonTree id = ASTSupport.getFirstDescendantWithType(position
              .getNode(), JACTRBuilder.IDENTIFIER);
          if (id != null) return PositionMarker.getTreeSpan(id, base);
        }
      }
      catch (CoreException ce)
      {

      }
    return null;
  }

  protected IResource getResource()
  {
    IEditorInput input = _editor.getEditorInput();
    if (input instanceof IFileEditorInput) return ((IFileEditorInput) input).getFile();
    return null;
  }
}
