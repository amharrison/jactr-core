package org.jactr.eclipse.core.comp.internal;

/*
 * default logging
 */
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResource;
import org.jactr.eclipse.core.comp.IProjectCompilationUnit;

public class ProjectCompilationUnit extends AbstractCompilationUnit implements
    IProjectCompilationUnit
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ProjectCompilationUnit.class);

  final private IResource            _resource;

  public ProjectCompilationUnit(IResource resource)
  {
    super();
    _resource = resource;
  }

  @Override
  public long getSourceModificationTime()
  {
    return _resource.getLocalTimeStamp();
  }

  public URI getSource()
  {
    return _resource.getLocationURI();
  }

  public IResource getResource()
  {
    return _resource;
  }

}
