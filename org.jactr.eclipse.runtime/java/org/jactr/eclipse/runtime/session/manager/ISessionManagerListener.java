package org.jactr.eclipse.runtime.session.manager;

/*
 * default logging
 */
import org.jactr.eclipse.runtime.session.ISession;

public interface ISessionManagerListener
{

  public void sessionAdded(ISession session);

  public void sessionRemoved(ISession session);
}
