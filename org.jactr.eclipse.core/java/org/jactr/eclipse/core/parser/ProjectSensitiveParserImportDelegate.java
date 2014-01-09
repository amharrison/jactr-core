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
package org.jactr.eclipse.core.parser;

import java.net.URL;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.bundles.descriptors.ASTParticipantDescriptor;
import org.jactr.eclipse.core.bundles.registry.ASTParticipantRegistry;
import org.jactr.io.parser.DefaultParserImportDelegate;
import org.jactr.io.participant.IASTParticipant;

public class ProjectSensitiveParserImportDelegate extends
    DefaultParserImportDelegate implements IProjectSensitive
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProjectSensitiveParserImportDelegate.class);

  private IProject                   _project;

  public void setProject(IProject project)
  {
    _project = project;
  }

  @Override
  public CommonTree importModuleInto(CommonTree modelDescriptor,
      String moduleClassName, boolean importContents) throws Exception
  {
    try
    {
      return super.importModuleInto(modelDescriptor, moduleClassName,
          importContents);
    }
    catch (Exception e)
    {
      CorePlugin.error(e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public CommonTree importExtensionInto(CommonTree modelDescriptor,
      String extensionClassName, boolean importContents) throws Exception
  {
    try
    {
      return super.importExtensionInto(modelDescriptor, extensionClassName,
          importContents);
    }
    catch (Exception e)
    {
      CorePlugin.error(e.getMessage(), e);
      throw e;
    }
  }

  @Override
  protected boolean isValidClassName(String moduleClassName)
  {
    if (super.isValidClassName(moduleClassName)) return true;

    try
    {
      /*
       * try to load from the Project
       */
      IJavaProject project = JavaCore.create(_project);
      IType type = project.findType(moduleClassName);

      if (type != null) return true;

      // CorePlugin.debug(String.format("Scanning for type %s",
      // moduleClassName));

      /*
       * ok. We couldn't find it in the heirarchy. As a module, there could be
       * loose couplings, so let's do something a little unusual. If the project
       * is aware of an ast participant that has a matched contributedclass, we
       * ignore this
       */
      for (ASTParticipantDescriptor desc : ASTParticipantRegistry.getRegistry()
          .getDescriptors(_project))
        if (desc.getContributingClassName().equals(moduleClassName)) //          CorePlugin.debug(String.format("Found contributor %s for %s in %s",
//              desc.getContributingClassName(), moduleClassName, project
//                  .getProject().getName()));
        return true;

      CorePlugin.error(String.format("Could not find %s within %s",
          moduleClassName, _project));
    }
    catch (Exception e)
    {
      CorePlugin.error(String.format("Could not find %s within %s",
          moduleClassName, _project), e);
    }

    return false;
  }

  /**
   * we've found the valid class name from the previous call to isValidClassName
   * if the super implementation can't find it, we check for any extensions that
   * provide one..
   * 
   * @param moduleClassName
   * @return
   */
  @Override
  protected IASTParticipant getASTParticipant(String moduleClassName)
  {
    IASTParticipant participant = super.getASTParticipant(moduleClassName);
    if (participant == null)
    {
      boolean participantIsDefined = false;

      /*
       * find one ourselves..
       */
      for (ASTParticipantDescriptor descriptor : org.jactr.eclipse.core.bundles.registry.ASTParticipantRegistry
          .getRegistry().getDescriptors(_project))
        if (descriptor.getContributingClassName().equals(moduleClassName))
        {
          participantIsDefined = true;

          // the code block below is handled by jactr.io.activator.Activator
          // /*
          // * if we can instantiate this, let's do so.. im not sure if this
          // * should actually be installed since we are project specific.. if
          // we
          // * had different versions active, bad things could happen
          // */
          // try
          // {
          // participant = (IASTParticipant) descriptor.instantiate();
          // if (participant != null) return participant;
          // }
          // catch (Exception e)
          // {
          // CorePlugin.debug("Could not instantiate "
          // + descriptor.getContributingClassName() + " for "
          // + moduleClassName, e);
          // }

          /*
           * construct and return. We know we can't use participantClass since
           * this is a workspace extension, we have to use BasicASTParticipat
           * and content location
           */
          String contentLocation = descriptor.getContentLocation();
          IPath location = new Path(contentLocation);
          try
          {
            IProject contributorProject = ResourcesPlugin.getWorkspace()
                .getRoot().getProject(descriptor.getContributor());

            // CorePlugin.debug(String.format("Checking %s(%s)",
            // contributorProject.getName(), contributorProject.exists()));

            IJavaProject project = JavaCore.create(contributorProject);
            IResource resource = null;

            IJavaElement containingPackage = project.findElement(location
                .removeLastSegments(1));

            if (containingPackage != null
                && containingPackage.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
            {
              if (LOGGER.isDebugEnabled())
                LOGGER.debug("Found package that contains " + location + " at "
                    + containingPackage.getPath());

              // CorePlugin.debug("Found package that contains " + location
              // + " at " + containingPackage.getPath());

              IResource tmp = containingPackage.getUnderlyingResource();
              if (tmp instanceof IContainer)
              {
                IContainer container = (IContainer) tmp;
                resource = container.findMember(location.lastSegment());
              }
            }
            // else
            // CorePlugin.debug(location + " Containing package = "
            // + containingPackage);

            if (resource == null)
              throw new RuntimeException(String.format(
                  "Could not find %s (%s) within %s' classpath",
                  contentLocation, location, _project.getName()));

            URL contentURL = resource.getLocationURI().toURL();

            // CorePlugin.debug(String.format(
            // "Creating participant for content @ %s",
            // contentURL.toExternalForm()));

            participant = new IDEBasicASTParticipant(contentURL);
          }
          catch (Exception e)
          {
            String message = "Could not create basic ast participant referencing "
                + contentLocation + " in " + _project.getName();
            if (LOGGER.isWarnEnabled()) LOGGER.warn(message, e);
            CorePlugin.warn(message, e);
            return null;
          }
        }

      if (participant == null && participantIsDefined)
      {
        /*
         * warn and log
         */
        String message = "Could not find a valid IASTParticipant in any dependencies for "
            + moduleClassName;
        if (LOGGER.isWarnEnabled()) LOGGER.warn(message);
        CorePlugin.warn(message);
      }
    }

    return participant;
  }

  @Override
  public URL resolveURL(String url, URL baseURL)
  {
    try
    {
      IJavaProject javaProject = JavaCore.create(_project);
      IClasspathEntry[] entries = javaProject.getResolvedClasspath(false);

      for (IClasspathEntry entry : entries)
        if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT
            && entry.getContentKind() == IPackageFragmentRoot.K_SOURCE)
        {
          IPath path = entry.getPath();
          if (LOGGER.isDebugEnabled())
            LOGGER.debug("checking " + entry + " at " + path);

          IProject tmpProject = ResourcesPlugin.getWorkspace().getRoot()
              .getProject(path.lastSegment());
          IFolder modelsFolder = tmpProject.getFolder("models");
          if (modelsFolder == null || !modelsFolder.exists()) continue;

          IFile modelFile = modelsFolder.getFile(url);
          if (modelFile == null || !modelFile.exists()) continue;

          // CorePlugin.debug("Found a matching file at "
          // + modelFile.getFullPath());

          if (javaProject.isOnClasspath(modelFile))
          {
            if (LOGGER.isDebugEnabled())
              LOGGER.debug("Is on classpath, returning url");

            URL rtn = modelFile.getLocation().toFile().toURI().toURL();

            // CorePlugin.debug(String.format("On path at %s",
            // rtn.toExternalForm()));

            return rtn;
          }
          else if (LOGGER.isDebugEnabled())
            LOGGER.debug("is not on classpath");
        }
    }
    catch (Exception e)
    {
      CorePlugin.error("Failed to extract location info for " + url, e);
    }
    return super.resolveURL(url, baseURL);
  }
}
