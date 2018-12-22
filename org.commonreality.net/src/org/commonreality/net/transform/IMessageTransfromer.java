package org.commonreality.net.transform;

import java.util.Collection;

/*
 * default logging
 */

/**
 * interface that allows you to decorate messages (or change them) as them come
 * in.
 * 
 * @author harrison
 */
public interface IMessageTransfromer
{

  /**
   * if the message is unchanged, return singleton of it. and empty collection
   * swallows the message.
   * 
   * @param message
   * @return
   */
  public Collection<?> messageReceived(Object message);
}
