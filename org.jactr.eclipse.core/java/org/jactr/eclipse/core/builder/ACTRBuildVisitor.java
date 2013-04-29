/*
 * Created on Jan 29, 2004 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jactr.eclipse.core.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.comp.CompilationUnitManager;
import org.jactr.eclipse.core.comp.internal.IMutableCompilationUnit;

/**
 * @author harrison To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ACTRBuildVisitor implements IResourceVisitor
{
  static private transient Log LOGGER = LogFactory
                                          .getLog(ACTRBuildVisitor.class);

  IProgressMonitor             _monitor;

  /**
   * @param monitor
   */
  public ACTRBuildVisitor(IProgressMonitor monitor)
  {
    _monitor = monitor;
  }

  /**
   * compile all models within the model directory of a project
   */
  public boolean visit(IResource resource)
  {
    if (_monitor.isCanceled()) return false;

    if (!resource.exists()) return false;

    // visit children
    if (resource.getType() != IResource.FILE) return true;

    if (CompilationUnitManager.isJACTRModel(resource)
        && CompilationUnitManager.isOnModelPath(resource))
    {
      /*
       * let's do the deed..immediately.. first we acquire, without enabling
       * build..
       */
      IMutableCompilationUnit compUnit = (IMutableCompilationUnit) CompilationUnitManager
          .getManager().acquire(resource, false);
      try
      {

        /*
         * this is where we could use a recycled compiler..
         */
        BuildJob.BuildRunnable builder = new BuildJob.BuildRunnable(compUnit,
            null, false);

        builder.run(_monitor);
      }
      catch (Exception e)
      {
        CorePlugin.error("Failed to build " + resource.getName(), e);
      }
      finally
      {
        /*
         * and release
         */
        CompilationUnitManager.release(compUnit);
      }
    }

    return false;
  }
}