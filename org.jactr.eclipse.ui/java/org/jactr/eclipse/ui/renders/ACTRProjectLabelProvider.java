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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jactr.eclipse.core.project.ACTRProjectNature;
import org.jactr.eclipse.ui.images.JACTRImages;

/**
 * @author harrison
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ACTRProjectLabelProvider extends LabelProvider
{

  static private ILabelProvider _provider  = new ACTRProjectLabelProvider();
  
  public static ILabelProvider getInstance()
  {
    return _provider;
  }
  /**
   * 
   */
  protected ACTRProjectLabelProvider()
  {
    super();
  }

  @Override
  public String getText(Object element)
  {
    if(element instanceof IProject)
    {
      IProject project = (IProject)element;
      return project.getName();
    }
    return super.getText(element);
  }
  
  @Override
  public Image getImage(Object element)
  {
    if(element instanceof IProject)
    {
      IProject project = (IProject)element;
      try
      {
        if(project.isNatureEnabled(ACTRProjectNature.NATURE_ID))
          return JACTRImages.getImage(JACTRImages.PROJECT);
      }
      catch(CoreException ce)
      {
        
      }
    }
    return super.getImage(element);
  }
}
