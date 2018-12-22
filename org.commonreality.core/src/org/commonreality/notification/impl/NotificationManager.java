package org.commonreality.notification.impl;

/*
 * default logging
 */
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.event.EventDispatcher;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.identifier.IIdentifier.Type;
import org.commonreality.identifier.impl.BasicIdentifier;
import org.commonreality.net.message.notification.NotificationMessage;
import org.commonreality.notification.INotification;
import org.commonreality.notification.INotificationManager;
import org.commonreality.notification.event.INotificationListener;
import org.commonreality.notification.event.NotificationEvent;
import org.commonreality.participant.IParticipant;

public class NotificationManager implements INotificationManager
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(NotificationManager.class);

  private final EventDispatcher<INotificationListener, NotificationEvent> _eventDispatcher = new EventDispatcher<INotificationListener, NotificationEvent>();

  private final IParticipant                                              _participant;

  public NotificationManager(IParticipant participant)
  {
    _participant = participant;
  }

  public void addListener(INotificationListener listener, Executor executor)
  {
    _eventDispatcher.addListener(listener, executor);
  }

  public boolean hasListeners()
  {
    return _eventDispatcher.hasListeners();
  }

  public void removeListener(INotificationListener listener)
  {
    _eventDispatcher.removeListener(listener);
  }

  /**
   * send the notifiction out to some participant, or all if id is null (but you
   * should probably use IIdentifier.ALL for clarity)
   * 
   * @param participantId
   * @param notification
   */
  public void notify(IIdentifier participantId, INotification notification)
  {
    /*
     * package the notification event and send it out
     */
    if (participantId == null) participantId = IIdentifier.ALL;

    _participant.send(new NotificationMessage(_participant.getIdentifier(),
        participantId, notification));
  }

  public void post(INotification notification)
  {
    if (hasListeners())
      _eventDispatcher.fire(new NotificationEvent(notification));
  }

  public IIdentifier createNotificationIdentifier(String name)
  {
    return new BasicIdentifier(name, Type.NOTIFICATION, _participant
        .getIdentifier());
  }
}
