package org.commonreality.notification.event;

/*
 * default logging
 */
import org.commonreality.event.ICommonRealityListener;

public interface INotificationListener extends ICommonRealityListener
{

  public void notificationPosted(NotificationEvent event);
}
