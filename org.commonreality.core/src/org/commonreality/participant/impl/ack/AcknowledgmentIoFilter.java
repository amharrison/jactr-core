package org.commonreality.participant.impl.ack;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.filter.IMessageFilter;
import org.commonreality.net.message.IAcknowledgement;
import org.commonreality.net.session.ISessionInfo;

public class AcknowledgmentIoFilter implements IMessageFilter
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AcknowledgmentIoFilter.class);

  @Override
  public boolean accept(ISessionInfo<?> session, Object message)
  {
    try
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Received message %s", message));

      if (message instanceof IAcknowledgement)
      {
        IAcknowledgement ackMsg = (IAcknowledgement) message;
        long requestId = ackMsg.getRequestMessageId();
        SessionAcknowledgements sessionAcks = SessionAcknowledgements
            .getSessionAcks(session);

        if (sessionAcks != null)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("(%s) request %d acknowledged by %s ",
                session, requestId, ackMsg));

          sessionAcks.acknowledgementReceived(ackMsg);
        }
        else if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("No session acknowledges available?"));
      }
    }
    catch (Exception e)
    {
      LOGGER.error("Failed filtering ", e);
    }
    return true;
  }
}
