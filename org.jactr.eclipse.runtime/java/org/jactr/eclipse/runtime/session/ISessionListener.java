package org.jactr.eclipse.runtime.session;

import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;

/*
 * default logging
 */

public interface ISessionListener
{

  public void sessionClosed(ISession session);

  public void sessionDestroyed(ISession session);

  public void newSessionData(ISessionData sessionData);

  public void newSessionDataStream(ISessionData sessionData,
      ISessionDataStream sessionDataStream);
}
