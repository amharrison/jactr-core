/*
 * Created on Apr 6, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.ui.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jactr.eclipse.core.project.ACTRProjectUtils;

public class ConvertToACTRProjectHandler extends AbstractHandler
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(ConvertToACTRProjectHandler.class);

  public Object execute(ExecutionEvent event) throws ExecutionException
  {
    ISelection selection = HandlerUtil.getCurrentSelection(event);
    IProject project = null;
    if (selection instanceof IStructuredSelection)
    {
      IStructuredSelection sSelection = (IStructuredSelection) selection;
      Object first = sSelection.getFirstElement();
      if (first instanceof IAdaptable)
        project = (IProject) ((IAdaptable) first).getAdapter(IProject.class);
    }

    if (project != null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Attempting to convert project " + project);

      try
      {
        ACTRProjectUtils.addNature(project);
      }
      catch (Exception e)
      {
        LOGGER.error("Could not add jactrProject nature", e);
      }
    }
    else
      LOGGER.error("Could not get project from current selection ");

    return null;
  }

}
