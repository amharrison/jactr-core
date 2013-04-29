/**
 * Copyright (C) 2001-3, Anthony Harrison anh23@pitt.edu This library is free
 * software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version. This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details. You should have received a copy of the GNU Lesser
 * General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

/*
 * Created on May 6, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jactr.eclipse.ui.renders;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jactr.eclipse.ui.images.JACTRImages;

/**
 * @author harrison
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ModelLabelProvider extends LabelProvider
{

  static private ILabelProvider _provider  = new ModelLabelProvider();
  
  public static ILabelProvider getInstance()
  {
    return _provider;
  }
  /**
   * 
   */
  protected ModelLabelProvider()
  {
    super();
  }
  
  public String getText(Object element)
  {
    if(element instanceof IFile)
    {
      IFile file = (IFile)element;
      if(file.getParent().getName().equals("models"))
        return file.getName();
    }
    
    return null;
  }
  
  public Image getImage(Object element)
  {
    if(element instanceof IFile)
    {
      IFile file = (IFile)element;
      if(file.getParent().getName().equals("models"))
        return JACTRImages.getImage(JACTRImages.MODEL);
    }
    return super.getImage(element);
  }

}
