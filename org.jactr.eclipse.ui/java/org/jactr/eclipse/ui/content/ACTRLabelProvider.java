/**
 * Copyright (C) 2001-3, Anthony Harrison anh23@pitt.edu This library is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

/*
 * Created on Apr 26, 2004 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jactr.eclipse.ui.content;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jactr.eclipse.ui.images.JACTRImages;
import org.jactr.io.antlr3.builder.JACTRBuilder;
import org.jactr.io.antlr3.misc.ASTSupport;

/**
 * @author harrison To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ACTRLabelProvider extends LabelProvider
{
  /**
   * Default logger
   */
  static private transient final Log LOGGER = LogFactory
                                                .getLog(ACTRLabelProvider.class);

  private final boolean              _debug = false;

  /**
   * 
   */
  public ACTRLabelProvider()
  {
    super();
  }

  @Override
  public Image getImage(Object element)
  {
    Image img = null;
    if (element instanceof CommonTree)
    {
      CommonTree node = (CommonTree) element;
      img = getImageOfAST(node);
    }
    return img;
  }

  @Override
  public String getText(Object element)
  {
    CommonTree node = (CommonTree) element;
    if (node == null) return "";

    if (_debug) return node.toString();

    String rtn = node.getText();

    CommonTree nameNode = ASTSupport.getFirstDescendantWithType(node,
        JACTRBuilder.NAME);

    String name = "";
    if (nameNode != null) name = nameNode.getText();

    //LOGGER.error("Getting text for element " + node + " with type " + node.getType() + " and text " + rtn);
    
    int type = node.getType();
    switch (type)
    {
      case JACTRBuilder.CONDITIONS:
        rtn = "lhs";
        break;
      case JACTRBuilder.ACTIONS:
        rtn = "rhs";
        break;
      case JACTRBuilder.CHUNKS:
        rtn = "chunks";
        break;
      case JACTRBuilder.ADD_ACTION:
      case JACTRBuilder.REMOVE_ACTION:
      case JACTRBuilder.MODIFY_ACTION:
      case JACTRBuilder.QUERY_CONDITION:
      case JACTRBuilder.MATCH_CONDITION:
        rtn = node.getChild(0).getText();
        break;
      case JACTRBuilder.CHUNK:
      case JACTRBuilder.CHUNK_TYPE:
      case JACTRBuilder.PRODUCTION:
      case JACTRBuilder.BUFFER:
      case JACTRBuilder.MODEL:
        rtn = name;
        break;
      case JACTRBuilder.SLOTS:
        if (node.getChildCount() == 0)
          rtn = "";
        else
          rtn = "...";
        break;
      case JACTRBuilder.SLOT:
        rtn = new String(name + " " + node.getChild(1).getText() + " "
            + node.getChild(2).getText());
        break;
      case JACTRBuilder.PARAMETER:
        rtn = new String(name + "  " + node.getChild(1).getText());
        break;
      case JACTRBuilder.BUFFERS:
        rtn = "Buffers";
        break;
      case JACTRBuilder.PROCEDURAL_MEMORY:
        rtn = "Procedural";
        break;
      case JACTRBuilder.DECLARATIVE_MEMORY:
        rtn = "Declarative";
        break;
      case JACTRBuilder.PARAMETERS:
        rtn = "Parameters";
        break;
    }

    return rtn;
  }

  static public Image getImageOfAST(CommonTree node)
  {
    Image img = null;
    if (node == null) return null;
    int type = node.getType();
    switch (type)
    {
      case JACTRBuilder.LIBRARY:
      case JACTRBuilder.DECLARATIVE_MEMORY:
      case JACTRBuilder.PROCEDURAL_MEMORY:
        img = JACTRImages.getImage(JACTRImages.LIBRARY);
        break;
      case JACTRBuilder.MODEL:
        img = JACTRImages.getImage(JACTRImages.MODEL);
        break;
      case JACTRBuilder.PRODUCTION:
        img = JACTRImages.getImage(JACTRImages.PRODUCTION);
        break;
      case JACTRBuilder.IDENTIFIER:
      case JACTRBuilder.CHUNK:
      case JACTRBuilder.CHUNKS:
        img = JACTRImages.getImage(JACTRImages.CHUNK);
        break;
      case JACTRBuilder.CHUNK_TYPE:
        img = JACTRImages.getImage(JACTRImages.CHUNK_TYPE);
        break;
      case JACTRBuilder.BUFFER:
      case JACTRBuilder.BUFFERS:
        img = JACTRImages.getImage(JACTRImages.BUFFER);
        break;
      case JACTRBuilder.MATCH_CONDITION:
        img = JACTRImages.getImage(JACTRImages.CHECK);
        break;
      case JACTRBuilder.ADD_ACTION:
        img = JACTRImages.getImage(JACTRImages.ADD);
        break;
      case JACTRBuilder.REMOVE_ACTION:
        img = JACTRImages.getImage(JACTRImages.REMOVE);
        break;
      case JACTRBuilder.OUTPUT_ACTION:
        img = JACTRImages.getImage(JACTRImages.OUTPUT);
        break;
      case JACTRBuilder.MODIFY_ACTION:
        img = JACTRImages.getImage(JACTRImages.MODIFY);
        break;
      case JACTRBuilder.QUERY_CONDITION:
        img = JACTRImages.getImage(JACTRImages.QUERY);
        break;
      case JACTRBuilder.SCRIPTABLE_ACTION:
      case JACTRBuilder.SCRIPTABLE_CONDITION:
        img = JACTRImages.getImage(JACTRImages.SCRIPT);
        break;
      case JACTRBuilder.SLOT:
      case JACTRBuilder.SLOTS:
        img = JACTRImages.getImage(JACTRImages.SLOT);
        break;
      case JACTRBuilder.PARAMETERS:
      case JACTRBuilder.PARAMETER:
        img = JACTRImages.getImage(JACTRImages.PARAMETER);
        break;
      case JACTRBuilder.EXTENSIONS:
      case JACTRBuilder.EXTENSION:
        img = JACTRImages.getImage(JACTRImages.EXTENSION);
        break;
      case JACTRBuilder.MODULES:
      case JACTRBuilder.MODULE:
        img = JACTRImages.getImage(JACTRImages.EXTENSION);
        break;
      case JACTRBuilder.CONDITIONS:
      case JACTRBuilder.ACTIONS:
        img = JACTRImages.getImage(JACTRImages.CONTAINER);
        break;
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Returning " + img + " for " + type);
    return img;
  }

}