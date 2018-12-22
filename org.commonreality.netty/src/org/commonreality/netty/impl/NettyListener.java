package org.commonreality.netty.impl;

/*
 * default logging
 */
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.net.session.ISessionListener;
import org.commonreality.netty.NettySessionInfo;

public class NettyListener extends ChannelHandlerAdapter
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(NettyListener.class);

  private final ISessionListener     _listener;

  public NettyListener(ISessionListener listener)
  {
    _listener = listener;
  }

  @Override
  public void connect(ChannelHandlerContext arg0, SocketAddress arg1,
      SocketAddress arg2, ChannelPromise arg3) throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Channel connect"));
    super.connect(arg0, arg1, arg2, arg3);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception
  {
    super.channelActive(ctx);
    _listener.opened(NettySessionInfo.asSessionInfo(ctx));
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception
  {
    super.channelRegistered(ctx);
    _listener.created(NettySessionInfo.asSessionInfo(ctx));
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception
  {
    super.channelInactive(ctx);

    ISessionInfo<?> session = NettySessionInfo.asSessionInfo(ctx);
    _listener.closed(session);
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception
  {

    super.channelUnregistered(ctx);
    _listener.destroyed(NettySessionInfo.asSessionInfo(ctx));
  }

  @Override
  public void disconnect(ChannelHandlerContext arg0, ChannelPromise arg1)
      throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Channel disconnect"));
    super.disconnect(arg0, arg1);
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
  }
}
