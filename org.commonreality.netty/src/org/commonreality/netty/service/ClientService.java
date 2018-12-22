package org.commonreality.netty.service;

/*
 * default logging
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.protocol.IProtocolConfiguration;
import org.commonreality.net.service.IClientService;
import org.commonreality.net.session.ISessionListener;
import org.commonreality.net.transport.ITransportProvider;
import org.commonreality.netty.impl.NettyListener;
import org.commonreality.netty.impl.NettyMultiplexer;
import org.commonreality.netty.transport.NettyConfig;

public class ClientService extends AbstractNettyNetworkService implements
    IClientService
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ClientService.class);

  private SocketAddress              _connectedTo;

  private Channel                    _activeChannel;

  private Bootstrap                  _bootstrap;

  @SuppressWarnings("unchecked")
  @Override
  public void configure(ITransportProvider transport,
      IProtocolConfiguration protocol,
      Map<Class<?>, IMessageHandler<?>> defaultHandlers,
      ISessionListener defaultListener, ThreadFactory threadFactory)
  {
    _multiplexer = createMultiplexer(defaultHandlers);

    NettyConfig config = (NettyConfig) transport.configureClient();

    _workerGroup = config.getClientSupplier().apply(1, threadFactory);

    _bootstrap = new Bootstrap();
    _bootstrap.group(_workerGroup);
    _bootstrap.channel(config.getClientClass());
    // _bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    _bootstrap.handler(new ChannelInitializer<Channel>() {

      @Override
      protected void initChannel(Channel ch) throws Exception
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("New connection"));

        protocol.configure(ch);

        _activeChannel = ch;
        _connectedTo = ch.remoteAddress();


        /*
         * add our multiplexer
         */
        ch.pipeline().addLast(_multiplexer.getClass().getName(),
            new NettyMultiplexer(_multiplexer));

        /*
         * this is moved to later so that when the close/unregister comes we've
         * already done all of our normal processing
         */

        if (defaultListener != null)
          ch.pipeline().addLast("defaultListener",
              new NettyListener(defaultListener));
      }
    });

  }

  @Override
  public SocketAddress start(SocketAddress address) throws Exception
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Connecting"));

    ChannelFuture future = _bootstrap.connect(address).sync();
    future.awaitUninterruptibly();
    // listening on will be set by the server handler.
    return _connectedTo;
  }

  @Override
  public void stop(SocketAddress address) throws Exception
  {
    try
    {
      ChannelFuture future = _activeChannel.closeFuture();
      future.addListener(new GenericFutureListener<Future<? super Void>>() {

        @Override
        public void operationComplete(Future<? super Void> arg0)
            throws Exception
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Shutting down executors"));
          _workerGroup.shutdownGracefully();
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Shut down executors"));
        }
      });

      if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Closing"));
      _activeChannel.close();

      if (!future.await(500, TimeUnit.MILLISECONDS))
        throw new RuntimeException("Timed out waiting for close");

      if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Closed"));
    }
    catch (Exception e)
    {
      LOGGER.error(
          String.format("Failed to cleanly close [%s]", _activeChannel), e);
    }

  }

}
