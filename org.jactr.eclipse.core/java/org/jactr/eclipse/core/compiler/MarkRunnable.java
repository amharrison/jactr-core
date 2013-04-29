package org.jactr.eclipse.core.compiler;

/*
 * default logging
 */
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import javolution.util.FastList;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.ast.Support;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.core.comp.ICompilationUnitRunnable;
import org.jactr.eclipse.core.comp.IProjectCompilationUnit;
import org.jactr.eclipse.core.comp.internal.ExceptionContainer;
import org.jactr.eclipse.core.comp.internal.IMutableCompilationUnit;
import org.jactr.io.antlr3.builder.BuilderWarning;
import org.jactr.io.antlr3.compiler.CompilationInfo;
import org.jactr.io.antlr3.compiler.CompilationWarning;
import org.jactr.io.antlr3.misc.CommonTreeException;
import org.jactr.io.antlr3.misc.DetailedCommonTree;

public class MarkRunnable implements ICompilationUnitRunnable
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(MarkRunnable.class);

  private IProjectCompilationUnit    _compilationUnit;

  public MarkRunnable(IProjectCompilationUnit compilation)
  {
    _compilationUnit = compilation;
  }
  
  public ICompilationUnit getCompilationUnit()
  {
    return _compilationUnit;
  }

  @Override
  public String toString()
  {
    return "Marking " + _compilationUnit.getSource().getPath();
  }

  public IStatus run(IProgressMonitor monitor)
  {
    URL url = null;
    try
    {
      url = _compilationUnit.getSource().toURL();
    }
    catch (MalformedURLException e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("MarkRunnable.run threw MalformedURLException : ", e);
    }

    IResource resource = _compilationUnit.getResource();
    clearMarkers(resource);
    
    ExceptionContainer container = ((IMutableCompilationUnit) _compilationUnit)
        .getParseContainer();

    
    FastList<Exception> exceptions = FastList.newInstance();
    container.getInfo(exceptions);
    container.getWarnings(exceptions);
    container.getErrors(exceptions);
    markExceptions(resource, url, exceptions);
    // container.clear();
    exceptions.clear();

    container = ((IMutableCompilationUnit) _compilationUnit)
        .getCompileContainer();
    container.getInfo(exceptions);
    container.getWarnings(exceptions);
    container.getErrors(exceptions);
    markExceptions(resource, url, exceptions);

    FastList.recycle(exceptions);

    return Status.OK_STATUS;
  }
  
  protected void clearMarkers(IResource resource)
  {
    try
    {
      resource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
    }
    catch (CoreException e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("MarkRunnable.clearMarkers threw CoreException : ", e);
    }
  }

  protected void markExceptions(IResource resource, URL source,
      Collection<Exception> exceptions)
  {
    if (resource.isDerived()) return;

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Marking " + exceptions.size() + " exceptions");
    for (Exception e : exceptions)
    {
      if (source != null) // skip marking this one if it isn't local
        if (!source.equals(getSource(e)) && getSource(e) != null) continue;

      int sev = getSeverity(e);
      String message = getMessage(e, true);
      int line = getLine(e);
      int start = getStartOffset(e);
      int end = getEndOffset(e);

      if (e.getCause() != null) // notify the platform for now..
        CorePlugin.debug(message, e);
      
      if (line == -1) continue;

      try
      {
        LOGGER.debug("Error on " + resource.getName());

        String markerType = getMarkerType(e);
        IMarker marker = resource.createMarker(markerType);

        if (marker.exists()) try
        {
          // temporary marker so that we can associate exceptions with specific
          // markers - specifically used by ASTContentProvider.updateMarkers
          marker.setAttribute("exceptionID", e.hashCode());
          // marker.setAttribute(IMarker.TRANSIENT, true);
          marker.setAttribute(IMarker.SEVERITY, sev);
          marker.setAttribute(IMarker.MESSAGE, message);

          LOGGER.debug(e.getMessage() + " : " + line);

          marker.setAttribute(IMarker.LINE_NUMBER, line);

          if (start != -1)
          {
            if (end == -1) end = start + 1;
            marker.setAttribute(IMarker.CHAR_START, start);
            marker.setAttribute(IMarker.CHAR_END, end);
          }
        }
        catch (CoreException ce2)
        {
          CorePlugin.error("Could not set attribute for marker", ce2);
        }
      }
      catch (CoreException ce3)
      {
        CorePlugin.error("Could not create marker", ce3);
      }
    }
  }

  protected String getMarkerType(Exception e)
  {
    return IMarker.PROBLEM;
  }

  /**
   * @param e
   * @return
   */
  protected String getMessage(Throwable e, boolean pedantic)
  {
    // while (e.getCause() != null)
    // e = e.getCause();

    String msg = e.getMessage();
    if (msg == null || msg.length() == 0)
    {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      pw.close();
      msg = sw.getBuffer().toString();
    }

    if (pedantic) while (e != null)
    {
      LOGGER.debug("Caused by ", e);
      e = e.getCause();
    }

    /*
     * if (pedantic) { // snag the true cause.. while (e.getCause() != null) e =
     * (Exception) e.getCause(); StackTraceElement[] ste = e.getStackTrace();
     * StringBuffer stackMessage = new StringBuffer(" ["); for (int i = 0; i <
     * ste.length; i++) { stackMessage.append(ste[i].getClassName());
     * stackMessage.append("."); stackMessage.append(ste[i].getMethodName());
     * stackMessage.append(":"); stackMessage.append(ste[i].getLineNumber());
     * stackMessage.append("/ "); } stackMessage.append("]"); msg = new
     * String(msg + " " + stackMessage.toString()); }
     */
    return msg;
  }

  protected URL getSource(Exception e)
  {
    if (e instanceof CommonTreeException)
    {
      CommonTree node = ((CommonTreeException) e).getStartNode();
      if (node instanceof DetailedCommonTree)
        return ((DetailedCommonTree) node).getSource();
    }
    return null;
  }

  /**
   * @param e
   * @return
   */
  protected int getSeverity(Exception e)
  {
    int priority = IMarker.SEVERITY_ERROR;

    if (e instanceof CompilationWarning || e instanceof BuilderWarning)
      priority = IMarker.SEVERITY_WARNING;

    if (e instanceof CompilationInfo) priority = IMarker.SEVERITY_INFO;

    return priority;
  }

  protected int getLine(Exception e)
  {
    int line = -1;

    if (e instanceof RecognitionException)
      line = ((RecognitionException) e).line;
    else if (e instanceof CommonTreeException)
    {
      CommonTree node = ((CommonTreeException) e).getStartNode();
      if (node != null) line = node.getLine();
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Returning line  " + line + " for ", e);

    return line;
  }

  protected int getStartOffset(Exception e)
  {
    int start = -1;
    if (e instanceof RecognitionException)
    {
      CommonToken tmp = (CommonToken) ((RecognitionException) e).token;
      if (tmp != null) start = tmp.getStartIndex();
    }
    else if (e instanceof CommonTreeException)
    {
      CommonTree node = ((CommonTreeException) e).getStartNode();
      if (node != null)
      {
        int[] span = Support.getTreeOffsets(node, null);
        if (span != null) start = span[0];
      }
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Returning start position " + start + " for ", e);
    return start;
  }

  protected int getEndOffset(Exception e)
  {
    int end = -1;
    if (e instanceof RecognitionException)
    {
      CommonToken tmp = (CommonToken) ((RecognitionException) e).token;
      if (tmp != null) end = tmp.getStopIndex();
    }
    else if (e instanceof CommonTreeException)
    {
      CommonTree node = ((CommonTreeException) e).getStartNode();
      if (node != null)
      {
        int[] span = Support.getTreeOffsets(node, null);
        if (span != null) end = span[1] + 1;
      }
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Returning end position " + end + " for ", e);
    return end;
  }
}
