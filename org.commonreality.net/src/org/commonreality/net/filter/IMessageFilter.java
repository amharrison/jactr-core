package org.commonreality.net.filter;

import org.commonreality.net.session.ISessionInfo;

/*
 * default logging
 */

public interface IMessageFilter
{
  public boolean accept(ISessionInfo<?> sessionInfo, Object message);
}
