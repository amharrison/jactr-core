package org.commonreality.net.message.notification;

/*
 * default logging
 */
import java.io.Serializable;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IMessage;
import org.commonreality.notification.INotification;

public interface INotificationMessage extends IMessage, Serializable
{
  public INotification getNotification();

  public IIdentifier getDestination();
}
