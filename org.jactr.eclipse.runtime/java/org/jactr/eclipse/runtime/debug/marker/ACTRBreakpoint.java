/*
 * Created on Jun 9, 2006
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
package org.jactr.eclipse.runtime.debug.marker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;
import org.jactr.eclipse.runtime.launching.norm.ACTRSession;

public class ACTRBreakpoint extends LineBreakpoint
{

  static public final String ID = "org.jactr.eclipse.runtime.debug.markerType.actrBreakpoint";
  static public final String MARKER_ID = ID+".marker";
  static public final String BREAKPOINT_NAME = "org.jactr.eclipse.runtime.debug.breakpoint.name";
  static public final String BREAKPOINT_TYPE = "org.jactr.eclipse.runtime.debug.breakpoint.type";
  static public final String PRODUCTION = "Production";
//  static public final String CHUNK = "Chunk";
//  static public final String CHUNK_TYPE = "ChunkType";
//  static public final String BUFFER = "Buffer";
  
  public ACTRBreakpoint()
  {
    
  }
  
  public ACTRBreakpoint(final IResource resource, final String breakPointType, final String breakPointName, final int lineNumber) throws CoreException
  {
    IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
        public void run(IProgressMonitor monitor) throws CoreException {
            IMarker marker = resource
                    .createMarker(MARKER_ID);
            setMarker(marker);
            
            marker.setAttribute(IBreakpoint.ENABLED, Boolean.TRUE);
            marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
            marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
            marker.setAttribute(BREAKPOINT_TYPE, breakPointType);
            marker.setAttribute(BREAKPOINT_NAME, breakPointName);
            marker.setAttribute(IMarker.MESSAGE, breakPointType+" breakpoint ("+breakPointName+") : "
                    + resource.getName() + "  @ " + lineNumber + "]");
        }
    };
    run(getMarkerRule(resource), runnable);
  }
  
  
  public String getModelIdentifier()
  {
    return ACTRSession.ACTR_DEBUG_MODEL;
  }
  
  public String getBreakpointName()
  {
    return getMarker().getAttribute(BREAKPOINT_NAME,"");
  }
  
  public String getBreakpointType()
  {
    return getMarker().getAttribute(BREAKPOINT_TYPE,"");
  }
}


