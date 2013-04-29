/*
 * Created on Mar 12, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.runtime.debug.util;

import java.util.Collection;
import java.util.Map;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.jactr.eclipse.core.comp.CompilationUnitManager;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;
import org.jactr.io.antlr3.builder.JACTRBuilder;

public class Utilities
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(Utilities.class);

  static public int extractLineNumber(ILaunch launch, String modelName,
      String elementName, int elementType)
  {
    ICompilationUnit compUnit = getCompilationUnitForAlias(launch, modelName);
    int lineNumber = -1;
    if (compUnit == null) return lineNumber;

    try
    {
      /*
       * try to figure out the line number of the breakpoint
       */

      Map<String, CommonTree> productions = compUnit
          .getNamedContents(elementType);

      CommonTree prod = productions.get(elementName);
      if (prod != null)
        lineNumber = prod.getFirstChildWithType(JACTRBuilder.NAME).getLine();

      return lineNumber;
    }
    finally
    {
      CompilationUnitManager.release(compUnit);
    }
  }

  /**
   * this compilation unit must be released when you are done..
   * 
   * @param launch
   * @param modelName
   * @return
   */
  static public ICompilationUnit getCompilationUnitForAlias(ILaunch launch,
      String modelName)
  {
    try
    {
      ILaunchConfiguration configuration = launch.getLaunchConfiguration();
      Collection<IResource> modelFiles = ACTRLaunchConfigurationUtils
          .getModelFiles(configuration);
      for (IResource modelFile : modelFiles)
        if (ACTRLaunchConfigurationUtils.getModelAliases(modelFile,
            configuration).contains(modelName))
          return CompilationUnitManager.acquire(modelFile);
    }
    catch (CoreException ce)
    {
      LOGGER.error("Could not get compilation unit for " + modelName, ce);
    }
    return null;

  }
}
