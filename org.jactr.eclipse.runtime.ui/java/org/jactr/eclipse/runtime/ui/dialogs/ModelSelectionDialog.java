/**
 * Copyright (C) 2001-3, Anthony Harrison anh23@pitt.edu This library is free
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

/*
 * Created on May 6, 2004 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jactr.eclipse.runtime.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.jactr.eclipse.core.project.ACTRProjectUtils;
import org.jactr.eclipse.ui.renders.ACTRProjectModelLabelProvider;
import org.jactr.eclipse.ui.renders.ModelLabelProvider;
import org.jactr.io.parser.ModelParserFactory;

/**
 * @author harrison To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ModelSelectionDialog extends TwoPaneElementSelector
{

  private final IRunnableContext fRunnableContext;

  private final IProject         fProject;

  public ModelSelectionDialog(Shell shell, IRunnableContext context,
      IProject project)
  {
    super(shell, ACTRProjectModelLabelProvider.getInstance(),
        ModelLabelProvider.getInstance());

    fRunnableContext = context;
    fProject = project;
  }

  /**
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(Shell newShell)
  {
    super.configureShell(newShell);
  }

  /**
   * @see org.eclipse.jface.window.Window#open()
   */
  @Override
  public int open()
  {
    IResource[] types = getModels();
    if (types == null)
    {
      return CANCEL;
    }
    setElements(types);
    return super.open();
  }

  /**
   * Return all types extending <code>java.lang.Applet</code> in the project,
   * or all types extending Applet in the workspace if the project is
   * <code>null</code>. If the search is canceled, return <code>null</code>.
   */
  private IResource[] getModels()
  {
    // Populate an array of java projects with either the project specified in
    // the constructor, or ALL projects in the workspace if no project was
    // specified
    final IProject[] projects;
    if (fProject == null)
    {
      projects = ACTRProjectUtils.getACTRProjects();
    }
    else
    {
      projects = new IProject[] { fProject };
    }

    // For each java project, calculate the Applet types it contains and add
    // them to the results
    final int projectCount = projects.length;
    final Set<IResource> results = new HashSet<IResource>(projectCount);
    boolean canceled = false;
    try
    {
      fRunnableContext.run(true, true, new IRunnableWithProgress() {
        public void run(IProgressMonitor monitor)
        {
          monitor.beginTask("Searching for ACT-R Models", projectCount); //$NON-NLS-1$
          for (int i = 0; i < projectCount; i++)
          {
            IProject project = projects[i];
            SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
            results.addAll(findModels(subMonitor, project));
            monitor.worked(1);
          }
          monitor.done();
        }
      });
    }
    catch (InvocationTargetException ite)
    {
    }
    catch (InterruptedException ie)
    {
      canceled = true;
    }

    // Convert the results to an array and return it
    if (canceled)
    {
      return null;
    }
    else
    {
      IResource[] types = null;
      types = results.toArray(new IResource[results.size()]);
      return types;
    }
  }

  /**
   * @param subMonitor
   * @param project
   * @return
   */
  protected Collection<IResource> findModels(SubProgressMonitor subMonitor,
      IProject project)
  {
    List<IResource> rtn = new ArrayList<IResource>();
    Collection extensions = ModelParserFactory.getValidExtensions();

    IFolder root = project.getFolder("models");
    checkPath(root, rtn, subMonitor, extensions);
    return rtn;
  }

  private void checkPath(IResource resource, Collection<IResource> rtn,
      SubProgressMonitor monitor, Collection validExtensions)
  {
    if (!resource.exists()) return;
    if (resource instanceof IFolder)
    {
      try
      {
        IResource[] children = ((IFolder) resource).members(false);
        for (int i = 0; !monitor.isCanceled() && children != null
            && i < children.length; i++)
          checkPath(children[i], rtn, monitor, validExtensions);
      }
      catch (CoreException e)
      {
      }
    }
    else if (resource instanceof IFile)
    {
      String ext = ((IFile) resource).getFileExtension().toLowerCase();
      if (validExtensions.contains(ext)) rtn.add(resource);
    }
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public Control createDialogArea(Composite parent)
  {
    Control control = super.createDialogArea(parent);
    applyDialogFont(control);
    return control;
  }
}