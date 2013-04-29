/*
 * Created on Jan 29, 2004 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jactr.eclipse.core.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jactr.eclipse.core.comp.CompilationUnitManager;

/**
 * @author harrison To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ACTRBuildDeltaVisitor extends ACTRBuildVisitor implements
    IResourceDeltaVisitor
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(ACTRBuildDeltaVisitor.class);

  /**
   * @param monitor
   */
  public ACTRBuildDeltaVisitor(IProgressMonitor monitor)
  {
    super(monitor);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core
   * .resources.IResourceDelta)
   */
  public boolean visit(IResourceDelta delta) throws CoreException
  {
    IResource resource = delta.getResource();

    if (!CompilationUnitManager.isOnModelPath(resource)) return false;

    if (delta.getKind() == IResourceDelta.REMOVED)
      return resource.getType() != IResource.FILE;
    else if (delta.getKind() != IResourceDelta.ADDED
        && delta.getKind() != IResourceDelta.CHANGED)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("We only care about deltas of added or changed : "
            + delta.getKind());
      return false;
    }
    return visit(resource);
  }
}