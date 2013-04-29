/*
 * Created on Mar 19, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.core.compiler;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.jactr.eclipse.core.bundles.descriptors.ASTParticipantDescriptor;
import org.jactr.eclipse.core.bundles.registry.ASTParticipantRegistry;
import org.jactr.eclipse.core.parser.IProjectSensitive;
import org.jactr.io.antlr3.compiler.CompilationError;

public class ProjectSensitiveClassVerifyingUnitCompiler extends
    org.jactr.io.compiler.ClassVerifyingUnitCompiler implements
    IProjectSensitive
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProjectSensitiveClassVerifyingUnitCompiler.class);

  private IProject                   _project;

  @SuppressWarnings("unchecked")
  @Override
  protected void tryToLoadClass(CommonTree classSpecNode, String className,
      Class ofType) throws Exception
  {

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Trying to load " + className);

    try
    {
      super.tryToLoadClass(classSpecNode, className, ofType);
      return;
    }
    catch (Exception e)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Could not find " + className
            + ", will search the current project");
    }

    if (_project == null) return;

    try
    {
      /*
       * try to project..
       */
      IJavaProject project = JavaCore.create(_project);
      IType type = project.findType(className, new NullProgressMonitor());

      if (type == null)
      {
        // CorePlugin.debug(String.format("Scanning for type %s", className));
        /*
         * ok. We couldn't find it in the heirarchy. As a module, there could be
         * loose couplings, so let's do something a little unusual. If the
         * project is aware of an ast participant that has a matched
         * contributedclass, we ignore this
         */
        for (ASTParticipantDescriptor desc : ASTParticipantRegistry
            .getRegistry().getDescriptors(_project))
          if (desc.getContributingClassName().equals(className)) return;

        throw new CompilationError(String.format(
            "Could not find %s in project %s", className, _project.getName()),
            classSpecNode);
      }

      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Found type " + type + " for " + className);

      /*
       * check super type..
       */
      if (ofType != null)
      {
        IType superType = project.findType(ofType.getName());
        if (superType == null)
          throw new RuntimeException("Could not get type information for "
              + ofType.getName());

      }
    }
    catch (Exception e)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Could not find type ", e);
      throw e;
    }

  }

  public void setProject(IProject project)
  {
    _project = project;

  }
}
