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
package org.jactr.eclipse.ui.commands;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.jactr.eclipse.core.comp.CompilationUnitManager;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.core.comp.IProjectCompilationUnit;
import org.jactr.io.generator.CodeGeneratorFactory;
import org.jactr.io.generator.ICodeGenerator;

public class ConvertTo extends CompoundContributionItem
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(ConvertTo.class);

  public ConvertTo()
  {
  }

  public ConvertTo(String id)
  {
    super(id);
  }

  @Override
  protected IContributionItem[] getContributionItems()
  {
    Collection<IContributionItem> rtn = new ArrayList<IContributionItem>();
    IWorkbenchWindow ww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    ISelection selection = ww.getSelectionService().getSelection();
    ICompilationUnit compilationUnit = null;
    if (selection instanceof IStructuredSelection)
    {
      IStructuredSelection sSelection = (IStructuredSelection) selection;
      if (sSelection.getFirstElement() instanceof IResource)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Found resource " + sSelection.getFirstElement());
        compilationUnit = CompilationUnitManager.acquire((IResource) sSelection
            .getFirstElement());
      }
    }

    if (compilationUnit == null
        || !(compilationUnit instanceof IProjectCompilationUnit))
      return new IContributionItem[0];

    IResource resource = ((IProjectCompilationUnit) compilationUnit)
        .getResource();

    final String currentExtension = resource.getFileExtension();
    /*
     * find all the code generators..
     */
    for (String extension : CodeGeneratorFactory.getExtensions())
      if (!extension.equals(currentExtension))
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Creating convert to " + extension);
        final ICodeGenerator codeGenerator = CodeGeneratorFactory
            .getCodeGenerator(extension);
        final IFile file = (IFile) resource;
        final CommonTree modelDescriptor = compilationUnit.getModelDescriptor();
        IAction action = new Action(extension) {
          @Override
          public void run()
          {
            IFolder folder = (IFolder) file.getParent();
            String sansExtension = file.getName();
            sansExtension = sansExtension.substring(0, sansExtension
                .lastIndexOf(currentExtension));
            IFile toCreate = folder.getFile(sansExtension + getText());
            if (!toCreate.exists())
            {
              StringWriter sw = new StringWriter();
              for (StringBuilder line : codeGenerator.generate(modelDescriptor,
                  true))
              {
                sw.write(line.toString());
                sw.write("\n");
                line.delete(0, line.length());
              }

              InputStream input = new ByteArrayInputStream(sw.toString()
                  .getBytes());

              try
              {
                toCreate.create(input, true, null);
              }
              catch (CoreException e)
              {
                LOGGER.error("Could not create " + toCreate, e);
              }
            }
            else
              LOGGER.error("File " + toCreate + " already exists");
          }
        };

        rtn.add(new ActionContributionItem(action));
      }

    CompilationUnitManager.release(compilationUnit);

    return rtn.toArray(new IContributionItem[0]);
  }
}
