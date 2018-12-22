package org.commonreality.netty.impl;

/*
 * default logging
 */
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.filter.IMessageFilter;
import org.commonreality.netty.NettySessionInfo;

public class NettyMessageFilter extends ChannelHandlerAdapter
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(NettyMessageFilter.class);

  private final IMessageFilter       _filter;

  public NettyMessageFilter(IMessageFilter filter)
  {
    _filter = filter;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg)
      throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Filtering %s", msg));

    if (_filter.accept(NettySessionInfo.asSessionInfo(ctx), msg))
      super.channelRead(ctx, msg);
  }
}
