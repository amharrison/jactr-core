package org.commonreality.reality.impl.handler;

/*
 * default logging
 */
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.message.command.control.ControlAcknowledgement;
import org.commonreality.net.message.notification.NotificationMessage;
import org.commonreality.net.message.request.connect.ConnectionRequest;
import org.commonreality.net.message.request.object.NewIdentifierRequest;
import org.commonreality.net.message.request.object.ObjectCommandRequest;
import org.commonreality.net.message.request.object.ObjectDataRequest;
import org.commonreality.net.message.request.time.RequestTime;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.participant.impl.handlers.GeneralObjectHandler;
import org.commonreality.reality.impl.DefaultReality;
import org.commonreality.reality.impl.StateAndConnectionManager;

/**
 * Utility for the creation of the default set of message handlers for
 * Defaultreality.
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
      final DefaultReality reality)
  {
    StateAndConnectionManager manager = reality.getStateAndConnectionManager();

    Map<Class<?>, IMessageHandler<?>> handlers = new HashMap<Class<?>, IMessageHandler<?>>();

    // connection handling
    handlers.put(ConnectionRequest.class, new ConnectionHandler(manager));

    // time
    handlers.put(RequestTime.class, new TimeHandler(reality));

    handlers.put(ControlAcknowledgement.class,
        new ConnectionAckHandler(manager));

    // object management
    GeneralObjectHandler goh = reality.getGeneralObjectHandler();

    handlers.put(ObjectCommandRequest.class, new ObjectCommandHandler(reality,
        manager, goh));
    handlers.put(ObjectDataRequest.class, new ObjectDataHandler(reality,
        manager, goh));

    handlers.put(NewIdentifierRequest.class, new NewIdentifierHandler(
        reality, manager, goh));

    // notification manager
    handlers.put(NotificationMessage.class,
        new IMessageHandler<NotificationMessage>() {

          @Override
          public void accept(ISessionInfo<?> t, NotificationMessage u)
          {
            IIdentifier dest = u.getDestination();
            reality.send(dest, u);
          }

        });

    return handlers;
  }
}
