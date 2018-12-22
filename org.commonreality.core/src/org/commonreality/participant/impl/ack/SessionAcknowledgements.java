package org.commonreality.participant.impl.ack;

/*
 * default logging
 */
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.message.IAcknowledgement;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.net.session.ISessionListener;

/**
 * contains session specific ack data. Many IMessages require an acknowledgment.
 * Some code may want to block until that ack is received. Once the ack is
 * received, any locks are released and the ack is moved to a memory sensitive
 * cache, allowing it to be accessed for a limited duration of time. <br/>
 * <br/>
 * 3/19/15 : recent profiling showed that the expireGarbageAcks() wasn't
 * behaving as expected. Specifically, we were holding on to acks that should
 * have expired. Specifically, the AckFutureReference's soft reference wasn't
 * reclaiming. Moved to weak references.
 * 
 * @author harrison
 */
public class SessionAcknowledgements
{

  /**
   * Logger definition
   */
  static private final transient Log                           LOGGER                  = LogFactory
                                                                                           .getLog(SessionAcknowledgements.class);

  private final ISessionInfo                                   _session;

  /**
   * map, indexed by the IMessage's id of unacknowledged Acks. Once the ack is
   * received, the AckFuture gets moved to the memory sensitive cache.
   */
  private SortedMap<Long, CompletableFuture<IAcknowledgement>> _pendingAcknowledgments = Collections
                                                                                           .synchronizedSortedMap(new TreeMap<Long, CompletableFuture<IAcknowledgement>>());


  private ISessionListener                                     _sessionListener;

  static public SessionAcknowledgements getSessionAcks(ISessionInfo session)
  {
    return (SessionAcknowledgements) session
        .getAttribute(SessionAcknowledgements.class.getName());
  }

  public SessionAcknowledgements(ISessionInfo<?> session)
  {
    _session = session;

    _sessionListener = new ISessionListener() {

      @Override
      public void opened(ISessionInfo<?> session)
      {
        // install(session);
      }

      @Override
      public void destroyed(ISessionInfo<?> session)
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void created(ISessionInfo<?> session)
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void closed(ISessionInfo<?> session)
      {
        uninstall(session);
      }
    };

    install(session);
  }

  protected void install(ISessionInfo<?> session)
  {
    session.setAttribute(SessionAcknowledgements.class.getName(), this);
    session.addListener(_sessionListener);
    session.addFilter(new AcknowledgmentIoFilter());

    // session.getService().addListener(_sessionListener);
    // add the filter for handling incoming responses
    // session.getFilterChain().addLast("ackFilter", new
    // AcknowledgmentIoFilter());
  }

  protected void uninstall(ISessionInfo<?> session)
  {
    // we could do a better job of cleaning up here..

    // session.getService().removeListener(_sessionListener);
    // session.getFilterChain().remove("ackFilter");
    session.setAttribute(SessionAcknowledgements.class.getName(), null);
    _pendingAcknowledgments.clear();
    // _acknowledged.clear();
  }

  public CompletableFuture<IAcknowledgement> newAckFuture(IMessage message)
  {
    long id = message.getMessageId();
    // AckFuture future = new AckFuture(id);
    CompletableFuture<IAcknowledgement> future = new CompletableFuture<IAcknowledgement>();

    _pendingAcknowledgments.put(id, future);
    return future;
  }

  public void acknowledgementReceived(IAcknowledgement ack)
  {
    long requestId = ack.getRequestMessageId();
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("(%s) Ack (%d) received for message (%d)",
          getSession(), ack.getMessageId(), requestId));

    /**
     * snag the future, update it, then move it to the _acknowledge set
     */
    CompletableFuture<IAcknowledgement> future = _pendingAcknowledgments
        .remove(requestId);
    if (future != null) future.complete(ack);

  }

  public ISessionInfo<?> getSession()
  {
    return _session;
  }
}