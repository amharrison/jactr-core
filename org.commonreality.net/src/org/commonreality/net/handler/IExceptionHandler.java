package org.commonreality.net.handler;

/*
 * default logging
 */
import org.commonreality.net.session.ISessionInfo;

@FunctionalInterface
public interface IExceptionHandler
{

  public boolean exceptionCaught(ISessionInfo<?> session, Throwable thrown);
}
