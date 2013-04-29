/*
 * Created on Apr 19, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jface.text.Position;
import org.jactr.io.antlr3.misc.DetailedCommonTree;

public class ASTPosition extends Position
{
  CommonTree              _node;

  ASTPosition             _parent;

  Collection<ASTPosition> _children;

  public ASTPosition(int offset, int length, CommonTree node)
  {
    super(offset, length);
    _node = node;
  }

  public CommonTree getNode()
  {
    return _node;
  }
  
  public URL getBase()
  {
    if(_node instanceof DetailedCommonTree)
      return ((DetailedCommonTree)_node).getSource();
    return null;
  }

  public boolean contains(int offset)
  {
    return this.offset <= offset && offset <= this.length + this.offset;
  }

  public void addChild(ASTPosition child)
  {
    if (_children == null) _children = new ArrayList<ASTPosition>();
    _children.add(child);
    child.setParent(this);
  }

  public Collection<ASTPosition> getChildren()
  {
    if (_children != null)
      return Collections.unmodifiableCollection(_children);
    return Collections.EMPTY_LIST;
  }

  private void setParent(ASTPosition parent)
  {
    _parent = parent;
  }

  public ASTPosition getParent()
  {
    return _parent;
  }

  @Override
  public String toString()
  {
    return "[" + getOffset() + ":" + getLength() + ":" + getNode() + "]";
  }
}
