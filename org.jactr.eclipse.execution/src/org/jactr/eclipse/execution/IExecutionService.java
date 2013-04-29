package org.jactr.eclipse.execution;

import java.util.Collection;
import java.util.concurrent.Executor;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;

public interface IExecutionService
{

  public void addListener(IExecutionServiceListener listener, Executor executor);

  public void removeListener(IExecutionServiceListener listener);

  public void getSessions(Collection<IExecutionSession> container);

  public IExecutionSession submit(IProject project,
      ILaunchConfiguration launchConfiguration, Object... parameters)
      throws Exception;
}
