package org.jactr.eclipse.runtime.launching.session;

/*
 * default logging
 */

public interface ISessionListener<T extends AbstractSession>
{
  

  public void sessionOpened(T session);
  
  public void sessionClosed(T session, boolean normal);
  
  public void sessionDestroyed(T session);
}
