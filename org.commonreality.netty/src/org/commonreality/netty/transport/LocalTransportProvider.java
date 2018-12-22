package org.commonreality.netty.transport;

/*
 * default logging
 */
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalServerChannel;

import java.net.SocketAddress;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.transport.ITransportProvider;

public class LocalTransportProvider implements ITransportProvider
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(LocalTransportProvider.class);

  public LocalTransportProvider()
  {

  }

  @Override
  public Object configureServer()
  {
    return new NettyConfig(
        LocalServerChannel.class,
        (n, tf) -> {
          return new DefaultEventLoopGroup(1, Executors
              .newSingleThreadExecutor(tf));
        },
        null,
        (n, tf) -> {
          if (n == 1)
            return new DefaultEventLoop(Executors.newSingleThreadExecutor(tf));

          return new DefaultEventLoopGroup(n, Executors.newCachedThreadPool(tf));
        });
  }

  @Override
  public Object configureClient()
  {
    return new NettyConfig(null, null, LocalChannel.class, (n, tf) -> {
      if (n == 1)
        return new DefaultEventLoop(Executors.newSingleThreadExecutor(tf));

      return new DefaultEventLoopGroup(n, Executors.newCachedThreadPool(tf));
    });
  }

  public SocketAddress createAddress(Object... args)
  {
    if (args.length != 1)
      throw new IllegalArgumentException(
          "Must only provide one integer or long");

    Object arg = args[0];
    int port = -1;

    if (arg instanceof String)
      port = Integer.parseInt((String) arg);
    else if (arg instanceof Number) port = ((Number) arg).intValue();

    if (port > 0) return new LocalAddress("" + port);

    throw new IllegalArgumentException("Invalid port specified, got " + port
        + " from " + arg);
  }

}
