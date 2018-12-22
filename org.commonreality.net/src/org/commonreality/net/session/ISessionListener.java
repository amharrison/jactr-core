package org.commonreality.net.session;

/*
 * default logging
 */

public interface ISessionListener
{

  public void opened(ISessionInfo<?> session);

  public void closed(ISessionInfo<?> session);

  public void created(ISessionInfo<?> session);

  public void destroyed(ISessionInfo<?> session);

  // public void idle(ISessionInfo session);

}
