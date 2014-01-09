package org.jactr.eclipse.core.parser;

/*
 * default logging
 */
import java.net.URL;
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
import org.jactr.io.parser.IModelParser;

public class ParseRunnable implements ICompilationUnitRunnable
{
  /**
   * Logger definition
   */
  static private final transient Log    LOGGER = LogFactory
                                                   .getLog(ParseRunnable.class);

  private final IModelParser            _modelParser;

  private final IMutableCompilationUnit _compilationUnit;

  public ParseRunnable(IMutableCompilationUnit compilationUnit,
      IModelParser parser)
  {
    _compilationUnit = compilationUnit;
    _modelParser = parser;
  }

  @Override
  public String toString()
  {
    return "Parsing " + _compilationUnit.getSource().getPath();
  }

  public ICompilationUnit getCompilationUnit()
  {
    return _compilationUnit;
  }

  public IStatus run(IProgressMonitor monitor)
  {
    if (monitor.isCanceled()) return Status.CANCEL_STATUS;

    FastList<Exception> tmp = FastList.newInstance();

    try
    {
      _modelParser.reset();

      /*
       * make sure the project is specified before attempting..
       */
      ProjectSensitiveParserImportDelegate importDelegate = (ProjectSensitiveParserImportDelegate) _modelParser
          .getImportDelegate();
      if (importDelegate != null
          && _compilationUnit instanceof IProjectCompilationUnit)
        importDelegate.setProject(((IProjectCompilationUnit) _compilationUnit)
            .getResource().getProject());

      if (LOGGER.isDebugEnabled()) LOGGER.debug("Parse started");

      _modelParser.parse();

      ExceptionContainer container = _compilationUnit.getParseContainer();
      container.clear();

      tmp.addAll(_modelParser.getParseWarnings());
      Collections.reverse(tmp);
      container.addWarnings(tmp);

      tmp.clear();
      tmp.addAll(_modelParser.getParseErrors());
      Collections.reverse(tmp);
      container.addErrors(tmp);

      if (LOGGER.isDebugEnabled()) LOGGER.debug("Parse compelted " + tmp);

      CommonTree oldDescriptor = _compilationUnit.getModelDescriptor();
      CommonTree descriptor = _modelParser.getDocumentTree();

      /*
       * if we have no old desc, use the new one no matter what
       */
      if (oldDescriptor == null)
        _compilationUnit.setModelDescriptor(descriptor);

      for (URL importedFrom : _modelParser.getImportDelegate()
          .getImportSources())
        try
        {
          _compilationUnit.addImportSource(importedFrom.toURI());
        }
        catch (Exception e)
        {

        }

      if (tmp.size() == 0)
      {
        _compilationUnit.setModelDescriptor(descriptor);
        return Status.OK_STATUS;
      }

      return new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,
          "Errors in parse ", tmp.get(0));
    }
    catch (CanceledException ce)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Parse canceled");
      return Status.CANCEL_STATUS;
    }
    finally
    {
      FastList.recycle(tmp);
    }
  }

}
