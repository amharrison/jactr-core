package org.commonreality.net.transport;

import java.net.SocketAddress;

/*
 * default logging
 */

public interface ITransportProvider
{

  /**
   * configure the session
   * 
   * @param session
   * @return
   */
  public Object configureServer();

  public Object configureClient();

  public SocketAddress createAddress(Object... address);
}
