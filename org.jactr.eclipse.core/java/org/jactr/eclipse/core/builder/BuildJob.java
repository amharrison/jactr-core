package org.jactr.eclipse.core.builder;

/*
 * default logging
 */
import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.comp.IProjectCompilationUnit;
import org.jactr.eclipse.core.comp.internal.IMutableCompilationUnit;
import org.jactr.eclipse.core.compiler.CompilationUnitSchedulingRule;
import org.jactr.eclipse.core.compiler.CompileRunnable;
import org.jactr.eclipse.core.compiler.IDECompiler;
import org.jactr.eclipse.core.compiler.MarkRunnable;
import org.jactr.eclipse.core.concurrent.IRunnableWithProgress;
import org.jactr.eclipse.core.parser.IDEParserFactory;
import org.jactr.eclipse.core.parser.ParseRunnable;
import org.jactr.io.parser.IModelParser;

public class BuildJob extends Job
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory.getLog(BuildJob.class);

  private BuildRunnable              _runnable;

  public BuildJob(IMutableCompilationUnit compilationUnit, boolean force)
  {
    this(compilationUnit, null, force);
  }

  public BuildJob(IMutableCompilationUnit compilationUnit,
      IDECompiler compiler, boolean force)
  {
    super("Building " + compilationUnit.getSource().getPath());
    _runnable = new BuildRunnable(compilationUnit, compiler, force);

    setRule(new CompilationUnitSchedulingRule(compilationUnit));

    // setSystem(true);
    setSystem(false);
    setUser(true);
    // setPriority(Job.BUILD);
  }

  @Override
  protected IStatus run(IProgressMonitor monitor)
  {
    return _runnable.run(monitor);
  }

  static public class BuildRunnable implements IRunnableWithProgress
  {
    private IMutableCompilationUnit         _compilationUnit;

    private IDECompiler                     _compiler;

    private boolean                         _force = false;

    private FastList<IRunnableWithProgress> _steps = FastList.newInstance();

    public BuildRunnable(IMutableCompilationUnit compilationUnit,
        IDECompiler compiler, boolean force)
    {
      _compilationUnit = compilationUnit;
      if (compiler == null) compiler = new IDECompiler();
      _compiler = compiler;
      _force = force;

      buildSteps();
    }

    protected void buildSteps()
    {
      IModelParser parser = null;

      if (_compilationUnit instanceof IProjectCompilationUnit)
        parser = IDEParserFactory
            .getParser(((IProjectCompilationUnit) _compilationUnit)
                .getResource());
      else
        parser = IDEParserFactory.getParser(_compilationUnit.getSource());

      if (parser == null)
      {
        CorePlugin.error("Could not get parser for "
            + _compilationUnit.getSource()
            + ". Empty compilation model in use.");
        // create an empty series of steps..
        return;
      }

      _steps.add(new ParseRunnable(_compilationUnit, parser));
      _steps.add(new CompileRunnable(_compilationUnit, _compiler));
      if (_compilationUnit instanceof IProjectCompilationUnit)
        _steps
            .add(new MarkRunnable((IProjectCompilationUnit) _compilationUnit));
    }

    public IStatus run(IProgressMonitor monitor)
    {
      try
      {
        if (_compilationUnit.isFresh() && !_force) return Status.OK_STATUS;

        for (IRunnableWithProgress step : _steps)
          if (monitor.isCanceled())
            break;
          else
          {
            monitor.setTaskName(step.toString());
            step.run(monitor);
          }

        if (monitor.isCanceled()) return Status.CANCEL_STATUS;

        return Status.OK_STATUS;
      }
      finally
      {
        FastList.recycle(_steps);
      }
    }
  }
}
