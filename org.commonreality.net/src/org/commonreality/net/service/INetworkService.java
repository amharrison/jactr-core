package org.commonreality.net.service;

/*
 * default logging
 */
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.protocol.IProtocolConfiguration;
import org.commonreality.net.session.ISessionListener;
import org.commonreality.net.transport.ITransportProvider;

public interface INetworkService
{

  public void configure(ITransportProvider transport,
      IProtocolConfiguration protocol,
      Map<Class<?>, IMessageHandler<?>> defaultHandlers,
      ISessionListener defaultListener, ThreadFactory threadFactory);

  public SocketAddress start(SocketAddress address) throws Exception;

  public void stop(SocketAddress address) throws Exception;
}
