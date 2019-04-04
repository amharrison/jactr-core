/*
 * Created on Jan 29, 2004 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jactr.eclipse.core.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.collections.impl.factory.Sets;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.io.parser.ModelParserFactory;

/**
 * @author harrison To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ACTRProjectUtils
{

  static private transient final Log LOGGER = LogFactory

      .getLog(ACTRProjectUtils.class);

  static public boolean isACTRProject(IProject project)
  {
    try
    {
      if (project == null) return false;

      return project.isNatureEnabled(ACTRProjectNature.NATURE_ID);
    }
    catch (CoreException e)
    {
      return false;
    }
  }

  static public IProject[] getACTRProjects()
  {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    Collection<IProject> projects = new ArrayList<IProject>();
    for (IProject project : workspaceRoot.getProjects())
      if (isACTRProject(project)) projects.add(project);
    return projects.toArray(new IProject[projects.size()]);
  }

  static public IProject getACTRProject(String projectName)
  {
    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
    IProject project = workspaceRoot.getProject(projectName);

    if (project.exists() && project.isOpen() && isACTRProject(project))
      return project;
    else
      return null;
  }

  static public Collection<IFile> getModels(IProject project)
  {
    if (!isACTRProject(project)) return Collections.EMPTY_LIST;

    Collection<String> validExtensions = new TreeSet<String>();

    // shortterm hack during io2 transition
    validExtensions.add("jactr");

    for (String ext : ModelParserFactory.getValidExtensions())
      validExtensions.add(ext.toLowerCase());

    ArrayList<IFile> rtn = new ArrayList<IFile>();
    findModelsOnPath(project.findMember("models"), rtn, validExtensions);
//    findModelsOnPath(project.findMember("src"), rtn, validExtensions);
    return rtn;
  }

  static protected void findModelsOnPath(IResource root,
      Collection<IFile> modelFiles, Collection<String> validExtensions)
  {
    if (!(root instanceof IFolder)) return;
    IFolder folder = (IFolder) root;
    if (!folder.exists()) return;
    try
    {
      for (IResource child : folder.members(false))
        if (child.exists()) if (child instanceof IFile)
        {
          String ext = child.getFileExtension();

          if (ext != null)
          {
            ext = ext.toLowerCase();
            if (validExtensions.contains(ext)) modelFiles.add((IFile) child);
          }
        }
        else
          findModelsOnPath(child, modelFiles, validExtensions);
    }
    catch (CoreException ce)
    {
      CorePlugin.error("Could not access " + folder, ce);
    }
  }

  static public void ensureNatureAndBuilders(IProject project)
      throws CoreException
  {
    Set<String> requiredNatures = (Set<String>) Sets.immutable.of(
        "org.eclipse.jdt.core.javanature", "org.eclipse.pde.PluginNature",
        "org.eclipse.xtext.ui.shared.xtextNature",
        "org.jactr.eclipse.core.project.actrNature");

    Set<String> requiredBuilders = (Set<String>) Sets.immutable.of(
        "org.eclipse.jdt.core.javabuilder",
        "org.eclipse.xtext.ui.shared.xtextBuilder",
        "org.jactr.eclipse.core.builder.actrBuilder");

    Set<String> definedNatures = Sets.mutable.empty();
    Set<String> definedBuilders = Sets.mutable.empty();

    IProjectDescription desc = project.getDescription();
    for (String defined : desc.getNatureIds())
      definedNatures.add(defined);

    for (ICommand defined : desc.getBuildSpec())
      definedBuilders.add(defined.getBuilderName());

    definedNatures.addAll(requiredNatures);
    definedBuilders.addAll(requiredBuilders);

    desc.setNatureIds(definedNatures.stream().toArray(String[]::new));

    desc.setBuildSpec(definedBuilders.stream().map(s -> {
      ICommand command = desc.newCommand();
      command.setBuilderName(s);
      return command;
    }).toArray(ICommand[]::new));

    project.setDescription(desc, null);
  }

}