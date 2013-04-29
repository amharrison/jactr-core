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
package org.jactr.eclipse.ui.editor.link;

import java.net.URL;
import java.util.Collection;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.jactr.eclipse.core.ast.Support;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.ui.editor.ACTRModelEditor;
import org.jactr.eclipse.ui.editor.markers.PositionMarker;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.DetailedCommonTree;

public class ACTRHyperlinkDetector implements IHyperlinkDetector
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(ACTRHyperlinkDetector.class);

  private final ACTRModelEditor      _editor;

  public ACTRHyperlinkDetector(ACTRModelEditor editor)
  {
    _editor = editor;
  }

  public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region,
      boolean canShowMultipleHyperlinks)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Looking for hyperlink @ " + region.getOffset() + " : "
          + region.getLength());

    ICompilationUnit compUnit = _editor.getCompilationUnit();

    CommonTree destination = null;
    /*
     * we'll just find the object nearest to region..
     */
    CommonTree nameNode = getSourceCandidate(compUnit, region);
    if (nameNode != null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Found a name that matches the region " + nameNode);
      /*
       * we got something.. get the name and compare it against our buds.
       */
      String name = nameNode.getText().toLowerCase();
      destination = getDestination(compUnit
          .getNamedContents(JACTRBuilder.CHUNK), name);
      if (destination == null)
        destination = getDestination(compUnit
            .getNamedContents(JACTRBuilder.CHUNK_TYPE), name);
      if (destination == null)
        destination = getDestination(compUnit
            .getNamedContents(JACTRBuilder.PRODUCTION), name);
      if (destination == null)
        destination = getDestination(compUnit
            .getNamedContents(JACTRBuilder.BUFFER), name);

      if (destination != null)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Found a valid destination : " + destination);
        Region span = PositionMarker.getNodeSpan(nameNode, _editor.getBase());

        if (span != null)
          return new IHyperlink[] { new ACTRHyperlink(span,
              (DetailedCommonTree) destination) };
      }
    }

    return null;
  }

  /**
   * new version, but still not functional
   * 
   * @param compUnit
   * @param target
   * @return
   */
  protected CommonTree getSourceCandidateNew(ICompilationUnit compUnit,
      IRegion target)
  {
    CommonTree node = null;

    try
    {
      node = compUnit.getModelDescriptor();
      URL base = compUnit.getSource().toURL();
      node = Support.getNodeOfOffset(node, target.getOffset(), base);
    }
    catch (Exception e)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Failed to convert source url ", e);
    }

    CommonTree oldNode = getSourceCandidate(compUnit, target);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("old : " + oldNode + " new : " + node);

    return node;
  }

  protected CommonTree getSourceCandidate(ICompilationUnit compUnit,
      IRegion target)
  {
    /*
     * we will look in all the identifiers..and parents
     */

    Collection<CommonTree> values = compUnit.getContents(JACTRBuilder.PARENT);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Checking parents " + values.size());

    for (CommonTree node : values)
    {
      Region region = PositionMarker.getTreeSpan(node, null);
      if (region != null)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Checking parent region of " + node + " "
              + region.getOffset() + ":" + region.getLength());
        if (intersects(region, target)) return node;
      }
    }

    int[] types = new int[] { JACTRBuilder.IDENTIFIER,
        JACTRBuilder.CHUNK_IDENTIFIER, JACTRBuilder.CHUNK_TYPE_IDENTIFIER };

    for (int type : types)
    {
      /*
       * nope? ok, now for the identifiers
       */
      values = compUnit.getContents(type);

      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Checking identifiers " + values.size());

      for (CommonTree node : values)
      {
        Region region = PositionMarker.getTreeSpan(node, null);
        if (region != null)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("Checking identifier region of " + node + " "
                + region.getOffset() + ":" + region.getLength());
          if (intersects(region, target)) return node;
        }
      }
    }

    return null;
  }

  protected CommonTree getDestination(Map<String, CommonTree> nodes, String name)
  {
    for (Map.Entry<String, CommonTree> entry : nodes.entrySet())
      if (name.equalsIgnoreCase(entry.getKey())) return entry.getValue();
    return null;
  }

  protected boolean intersects(IRegion query, IRegion target)
  {
    int qLow = query.getOffset();
    int qHigh = query.getLength() + qLow;
    int tLow = target.getOffset();
    int tHigh = target.getLength() + tLow;

    return qLow >= tLow && qLow <= tHigh || qHigh >= tLow && qHigh <= tHigh
        || tLow >= qLow && tLow <= qHigh || tHigh >= qLow && tHigh <= qHigh;
  }
}
