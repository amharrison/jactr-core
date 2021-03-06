/*
 * Created on May 14, 2006
 * Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu (jactr.org) This library is free
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
package org.jactr.io.antlr3.compiler;

import org.antlr.runtime.tree.CommonTree;
 
import org.slf4j.LoggerFactory;
import org.jactr.io.antlr3.misc.CommonTreeException;
public class CompilationInfo extends CommonTreeException
{

  /**
   logger definition
   */
  static public final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CompilationInfo.class);

  public CompilationInfo(String message, CommonTree node)
  {
    super(message, node);
  }
  
  public CompilationInfo(String message, CommonTree node, Throwable thrown)
  {
    super(message, node, thrown);
  }

}


