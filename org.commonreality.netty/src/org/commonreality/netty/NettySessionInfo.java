package org.commonreality.netty;

/*
 * default logging
 */
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.filter.IMessageFilter;
import org.commonreality.net.handler.IExceptionHandler;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.handler.MessageMultiplexer;
import org.commonreality.net.impl.AbstractSessionInfo;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.net.session.ISessionListener;
import org.commonreality.net.transform.IMessageTransfromer;
import org.commonreality.netty.impl.NettyListener;
import org.commonreality.netty.impl.NettyMessageFilter;
import org.commonreality.netty.impl.NettyMessageTransformer;
import org.commonreality.util.LockUtilities;

public class NettySessionInfo extends
    AbstractSessionInfo<ChannelHandlerContext>
{

  static public NettySessionInfo asSessionInfo(ChannelHandlerContext session)
  {
    AttributeKey<NettySessionInfo> key = AttributeKey
        .valueOf("NETTYsessionWrapper");
    Attribute<NettySessionInfo> siat = session.attr(key);
    NettySessionInfo si = siat.get();

    if (si == null)
    {
      si = new NettySessionInfo(session);
      siat.set(si);
    }

    return si;
  }

  static public final String         MULTIPLEXER = "multiplexingHandler";

  /**
   * Logger definition
   */
  static private final transient Log LOGGER      = LogFactory
                                                     .getLog(NettySessionInfo.class);

  private ChannelFuture              _closing;

  private ChannelFuture              _lastWrite;

  private Lock                       _lock       = new ReentrantLock();

  private Condition                  _connected  = _lock.newCondition();

  public NettySessionInfo(ChannelHandlerContext session)
  {
    super(session);
    addListener(new ISessionListener() {

      private void notifyListeners()
      {
        try
        {
          _lock.lock();
          _connected.signalAll();
        }
        finally
        {
          _lock.unlock();
        }
      }

      @Override
      public void opened(ISessionInfo<?> session)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Connection opened"));
        notifyListeners();
      }

      @Override
      public void destroyed(ISessionInfo<?> session)
      {
        notifyListeners();

      }

      @Override
      public void created(ISessionInfo<?> session)
      {
        notifyListeners();

      }

      @Override
      public void closed(ISessionInfo<?> session)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Connection closed"));
        notifyListeners();
      }
    });
  }

  @Override
  public boolean isClosing()
  {
    return _closing != null && !_closing.isDone();
  }

  @Override
  public boolean isConnected()
  {
    Channel c = getRawSession().channel();
    return c.isOpen() && c.isActive();
  }

  @Override
  public void write(Object message) throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Writing %s", message));

    if (!isClosing() && isConnected())
      _lastWrite = getRawSession().writeAndFlush(message);
    else if (LOGGER.isWarnEnabled())
      LOGGER.warn(String.format("Tried to write [%s] to closing session",
          message));
  }

  @Override
  public void writeAndWait(Object message) throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Writing %s", message));
    if (!isClosing() && isConnected())
      _lastWrite = getRawSession().writeAndFlush(message);
    // if (!_lastWrite.awaitUninterruptibly(500, TimeUnit.MILLISECONDS))
    // if (LOGGER.isWarnEnabled())
    // LOGGER.warn(String.format("Took too long to write %s to %s", message,
    // this));
    else if (LOGGER.isWarnEnabled())
      LOGGER.warn(String.format("Tried to write [%s] to closing session",
          message));
  }

  @Override
  public void close() throws Exception
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format("closing"));
    // hold onto this for waiting..
    boolean shouldClose = LockUtilities
        .runLocked(_lock, () -> _closing != null);

    if (shouldClose && isConnected())
    {
      flush();
      ChannelFuture future = getRawSession().close();
      LockUtilities.runLocked(_lock, () -> _closing = future);
    }
  }

  @Override
  public Object getAttribute(String key)
  {
    AttributeKey<Object> authKey = AttributeKey.valueOf(key);
    Attribute<Object> att = getRawSession().attr(authKey);

    return att.get();
  }

  @Override
  public void setAttribute(String key, Object value)
  {
    AttributeKey<Object> authKey = AttributeKey.valueOf(key);

    Attribute<Object> att = getRawSession().attr(authKey);
    att.set(value);
  }

  @Override
  public void addListener(ISessionListener listener)
  {
    getRawSession().pipeline().addBefore(MessageMultiplexer.class.getName(),
        listener.getClass().getName(), new NettyListener(listener));
  }

  @Override
  public void addFilter(IMessageFilter filter)
  {
    getRawSession().pipeline().addBefore(MessageMultiplexer.class.getName(),
        filter.getClass().getName(), new NettyMessageFilter(filter));
  }

  @Override
  public void addExceptionHandler(IExceptionHandler handler)
  {
    getRawSession().pipeline().addAfter(MessageMultiplexer.class.getName(),
        handler.getClass().getName(), new ChannelHandlerAdapter() {
          @Override
          public void exceptionCaught(ChannelHandlerContext ctx,
              Throwable thrown) throws Exception
          {
            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format("Exception caught %s", thrown));

            // if the handler doesn't consume, pass it on
            if (!handler.exceptionCaught(NettySessionInfo.asSessionInfo(ctx),
                thrown)) super.exceptionCaught(ctx, thrown);
          }
        });
  }

  @Override
  public <M> void addHandler(Class<M> clazz, IMessageHandler<M> handler)
  {
    MessageMultiplexer mm = (MessageMultiplexer) getAttribute(MULTIPLEXER);
    mm.add(clazz, handler);
  }

  @Override
  public <M> void removeHandler(Class<M> clazz)
  {
    MessageMultiplexer mm = (MessageMultiplexer) getAttribute(MULTIPLEXER);
    mm.remove(clazz);
  }

  @Override
  public void addTransformer(IMessageTransfromer decorator)
  {
    getRawSession().pipeline().addBefore(MessageMultiplexer.class.getName(),
        decorator.getClass().getName(), new NettyMessageTransformer(decorator));

  }

  @Override
  public void waitForPendingWrites() throws InterruptedException
  {
    try
    {
      flush();
    }
    catch (Exception e)
    {
      LOGGER.error("Failed to wait for writes.", e);
    }
  }

  @Override
  public void waitForDisconnect() throws InterruptedException
  {
    try
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Waiting for disconnect"));

      _lock.lock();
      while (isConnected())
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Waiting for connection to close");
        _connected.await(500, TimeUnit.MILLISECONDS);
      }

      if (LOGGER.isDebugEnabled()) LOGGER.debug("all sessions disconnected");
    }
    finally
    {
      _lock.unlock();
    }
  }

  @Override
  public void flush() throws Exception
  {
    if (!isClosing() && isConnected())
      getRawSession().flush();
    else if (LOGGER.isWarnEnabled())
      LOGGER.warn(String.format("Tried to flush a closing session"));
  }

  @Override
  public String toString()
  {
    IIdentifier id = (IIdentifier) getAttribute("org.commonreality.reality.impl.StateAndConnectionManager.identifier");
    Channel channel = getRawSession().channel();

    StringBuilder sb = new StringBuilder();
    sb.append("[").append(channel.getClass().getSimpleName()).append(", ")
        .append(channel.hashCode()).append(", id:");
    sb.append(id).append(", l:").append(channel.localAddress()).append(", r:")
        .append(channel.remoteAddress()).append("]");

    return sb.toString();
  }
}
