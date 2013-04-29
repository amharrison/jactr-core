package org.jactr.eclipse.core.comp.internal;

/*
 * default logging
 */
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.eclipse.core.comp.ICompilationUnit;
import org.jactr.eclipse.core.comp.ICompilationUnitListener;

/**
 * compilation unit for items that are loaded from a url (i.e. jar'ed
 * resources). The source modification time is current time until such time as
 * the resource is cleanly compiled.
 * 
 * @author harrison
 */
public class URLCompilationUnit extends AbstractCompilationUnit
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(URLCompilationUnit.class);

  private final URL                  _resource;
  private long                       _cleanCompileTime = -1;

  public URLCompilationUnit(URL resource)
  {
    _resource = resource;
    addListener(new ICompilationUnitListener() {

      public void updated(ICompilationUnit compilationUnit)
      {
        if (compilationUnit.isCompileClean())
          _cleanCompileTime = getModificationTime();
      }
    });
  }

  @Override
  public long getSourceModificationTime()
  {
    if (_cleanCompileTime < 0) return System.currentTimeMillis();
    return _cleanCompileTime;
  }

  public URI getSource()
  {
    try
    {
      return _resource.toURI();
    }
    catch (URISyntaxException e)
    {
      CorePlugin.error(
          "URLCompilationUnit.getSource threw URISyntaxException : ", e);
      return null;
    }
  }


}
