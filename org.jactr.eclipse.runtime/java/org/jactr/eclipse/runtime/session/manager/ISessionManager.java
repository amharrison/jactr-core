package org.jactr.eclipse.runtime.session.manager;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.jactr.eclipse.runtime.session.ISession;

public interface ISessionManager
{

  public Set<ISession> getSessions(Set<ISession> container);

  public ISession getSession(UUID sessionId);

  public void addListener(ISessionManagerListener listener, Executor executor);

  public void removeListener(ISessionManagerListener listener);

  public Collection<ISessionManagerListener> getListeners();
}
