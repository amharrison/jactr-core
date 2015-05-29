/*
 * Created on Mar 28, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.jactr.eclipse.ui.wizards.templates;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.ui.templates.OptionTemplateSection;
import org.eclipse.pde.ui.templates.PluginReference;
import org.jactr.eclipse.core.bundles.meta.ManifestTools;
import org.jactr.eclipse.core.project.ACTRProjectUtils;
import org.jactr.eclipse.ui.UIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;


public abstract class BaseACTRContributorTemplateSection extends
    OptionTemplateSection
{

  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(BaseACTRContributorTemplateSection.class);

  public BaseACTRContributorTemplateSection()
  {
    super();
  }

  @Override
  protected ResourceBundle getPluginResourceBundle()
  {
    Bundle bundle = Platform.getBundle(UIPlugin.ID);
    return Platform.getResourceBundle(bundle);
  }

  @Override
  protected URL getInstallURL()
  {
    return UIPlugin.getDefault().getBundle().getEntry("/");
  }

  @Override
  protected String getTemplateDirectory()
  {
    return "code_templates";
  }

  @Override
  public void initializeFields(IPluginModelBase model)
  {

  }

  public String[] getNewFiles()
  {
    return new String[0];
  }

  protected void addClasspathEntries(Collection<String> entries,
      boolean asSource) throws CoreException
  {
    IJavaProject javaProject = JavaCore.create(project);

    IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
    IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length
        + entries.size()];

    /*
     * first old entry
     */
    newEntries[0] = oldEntries[0];
    
    System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);

    Iterator<String> itr = entries.iterator();
    for (int i = oldEntries.length; itr.hasNext(); i++)
    {
      IPath path = project.getFullPath().append(itr.next());
      if (asSource) newEntries[i] = JavaCore.newSourceEntry(path);
      else newEntries[i] = JavaCore.newLibraryEntry(path, null, null, true);
    }

    javaProject.setRawClasspath(newEntries, null);
  }

  protected void addSourceFolders(Collection<String> folders)
      throws CoreException
  {
    addClasspathEntries(folders, true);

    /**
     * this ensures that all the class path entries we've added are included in
     * the packaged jar file <br>
     * this is still necessary since model.getBuildModel() will normally return
     * null..
     */

    WorkspaceBuildModel model = new WorkspaceBuildModel(project
        .getFile("build.properties"));

    // default jar
    IBuildEntry root = model.getBuild().getEntry(IBuildEntry.JAR_PREFIX + ".");

    for (String folder : folders)
      if (!root.contains(folder)) root.addToken(folder);

    model.save();
    model.dispose();
  }

  /**
   * returns a collection of the basic actr plugin references
   * 
   * @return
   */
  protected Collection<IPluginReference> getDefaultPluginReferences()
  {
    ArrayList<IPluginReference> rtn = new ArrayList<IPluginReference>();
    /*
     * access to the core
     */
    rtn.add(new PluginReference("org.jactr", null, 0));
    /*
     * and can be accessed from io (needed for the buddy policy stuff)
     */
    rtn.add(new PluginReference("org.jactr.io", null, 0));
    /*
     * so that they get the default tools
     */
    rtn.add(new PluginReference("org.jactr.tools", null, 0));
    /*
     * basic logging.. this may change org.apache.commons.logging official
     * package is still pending..
     */
    rtn.add(new PluginReference("org.jactr.support", null, 0));

    rtn.add(new PluginReference("org.commonreality.api", null, 0));
    rtn.add(new PluginReference("org.commonreality.core", null, 0));
    rtn.add(new PluginReference("org.commonreality.time", null, 0));
    rtn.add(new PluginReference("org.commonreality.sensors", null, 0));
    rtn.add(new PluginReference("org.commonreality.modalities", null, 0));

    rtn.add(new PluginReference("org.apache.log4j", null, 0));
    return rtn;
  }

  @Override
  public IPluginReference[] getDependencies(String schemaVersion)
  {
    ArrayList<IPluginReference> plugins = new ArrayList<IPluginReference>();
    plugins.addAll(getDefaultPluginReferences());

    return plugins.toArray(new IPluginReference[plugins.size()]);
  }

  /**
   * will use the IPluginBaseModel to try to set Eclipse-RegisterBuddy in the
   * header file which allows the required plugins to access the classes in this
   * package..
   * 
   * @param additionalBuddies
   */
  protected void addEclipseBuddies(Collection<String> additionalBuddies)
      throws CoreException
  {
    if (!ManifestTools.addEclipseBuddies(model, additionalBuddies))
      throw new CoreException(new Status(IStatus.ERROR, UIPlugin.ID,
          "model info is " + model.getClass().getName()
              + " cannot set buddy info"));
  }

  protected void exportPackages(Collection<String> packagesToExport)
      throws CoreException
  {
    /**
     * need this to ensure that everything is exported
     */
    if (model instanceof IBundlePluginModelBase)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER
            .debug("we have access to the bundle information, attempting to update");
      IBundle bundle = ((IBundlePluginModelBase) model).getBundleModel()
          .getBundle();

      
      String oldExports = bundle.getHeader(Constants.EXPORT_PACKAGE);
      StringBuilder packages = new StringBuilder(oldExports == null ? ""
          : oldExports);

      if (packages.length() != 0) packages.append(",");

      for (String packageToExport : packagesToExport)
        packages.append(packageToExport).append(",");

      if (packages.length() > 1)
        packages.delete(packages.length() - 1, packages.length());

      bundle.setHeader(Constants.EXPORT_PACKAGE, packages.toString());
    }
    else
      throw new CoreException(new Status(IStatus.ERROR, UIPlugin.ID,
          "model info is " + model.getClass().getName()
              + " cannot set export info"));
  }

  /**
   * adds the project nature..
   * 
   * @throws CoreException
   */
  protected void addProjectNature() throws CoreException
  {
    ACTRProjectUtils.addNature(project);
  }
}
