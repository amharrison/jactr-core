/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html Contributors: IBM
 * Corporation - initial API and implementation Bjorn Freeman-Benson - initial
 * API and implementation
 ******************************************************************************/
package org.jactr.eclipse.runtime.launching.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourcePathComputer;
import org.jactr.eclipse.runtime.launching.ACTRLaunchConfigurationUtils;

/**
 * Computes the default source lookup path for a PDA launch configuration. The
 * default source lookup path is the folder or project containing the PDA
 * program being launched. If the program is not specified, the workspace is
 * searched by default.
 */
public class ACTRSourcePathComputerDelegate extends JavaSourcePathComputer
{

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.debug.internal.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public ISourceContainer[] computeSourceContainers(
      ILaunchConfiguration configuration, IProgressMonitor monitor)
      throws CoreException
  {

    Collection<IResource> modelFiles = ACTRLaunchConfigurationUtils
        .getModelFiles(configuration);
    HashSet<IContainer> containers = new HashSet<IContainer>();

    for (IResource modelFile : modelFiles)
      containers.add(modelFile.getProject());

    ArrayList<ISourceContainer> sourceContainers = new ArrayList<ISourceContainer>();

    for (IContainer container : containers)
    {
      if (container.getType() == IResource.PROJECT)
        sourceContainers.add(new ProjectSourceContainer((IProject) container,
            false));
      else if (container.getType() == IResource.FOLDER)
        sourceContainers.add(new FolderSourceContainer(container, false));
    }

    sourceContainers.addAll(Arrays.asList(super.computeSourceContainers(
        configuration, monitor)));

    if (sourceContainers.size() == 0)
    {
      sourceContainers.add(new WorkspaceSourceContainer());
    }

    return sourceContainers.toArray(new ISourceContainer[0]);
  }
}
