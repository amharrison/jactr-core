package org.jactr.eclipse.core.compiler;

/*
 * default logging
 */
import java.util.Collections;

import javolution.util.FastList;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.core.comp.ICompilationUnitRunnable;
import org.jactr.eclipse.core.comp.IProjectCompilationUnit;
import org.jactr.eclipse.core.comp.internal.ExceptionContainer;
import org.jactr.eclipse.core.comp.internal.IMutableCompilationUnit;
import org.jactr.io.parser.CanceledException;

public class CompileRunnable implements ICompilationUnitRunnable
{
  /**
   * Logger definition
   */
  static private final transient Log    LOGGER = LogFactory
                                                   .getLog(CompileRunnable.class);

  private final IDECompiler             _compiler;

  private final IMutableCompilationUnit _compilationUnit;

  public CompileRunnable(IMutableCompilationUnit compUnit, IDECompiler compiler)
  {
    _compilationUnit = compUnit;
    if (compiler == null) compiler = new IDECompiler();
    _compiler = compiler;
  }
  
  @Override
  public String toString()
  {
    return "Compiling " + _compilationUnit.getSource().getPath();
  }

  public ICompilationUnit getCompilationUnit()
  {
    return _compilationUnit;
  }

  public IStatus run(IProgressMonitor monitor)
  {
    if (monitor.isCanceled()) return Status.CANCEL_STATUS;

    CommonTree modelDesc = _compilationUnit.getModelDescriptor();
    if (modelDesc == null)
      return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
          "Null model descriptor");

    if (_compilationUnit instanceof IProjectCompilationUnit)
      _compiler.setProject(((IProjectCompilationUnit) _compilationUnit)
          .getResource().getProject());

    FastList<Exception> info = FastList.newInstance();
    FastList<Exception> warn = FastList.newInstance();
    FastList<Exception> error = FastList.newInstance();

    ExceptionContainer container = _compilationUnit.getCompileContainer();

    try
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Compile started");
      
      _compiler.compile(modelDesc, info, warn, error,
          new CancelableTreeNodeStream(modelDesc, monitor));

      container.clear();
      Collections.reverse(info);
      Collections.reverse(warn);
      Collections.reverse(error);

      container.addErrors(error);
      container.addInfo(info);
      container.addWarnings(warn);
      
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Compile completed");

      if (error.size() == 0) return Status.OK_STATUS;

      return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
          "Errors in compile ", error.get(0));
    }
    catch (CanceledException e)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Compile canceled");
      return Status.CANCEL_STATUS;
    }
    finally
    {
      _compiler.setProject(null);
      FastList.recycle(info);
      FastList.recycle(warn);
      FastList.recycle(error);
    }
  }

}
