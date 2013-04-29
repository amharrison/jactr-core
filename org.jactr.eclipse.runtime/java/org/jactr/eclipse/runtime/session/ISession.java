package org.jactr.eclipse.runtime.session;

/*
 * default logging
 */
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.jactr.eclipse.runtime.session.control.ISessionController;
import org.jactr.eclipse.runtime.session.data.ISessionData;

public interface ISession
{
  public UUID getSessionId();

  public ISessionController getController();

  public Set<String> getKeys(Set<String> container);

  public ISessionData getData(String key);

  public void addData(String key, ISessionData sessionData);

  public void addListener(ISessionListener listener, Executor executor);

  public void removeListener(ISessionListener listener);

  public Set<String> getMetaDataKeys(Set<String> container);

  public Object getMetaData(String key);

  public Object setMetaData(String key, Object value);

  /**
   * return true if this session is either running or otherwise active. That is,
   * we can expect there to be data coming along the pipe
   * 
   * @return
   */
  public boolean isOpen();

  /**
   * close the session, terminating the run if possible.
   */
  public void close();

  /**
   * release all resources and remove from the session manager
   */
  public void destroy();

  public boolean hasBeenDestroyed();

  public Date getTimeOfExecution();
}
