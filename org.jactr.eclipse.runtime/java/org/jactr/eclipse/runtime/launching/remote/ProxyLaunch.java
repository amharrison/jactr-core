package org.jactr.eclipse.runtime.launching.remote;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;

public class ProxyLaunch implements ILaunch
{
  /**
  * Logger definition
  */
  static private final transient Log LOGGER = LogFactory
      .getLog(ProxyLaunch.class);

  public ProxyLaunch()
  {
    // TODO Auto-generated constructor stub
  }

  @Override
  public boolean canTerminate()
  {
    return true;
  }

  @Override
  public boolean isTerminated()
  {
    return false;
  }

  @Override
  public void terminate() throws DebugException
  {

  }

  @Override
  public <T> T getAdapter(Class<T> adapter)
  {
    return null;
  }

  @Override
  public Object[] getChildren()
  {
    return new Object[0];
  }

  @Override
  public IDebugTarget getDebugTarget()
  {
    return null;
  }

  @Override
  public IProcess[] getProcesses()
  {
    return new IProcess[0];
  }

  @Override
  public IDebugTarget[] getDebugTargets()
  {
    return new IDebugTarget[0];
  }

  @Override
  public void addDebugTarget(IDebugTarget target)
  {

  }

  @Override
  public void removeDebugTarget(IDebugTarget target)
  {

  }

  @Override
  public void addProcess(IProcess process)
  {

  }

  @Override
  public void removeProcess(IProcess process)
  {

  }

  @Override
  public ISourceLocator getSourceLocator()
  {

    return null;
  }

  @Override
  public void setSourceLocator(ISourceLocator sourceLocator)
  {

  }

  @Override
  public String getLaunchMode()
  {
    return ILaunchManager.RUN_MODE;
  }

  @Override
  public ILaunchConfiguration getLaunchConfiguration()
  {
    return null;
  }

  @Override
  public void setAttribute(String key, String value)
  {


  }

  @Override
  public String getAttribute(String key)
  {
    return null;
  }

  @Override
  public boolean hasChildren()
  {
    return false;
  }

}
