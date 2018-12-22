package org.commonreality.netty.impl;

/*
 * default logging
 */
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.handler.MessageMultiplexer;
import org.commonreality.netty.NettySessionInfo;

public class NettyMultiplexer extends ChannelHandlerAdapter
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(NettyMultiplexer.class);

  private final MessageMultiplexer   _multiplexer;

  public NettyMultiplexer(MessageMultiplexer multiplexer)
  {
    _multiplexer = multiplexer;
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception
  {
    NettySessionInfo.asSessionInfo(ctx).setAttribute(
        NettySessionInfo.MULTIPLEXER, _multiplexer);
    super.channelRegistered(ctx);
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg,
      ChannelPromise promize) throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Writing %s", msg));
    super.write(ctx, msg, promize);
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
      throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Received %s", msg));

    super.channelRead(ctx, msg);
    _multiplexer.accept(NettySessionInfo.asSessionInfo(ctx), msg);
  }

}
