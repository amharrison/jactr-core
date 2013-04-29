package org.jactr.eclipse.core.parser;

/*
 * default logging
 */
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.jactr.eclipse.core.CorePlugin;
import org.jactr.io.parser.IModelParser;
import org.jactr.io.parser.ModelParserFactory;

public class IDEParserFactory
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(IDEParserFactory.class);

  
  static public IModelParser getParser(IResource resource)
  {
    try
    {
      IModelParser parser = ModelParserFactory.getModelParser(resource
          .getLocationURI().toURL());

      ProjectSensitiveParserImportDelegate delegate = new ProjectSensitiveParserImportDelegate();
      delegate.setProject(resource.getProject());

      parser.setImportDelegate(delegate);

      return parser;
    }
    catch (Exception e)
    {
      CorePlugin.error("Could not create parser for " + resource, e);
      return null;
    }
  }

  static public IModelParser getParser(URI uri)
  {
    try
    {
      IModelParser parser = ModelParserFactory.getModelParser(uri.toURL());
      return parser;
    }
    catch (Exception e)
    {
      CorePlugin.error("Could not create parser for " + uri, e);
      return null;
    }
  }
}
