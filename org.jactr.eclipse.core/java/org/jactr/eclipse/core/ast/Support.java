/*
 * Created on Jun 1, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.core.ast;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javolution.util.FastList;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;
import org.jactr.io.antlr3.misc.DetailedCommonTree;

public class Support
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER          = LogFactory
                                                         .getLog(Support.class);

  /*
   * types of nodes that will not be displayed
   */
  static private Set<Integer>        _invisibleNodes = new TreeSet<Integer>();

  /*
   * types of nodes that will not be descended into
   */
  static private Set<Integer>        _dontDescend    = new TreeSet<Integer>();

  /**
   * nodes that we steal the children of...
   */
  static private Set<Integer>        _flattenNodes   = new TreeSet<Integer>();

  static private Set<Integer>        _alwaysShow     = new TreeSet<Integer>();

  static
  {
    _flattenNodes.add(JACTRBuilder.LIBRARY);

    _invisibleNodes.add(new Integer(JACTRBuilder.MODULES));
    _invisibleNodes.add(new Integer(JACTRBuilder.EXTENSIONS));
    _invisibleNodes.add(new Integer(JACTRBuilder.CLASS_SPEC));
    _invisibleNodes.add(new Integer(JACTRBuilder.LIBRARY));
    // we need identifiers to get the contents of the buffers
    _invisibleNodes.add(new Integer(JACTRBuilder.NUMBER));
    _invisibleNodes.add(new Integer(JACTRBuilder.PARENT));
    _invisibleNodes.add(new Integer(JACTRBuilder.VARIABLE));
    _invisibleNodes.add(new Integer(JACTRBuilder.NAME));

    _dontDescend.add(new Integer(JACTRBuilder.SLOT));
    _dontDescend.add(new Integer(JACTRBuilder.PARAMETER));
    _dontDescend.add(new Integer(JACTRBuilder.OUTPUT_ACTION));

    _alwaysShow.add(new Integer(JACTRBuilder.BUFFER));
  }

  static public boolean mustDisplay(CommonTree node)
  {
    return _alwaysShow.contains(node.getType());
  }

  static public boolean shouldDisplay(CommonTree node)
  {
    return !_invisibleNodes.contains(node.getType());
  }

  static public boolean shouldFlattern(CommonTree node)
  {
    return _flattenNodes.contains(node.getType());
  }

  static public Collection<CommonTree> getVisibleChildren(CommonTree node)
  {
    return getVisibleChildren(node, null);
  }

  static public Collection<CommonTree> getVisibleChildren(CommonTree node,
      URL source)
  {
    ArrayList<CommonTree> children = new ArrayList<CommonTree>();

    if (_dontDescend.contains(node.getType())) return children;

    for (int i = 0; i < node.getChildCount(); i++)
    {
      CommonTree child = (CommonTree) node.getChild(i);
      boolean ignore = false;

      if (child instanceof DetailedCommonTree && source != null
          && !mustDisplay(node))
      {
        URL url = ((DetailedCommonTree) child).getSource();
        ignore = url == null || !source.equals(url);
        /*
         * now we might include the node still if it contains something that
         * should be included..
         */
        if (ignore) ignore = getVisibleChildren(child, source).size() == 0;
      }

      if (!ignore)
        if (shouldDisplay(child))
          children.add(child);
        else if (shouldFlattern(child))
          children.addAll(getVisibleChildren(child, source));
    }
    return children;
  }

  static public Collection<CommonTree> getAllChildren(CommonTree root)
  {
    return getAllChildren(root, null);
  }

  static public Collection<CommonTree> getAllChildren(CommonTree root,
      Collection<CommonTree> container)
  {
    if (container == null) container = new ArrayList<CommonTree>();
    for (int i = 0, len = root.getChildCount(); i < len; i++)
      container.add((CommonTree) root.getChild(i));
    return container;
  }

  static public int getLine(Token token)
  {
    return token.getLine();
  }

  static public int getLine(CommonTree tree)
  {
    return tree.getLine();
  }

  static public int getCharInLine(CommonTree tree)
  {
    return tree.getCharPositionInLine();
  }

  static public int getCharInLine(Token token)
  {
    return token.getCharPositionInLine();
  }

  static public CommonTree getMostDistantChild(CommonTree tree)
  {
    CommonTree distantChild = null;
    int offset = -1;
    for (int i = 0; i < tree.getChildCount(); i++)
    {
      CommonTree child = (CommonTree) tree.getChild(i);
      CommonTree tmpDist = getMostDistantChild(child);
      int tmpOffset = -1;
      if ((tmpOffset = getCharInLine(tmpDist)) > offset)
      {
        distantChild = tmpDist;
        offset = tmpOffset;
      }
    }
    return distantChild;
  }

  static public String guessChunkType(CommonTree tree)
  {
    if (tree == null) return null;

    CommonTree ct = null;
    try
    {
      switch (tree.getType())
      {
        case JACTRBuilder.CHUNK_TYPE_IDENTIFIER:
        case JACTRBuilder.CHUNK_IDENTIFIER:
        case JACTRBuilder.IDENTIFIER:
          return tree.getText();
        case JACTRBuilder.CHUNK:
          ct = ASTSupport.getFirstDescendantWithType(tree, JACTRBuilder.PARENT);
          return ct.getText();

        case JACTRBuilder.CHUNK_TYPE:
          ct = ASTSupport.getFirstDescendantWithType(tree, JACTRBuilder.PARENT);
          if (ct != null) return ct.getText();
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("chunktype has no parent " + tree);
          return null;
      }
    }
    catch (Exception e)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Problem guessing chunktype of " + tree + " got " + ct, e);
    }
    return null;
  }

  static public Map<String, CommonTree> getSlots(
      Map<String, CommonTree> chunkTypes, String chunkTypeName)
  {
    CommonTree chunkType = chunkTypes.get(chunkTypeName);
    if (chunkType == null) return Collections.EMPTY_MAP;

    Map<String, CommonTree> slotMap = ASTSupport.getMapOfTrees(chunkType,
        JACTRBuilder.SLOT);
    /*
     * get parent..
     */
    for (int i = 0; i < chunkType.getChildCount(); i++)
      if (chunkType.getChild(i).getType() == JACTRBuilder.PARENT)
      {
        chunkType = (CommonTree) chunkType.getChild(i);
        break;
      }

    if (chunkType != null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Trying the parent of " + chunkTypeName + " "
            + chunkType.getText());
      slotMap.putAll(getSlots(chunkTypes, chunkType.getText()));
    }

    return slotMap;

  }

  /**
   * return true if the node has the same base url as base
   * 
   * @param node
   * @param base
   * @return
   */
  static private boolean isLocal(CommonTree node, URL base)
  {
    if (base == null) return true;
    if (node instanceof DetailedCommonTree)
    {
      URL source = ((DetailedCommonTree) node).getSource();
      if (source == null) return true;
      return base.equals(source);
    }
    return false;
  }

  /**
   * return start and end offset of the node (ignoring children)
   * 
   * @param node
   * @param base
   * @return
   */
  static public int[] getNodeOffsets(CommonTree node, URL base)
  {
    if (node == null) return null;

    if (!isLocal(node, base)) return null;

    CommonToken ct = (CommonToken) node.getToken();
    if (ct == null) return null;

    return new int[] { ct.getStartIndex(), ct.getStopIndex() };
  }

  /**
   * returns the start and end offset of the tree (including children)
   * 
   * @param node
   * @param base
   * @return
   */
  static public int[] getTreeOffsets(CommonTree node, URL base)
  {
    if (node == null) return new int[] { -1, -1 };

    int end = -1;
    int start = -1;
    CommonToken ct = (CommonToken) node.getToken();
    boolean isPseudoNode = ct == null || ct.getTokenIndex() == -1;

    if (node instanceof DetailedCommonTree && isLocal(node, base))
    {
      /*
       * oops, pseudo trees may have 0 for start and stop.. check the pseudo
       * status
       */
      start = ((DetailedCommonTree) node).getStartOffset();
      end = ((DetailedCommonTree) node).getStopOffset();
      return new int[] { start, end };
    }

    /*
     * if the detailed ct is pseudo or the ct is normal, we need to compute the
     * span
     */

    /*
     * use the token offset and then check the children
     */
    if (ct != null && !isPseudoNode)
    {
      start = ct.getStartIndex();
      end = ct.getStopIndex();
    }

    FastList<CommonTree> container = FastList.newInstance();
    for (CommonTree child : Support.getAllChildren(node, container))
    {
      int[] tmp = getTreeOffsets(child, base);
      end = Math.max(end, tmp[1]);
      if (tmp[0] >= 0) if (isPseudoNode)
        start = tmp[0];
      else if (tmp[0] < start) start = tmp[0];
    }
    FastList.recycle(container);

    if (node instanceof DetailedCommonTree)
    {
      ((DetailedCommonTree) node).setStartOffset(start);
      ((DetailedCommonTree) node).setEndOffset(end);
    }

    return new int[] { start, end };
  }

  /**
   * find the smallest node that contains offset.
   * 
   * @param root
   * @param offset
   * @param base
   * @return
   */
  static public CommonTree getNodeOfOffset(CommonTree root, int offset, URL base)
  {
    /*
     * outside
     */
    int[] bounds = Support.getTreeOffsets(root, base);
    if (bounds[1] < offset || bounds[0] > offset) return null;

    int size = bounds[1] - bounds[0];
    CommonTree candidate = root;

    FastList<CommonTree> container = FastList.newInstance();
    for (CommonTree child : Support.getAllChildren(root, container))
    {
      int[] cBounds = getTreeOffsets(child, base);

      // outside? unlikely
      if (cBounds[0] < bounds[0] || cBounds[1] > bounds[1]) continue;

      if (cBounds[0] <= offset && cBounds[1] >= offset)
        if (cBounds[1] - cBounds[0] <= size)
        {
          size = cBounds[1] - cBounds[0];
          candidate = child;
        }
    }
    FastList.recycle(container);

    /*
     * the child contains the offset, lets dive in.
     */
    if (candidate != root)
      candidate = Support.getNodeOfOffset(candidate, offset, base);

    return candidate;
  }
}
