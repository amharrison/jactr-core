/*
 * Created on Mar 22, 2007
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
package org.jactr.eclipse.runtime.launching.support;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;


public class ACTRProjectVariableResolver implements IDynamicVariableResolver
{

  static private IProject _currentProject;
  
  static public void setCurrentProject(IProject project)
  {
    _currentProject = project;
  }
  
  public String resolveValue(IDynamicVariable variable, String argument)
      throws CoreException
  {
    if(_currentProject!=null)
      return _currentProject.getName();
    return "unknown";
  }

}


