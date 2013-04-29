package org.jactr.eclipse.core.compiler;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.bundles.descriptors.UnitCompilerDescriptor;
import org.jactr.eclipse.core.bundles.registry.UnitCompilerRegistry;
import org.jactr.eclipse.core.parser.IProjectSensitive;
import org.jactr.io.compiler.ClassVerifyingUnitCompiler;
import org.jactr.io.compiler.DefaultCompiler;
import org.jactr.io.compiler.IReportableUnitCompiler;
import org.jactr.io.compiler.IUnitCompiler;

public class IDECompiler extends DefaultCompiler
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(IDECompiler.class);

  private final Collection<IProjectSensitive> _resourceUnitCompilers;

  public IDECompiler()
  {
    super();
    _resourceUnitCompilers = new ArrayList<IProjectSensitive>();

    /*
     * remove the default class verifying compiler
     */
    for (IUnitCompiler unit : getCompilers())
      if (unit instanceof ClassVerifyingUnitCompiler) removeCompiler(unit);

    /*
     * and replace it with ours..
     */
    ProjectSensitiveClassVerifyingUnitCompiler verifyingCompiler = new ProjectSensitiveClassVerifyingUnitCompiler();
    _resourceUnitCompilers.add(verifyingCompiler);
    addCompiler(verifyingCompiler);
    
    addEnvironmentCompilers();
  }

  @Override
  public void addCompiler(IUnitCompiler compiler)
  {
    super.addCompiler(compiler);
    if (compiler instanceof IProjectSensitive)
      _resourceUnitCompilers.add((IProjectSensitive) compiler);
  }

  @Override
  public void removeCompiler(IUnitCompiler compiler)
  {
    super.removeCompiler(compiler);
    if (compiler instanceof IProjectSensitive)
      _resourceUnitCompilers.remove(compiler);
  }
  
  protected void addEnvironmentCompilers()
  {
    Preferences prefs = CorePlugin.getDefault().getPluginPreferences();

    for (UnitCompilerDescriptor descriptor : UnitCompilerRegistry.getRegistry()
        .getAllDescriptors())
      if (!descriptor.isInWorkspace())
      {
        String className = descriptor.getClassName();
        // make sure the default value is available
        prefs.setDefault(className + ".enabled", descriptor.isDefaultEnabled());
        prefs.setDefault(className + ".level", descriptor.getReportLevel()
            .toString());

        boolean enable = prefs.getBoolean(className + ".enabled");
        IReportableUnitCompiler.Level level = IReportableUnitCompiler.Level.IGNORE;
        try
        {
          level = IReportableUnitCompiler.Level.valueOf(prefs
              .getString(className + ".level"));
        }
        catch (Exception e)
        {
          //
        }

        if (enable)
          try
          {
            IUnitCompiler unit = (IUnitCompiler) descriptor.instantiate();
            if (unit instanceof IReportableUnitCompiler)
              ((IReportableUnitCompiler) unit).setReportLevel(level);

            if (unit != null) addCompiler(unit);
          }
          catch (CoreException e)
          {
            CorePlugin.log(e.getStatus());
            LOGGER.error("Could not create unit compiler "
                + descriptor.getName(), e);
          }
      }
  }
  
  public void setProject(IProject project)
  {
    for (IProjectSensitive resComp : _resourceUnitCompilers)
      resComp.setProject(project);
  }
  
}
