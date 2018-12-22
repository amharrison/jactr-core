package org.commonreality.notification.event;

/*
 * default logging
 */
import org.commonreality.event.ICommonRealityEvent;
import org.commonreality.notification.INotification;

public class NotificationEvent implements
    ICommonRealityEvent<INotificationListener>
{

  private final long                 _systemTime;

  private final INotification        _notification;

  public NotificationEvent(INotification notification)
  {
    _systemTime = System.currentTimeMillis();
    _notification = notification;
  }

  public void fire(INotificationListener listener)
  {
    listener.notificationPosted(this);
  }

  public INotification getNotification()
  {
    return _notification;
  }

  public long getSystemTime()
  {
    return _systemTime;
  }

}
