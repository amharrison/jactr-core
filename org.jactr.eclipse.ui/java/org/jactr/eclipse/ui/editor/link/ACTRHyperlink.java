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

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.jactr.eclipse.ui.editor.command.GoTo;
import org.jactr.io.antlr3.misc.DetailedCommonTree;

public class ACTRHyperlink implements IHyperlink
{

  private final IRegion _target;
  
  private final DetailedCommonTree _destination;

  public ACTRHyperlink(IRegion location, DetailedCommonTree destination)
  {
    _target = location;
    _destination = destination;
  }

  public IRegion getHyperlinkRegion()
  {
    return _target;
  }

  public String getHyperlinkText()
  {
    return null;
  }

  public String getTypeLabel()
  {
    return null;
  }

  public void open()
  {
    try
    {
      GoTo.goTo(_destination);
    }
    catch (Exception e)
    {

    }
  }

}
