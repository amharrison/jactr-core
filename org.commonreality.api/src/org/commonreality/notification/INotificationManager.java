package org.commonreality.notification;

/*
 * default logging
 */
import java.util.concurrent.Executor;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.notification.event.INotificationListener;

/**
 * centralized authority within a participant for the management and dispatch of
 * notifications.
 * 
 * @author harrison
 */
public interface INotificationManager
{
  public void addListener(INotificationListener listener, Executor executor);

  public void removeListener(INotificationListener listener);

  public boolean hasListeners();

  /**
   * send the notifiction out to some participant, or all if id is null (but you
   * should probably use IIdentifier.ALL for clarity)
   * 
   * @param participantId
   * @param notification
   */
  public void notify(IIdentifier participantId, INotification notification);

  /**
   * post a notification internally, firing the listeners
   * 
   * @param notification
   */
  public void post(INotification notification);

  /**
   * create a locally unique identifier with the participant as the owner
   * 
   * @param name
   * @return
   */
  public IIdentifier createNotificationIdentifier(String name);

}
