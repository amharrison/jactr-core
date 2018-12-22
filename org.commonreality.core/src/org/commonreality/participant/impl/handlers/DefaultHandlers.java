package org.commonreality.participant.impl.handlers;

/*
 * default logging
 */
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.message.command.control.ControlCommand;
import org.commonreality.net.message.command.object.ObjectCommand;
import org.commonreality.net.message.command.object.ObjectData;
import org.commonreality.net.message.command.time.TimeCommand;
import org.commonreality.net.message.notification.NotificationMessage;
import org.commonreality.net.message.request.connect.ConnectionAcknowledgment;
import org.commonreality.net.message.request.object.NewIdentifierAcknowledgement;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.participant.impl.AbstractParticipant;

/**
 * Utility for the creation of the default set of message handlers for
 * participants.
 * 
 * @author harrison
 */
public class DefaultHandlers
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(DefaultHandlers.class);

  public DefaultHandlers()
  {
  }

  public Map<Class<?>, IMessageHandler<?>> createHandlers(
      AbstractParticipant participant)
  {
    Map<Class<?>, IMessageHandler<?>> handlers = new HashMap<Class<?>, IMessageHandler<?>>();

    // connection handling
    handlers.put(ConnectionAcknowledgment.class, new ConnectionHandler(
        participant));

    // control
    handlers.put(ControlCommand.class, new ControlHandler(participant));

    // time
    handlers.put(TimeCommand.class, new TimeHandler(participant));

    // object management
    GeneralObjectHandler goh = participant.getGeneralObjectHandler();
    handlers.put(ObjectCommand.class, new ObjectCommandHandler(goh));
    handlers.put(ObjectData.class, new ObjectDataHandler(goh));
    handlers.put(NewIdentifierAcknowledgement.class, new NewIdentifierHandler(
        goh));

    // notification manager
    handlers.put(NotificationMessage.class,
        new IMessageHandler<NotificationMessage>() {

          @Override
          public void accept(ISessionInfo<?> t, NotificationMessage u)
          {
            participant.getNotificationManager().post(u.getNotification());
          }

        });

    return handlers;
  }
}
