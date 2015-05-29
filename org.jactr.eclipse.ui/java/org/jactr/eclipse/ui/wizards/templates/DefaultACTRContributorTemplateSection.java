/*
 * Created on Mar 23, 2007 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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

import java.util.Arrays;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.build.IBuildModel;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.ibundle.IManifestHeader;
import org.eclipse.pde.internal.core.text.bundle.BundleSymbolicNameHeader;
import org.eclipse.pde.ui.templates.TemplateOption;
import org.osgi.framework.Constants;

/**
 * basic template that contributes a jACT-R Module
 */
public class DefaultACTRContributorTemplateSection extends
    BaseACTRContributorTemplateSection
{
  /**
   * Logger definition
   */

  static private final transient Log LOGGER = LogFactory
                                                .getLog(DefaultACTRContributorTemplateSection.class);

  public DefaultACTRContributorTemplateSection()
  {
  }

  @Override
  protected void updateModel(IProgressMonitor monitor) throws CoreException
  {
    addProjectNature();

    addEclipseBuddies(Arrays.asList("org.jactr", "org.jactr.io",
        "org.jactr.support", "org.jactr.tools", "org.commonreality.core",
        "org.apache.log4j", "org.apache.commons.logging"));

    /*
     * add the classpaths for models/, configurations/
     */
    addSourceFolders(Arrays.asList("models/", "configuration/"));

    /*
     * this is just cosmetic..
     */
    sortEntries();

    IBuildModel bModel = model.getBuildModel();
    if (bModel == null)
      LOGGER.warn("null build model");
    else
      LOGGER.warn("Non null build model");

    if (bModel instanceof IEditableModel) ((IEditableModel) bModel).save();

    /*
     * one last tidbit.. to ensure that we are a singleton, regardless of the
     * presence of extensions.. copied from
     * org.eclipse.pde.internal.core.bundle.BundlePluginBase
     */
    if (model instanceof IBundlePluginModelBase)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER
            .debug("we have access to the bundle information, attempting to update");
      IBundle bundle = ((IBundlePluginModelBase) model).getBundleModel()
          .getBundle();

      IManifestHeader header = bundle
          .getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);
      if (header instanceof BundleSymbolicNameHeader)
        ((BundleSymbolicNameHeader) header).setSingleton(true);
      else
      {
        String version = bundle.getHeader(Constants.BUNDLE_MANIFESTVERSION);
        if (version == null) version = "1"; //$NON-NLS-1$
        String value = header.getValue();
        String singletonValue = null;
        if (version != null && Integer.parseInt(version) >= 2)
          singletonValue = Constants.SINGLETON_DIRECTIVE + ":=true"; //$NON-NLS-1$
        else
          singletonValue = Constants.SINGLETON_DIRECTIVE + "=true"; //$NON-NLS-1$
        if (value.indexOf(singletonValue) == -1)
          bundle.setHeader(Constants.BUNDLE_SYMBOLICNAME, value
              + "; " + singletonValue); //$NON-NLS-1$
      }
    }

  }

  /**
   * stupid cosmetic bit to reorder the classpath entries so that models/,
   * java/, conf/ appear first
   * 
   * @throws CoreException
   */
  private void sortEntries() throws CoreException
  {
    IJavaProject javaProject = JavaCore.create(project);

    IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
    SortedMap<String, IClasspathEntry> sortedMap = new TreeMap<String, IClasspathEntry>(
        Collections.reverseOrder());

    for (IClasspathEntry entry : oldEntries)
    {
      String name = entry.getPath().lastSegment();
      if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE
          && entry.getEntryKind() != IClasspathEntry.CPE_LIBRARY)
        name = "a" + name;
      sortedMap.put(name, entry);
    }

    oldEntries = sortedMap.values().toArray(oldEntries);
    javaProject.setRawClasspath(oldEntries, null);
  }

  @Override
  public String getSectionId()
  {
    return "default";
  }

  @Override
  public String getLabel()
  {
    return "Contributes basic jACT-R Content";
  }

  @Override
  public String[] getNewFiles()
  {
    return new String[] { "jactr-log.xml", "libs/", "configuration/",
        "plugin.xml", "models/" };
  }

  public String getUsedExtensionPoint()
  {
    return null;
  }

  @Override
  public void validateOptions(TemplateOption changed)
  {
  }

}
