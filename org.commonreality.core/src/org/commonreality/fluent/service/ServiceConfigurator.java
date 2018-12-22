package org.commonreality.fluent.service;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.fluent.ParticipantConfigurator;
import org.commonreality.net.protocol.IProtocolConfiguration;
import org.commonreality.net.provider.INetworkingProvider;
import org.commonreality.net.transport.ITransportProvider;

public class ServiceConfigurator<R extends ServiceConfigurator<?>>
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
      .getLog(ServiceConfigurator.class);

  private ParticipantConfigurator<?> _parent;

  protected INetworkingProvider      _provider;

  protected ITransportProvider       _transport;

  protected IProtocolConfiguration   _protocol;

  protected String                   _address;

  protected R                        _returnBuilder;

  public ServiceConfigurator(ParticipantConfigurator<?> parent)
  {
    _parent = parent;
    try
    {
      _provider = INetworkingProvider
          .getProvider("org.commonreality.netty.NettyNetworkingProvider");
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      LOGGER.error("ServiceConfigurator.ServiceConfigurator threw Exception : ",
          e);
    }
  }

  public R using(INetworkingProvider provider)
  {
    _provider = provider;
    return (R) this;
  }

  public R connectAt(String address)
  {
    _address = address;
    return (R) this;
  }

  public R transport(ITransportProvider transport)
  {
    _transport = transport;
    return (R) this;
  }

  public R protocol(IProtocolConfiguration protocol)
  {
    _protocol = protocol;
    return (R) this;
  }

  /**
   * NoOp protocol and transport, aka local memory
   * 
   * @return
   */
  public R noOp()
  {
    transport(_provider.getTransport(INetworkingProvider.NOOP_TRANSPORT));
    protocol(_provider.getProtocol(INetworkingProvider.NOOP_PROTOCOL));
    return (R) this;
  }

  public R local()
  {
    return noOp();
  }

  public R serialized()
  {
    return protocol(
        _provider.getProtocol(INetworkingProvider.SERIALIZED_PROTOCOL));
  }

  public R nio()
  {
    return transport(_provider.getTransport(INetworkingProvider.NIO_TRANSPORT));
  }

  public R remote()
  {
    serialized();
    return nio();
  }

  protected ParticipantConfigurator<?> parent()
  {
    return _parent;
  }

}
