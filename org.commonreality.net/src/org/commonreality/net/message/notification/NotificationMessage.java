package org.commonreality.net.message.notification;

/*
 * default logging
 */
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.impl.BaseMessage;
import org.commonreality.notification.INotification;

public class NotificationMessage extends BaseMessage implements
    INotificationMessage
{
  private static final long          serialVersionUID = 2315376053212789989L;

  private final INotification        _notification;

  private final IIdentifier   _destination;

  public NotificationMessage(IIdentifier source, IIdentifier destination,
      INotification notification)
  {
    super(source);
    _notification = notification;
    _destination = destination;
  }

  public INotification getNotification()
  {
    return _notification;
  }

  public IMessage copy()
  {
    return new NotificationMessage(getSource(), getDestination(), _notification);
  }

  public IIdentifier getDestination()
  {
    return _destination;
  }

}
