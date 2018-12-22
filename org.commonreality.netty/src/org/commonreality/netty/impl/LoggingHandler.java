package org.commonreality.netty.impl;

/*
 * default logging
 */
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggingHandler implements ChannelHandler
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(LoggingHandler.class);

  private String                     _prefix;

  public LoggingHandler(String prefix)
  {
    _prefix = prefix;
  }

  @Override
  public void bind(ChannelHandlerContext arg0, SocketAddress arg1,
      ChannelPromise arg2) throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("%s Binding %s", _prefix, arg1));
    arg0.bind(arg1, arg2);
  }

  @Override
  public void channelActive(ChannelHandlerContext arg0) throws Exception
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Channel active"));
    arg0.fireChannelActive();
  }

  @Override
  public void channelInactive(ChannelHandlerContext arg0) throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Channel inactive"));
    arg0.fireChannelInactive();
  }

  @Override
  public void channelRead(ChannelHandlerContext arg0, Object arg1)
      throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("%s Channel read %s", _prefix, arg1));
    arg0.fireChannelRead(arg1);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext arg0) throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("%s read complete", _prefix));
    arg0.fireChannelReadComplete();
  }

  @Override
  public void channelRegistered(ChannelHandlerContext arg0) throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Channel registered"));
    arg0.fireChannelRegistered();
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext arg0) throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Channel unregistered"));
    arg0.fireChannelUnregistered();
  }

  @Override
  public void channelWritabilityChanged(ChannelHandlerContext arg0)
      throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Channel writability"));
    arg0.fireChannelWritabilityChanged();
  }

  @Override
  public void close(ChannelHandlerContext arg0, ChannelPromise arg1)
      throws Exception
  {
    try
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("%s Channel close", _prefix));
      arg0.close(arg1);
    }
    catch (Exception e)
    {
      LOGGER.error("wtf", e);
    }
  }

  @Override
  public void connect(ChannelHandlerContext arg0, SocketAddress arg1,
      SocketAddress arg2, ChannelPromise arg3) throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Channel connect"));
    arg0.connect(arg1, arg2, arg3);
  }

  @Override
  public void deregister(ChannelHandlerContext arg0, ChannelPromise arg1)
      throws Exception
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Channel dereg"));
    arg0.deregister(arg1);
  }

  @Override
  public void disconnect(ChannelHandlerContext arg0, ChannelPromise arg1)
      throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Channel disconnect"));
    arg0.disconnect(arg1);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext arg0, Throwable arg1)
      throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("exception caught %s", arg1), arg1);
    arg0.fireExceptionCaught(arg1);
  }

  @Override
  public void flush(ChannelHandlerContext arg0) throws Exception
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("Channel flush"));
    arg0.flush();
  }

  @Override
  public void handlerAdded(ChannelHandlerContext arg0) throws Exception
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("added handler"));
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext arg0) throws Exception
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("remove handler"));
  }

  @Override
  public void read(ChannelHandlerContext arg0) throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("%s read", _prefix));
    arg0.read();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext arg0, Object arg1)
      throws Exception
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void write(ChannelHandlerContext arg0, Object arg1, ChannelPromise arg2)
      throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("%s write %s", _prefix, arg1));
    arg0.write(arg1, arg2);
  }

}
