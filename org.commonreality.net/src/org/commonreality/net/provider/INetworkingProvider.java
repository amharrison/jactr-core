package org.commonreality.net.provider;

import java.util.Map;
import java.util.TreeMap;

import org.commonreality.net.protocol.IProtocolConfiguration;
import org.commonreality.net.service.IClientService;
import org.commonreality.net.service.IServerService;
import org.commonreality.net.transport.ITransportProvider;

/*
 * default logging
 */

public interface INetworkingProvider
{
  static public final String SERIALIZED_PROTOCOL = "protocol.serialized";

  static public final String NOOP_PROTOCOL       = "protocol.noop";

  static public final String NOOP_TRANSPORT      = "transport.noop";

  static public final String NIO_TRANSPORT       = "transport.nio";

  public IServerService newServer();

  public IClientService newClient();

  public IProtocolConfiguration getProtocol(String protocolType);

  public ITransportProvider getTransport(String transportType);


  /**
   * Get an existing instance of a netowkring provider, creating if absent.
   * 
   * @param className
   * @return
   * @throws ClassNotFoundException
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  static INetworkingProvider getProvider(String className) throws Exception
  {
    synchronized (_knownProviders)
    {
      INetworkingProvider provider = _knownProviders.get(className);
      if (provider != null) return provider;

      Class c = INetworkingProvider.class.getClassLoader().loadClass(className);
      provider = (INetworkingProvider) c.newInstance();
      _knownProviders.put(className, provider);
      return provider;
    }
  }

  static final Map<String, INetworkingProvider> _knownProviders = new TreeMap<String, INetworkingProvider>();
}
