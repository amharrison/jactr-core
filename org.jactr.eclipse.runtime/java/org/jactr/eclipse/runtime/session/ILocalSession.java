package org.jactr.eclipse.runtime.session;

/*
 * default logging
 */
import java.net.URI;

import org.eclipse.debug.core.ILaunch;

public interface ILocalSession extends ISession
{

  public URI getWorkingDirectory();

  public ILaunch getLaunch();
}
