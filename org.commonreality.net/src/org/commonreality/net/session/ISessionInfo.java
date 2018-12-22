package org.commonreality.net.session;

import org.commonreality.net.filter.IMessageFilter;
import org.commonreality.net.handler.IExceptionHandler;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.transform.IMessageTransfromer;

/*
 * default logging
 */

/**
 * abstraction for a network connection
 * 
 * @author harrison
 */
public interface ISessionInfo<T>
{

  public boolean isClosing();

  public boolean isConnected();

  public void write(Object message) throws Exception;

  public void writeAndWait(Object message) throws Exception;

  public void close() throws Exception;

  public T getRawSession();

  public Object getAttribute(String key);

  public void setAttribute(String key, Object value);

  public void addListener(ISessionListener listener);


  public void addFilter(IMessageFilter filter);

  public void addExceptionHandler(IExceptionHandler handler);

  public <M> void addHandler(Class<M> clazz, IMessageHandler<M> handler);

  public <M> void removeHandler(Class<M> clazz);

  public void addTransformer(IMessageTransfromer decorator);

  public void waitForPendingWrites() throws InterruptedException;

  public void waitForDisconnect() throws InterruptedException;

  public void flush() throws Exception;
}
