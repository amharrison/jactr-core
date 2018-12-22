package org.commonreality.netty;

/*
 * default logging
 */
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.protocol.IProtocolConfiguration;
import org.commonreality.net.provider.INetworkingProvider;
import org.commonreality.net.service.IClientService;
import org.commonreality.net.service.IServerService;
import org.commonreality.net.transport.ITransportProvider;
import org.commonreality.netty.protocol.NOOPProtocol;
import org.commonreality.netty.protocol.SerializingProtocol;
import org.commonreality.netty.service.ClientService;
import org.commonreality.netty.service.ServerService;
import org.commonreality.netty.transport.LocalTransportProvider;
import org.commonreality.netty.transport.NIOTransportProvider;

public class NettyNetworkingProvider implements INetworkingProvider
{
  /**
   * Logger definition
   */
  static private final transient Log                LOGGER               = LogFactory
                                                                             .getLog(NettyNetworkingProvider.class);

  final private Map<String, ITransportProvider>     _availableTransports = new TreeMap<String, ITransportProvider>();

  final private Map<String, IProtocolConfiguration> _availableProtocols  = new TreeMap<String, IProtocolConfiguration>();

  public NettyNetworkingProvider()
  {
    _availableTransports.put(NIO_TRANSPORT, new NIOTransportProvider());
    _availableTransports.put(NOOP_TRANSPORT, new LocalTransportProvider());

    _availableProtocols.put(NOOP_PROTOCOL, new NOOPProtocol());
    _availableProtocols.put(SERIALIZED_PROTOCOL, new SerializingProtocol());
  }

  @Override
  public IServerService newServer()
  {
    return new ServerService();
  }

  @Override
  public IClientService newClient()
  {
    return new ClientService();
  }

  @Override
  public IProtocolConfiguration getProtocol(String protocolType)
  {
    return _availableProtocols.get(protocolType);
  }

  @Override
  public ITransportProvider getTransport(String transportType)
  {
    return _availableTransports.get(transportType);
  }

}
