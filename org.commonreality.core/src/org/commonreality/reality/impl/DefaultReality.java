/*
 * Created on Feb 23, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.reality.impl;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.identifier.impl.BasicIdentifier;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.message.IAcknowledgement;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.command.ICommand;
import org.commonreality.net.message.command.time.TimeCommand;
import org.commonreality.net.message.credentials.ICredentials;
import org.commonreality.net.message.notification.NotificationMessage;
import org.commonreality.net.message.request.IRequest;
import org.commonreality.net.message.request.connect.ConnectionAcknowledgment;
import org.commonreality.net.message.request.object.NewIdentifierAcknowledgement;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.net.session.ISessionListener;
import org.commonreality.object.identifier.BasicSensoryIdentifier;
import org.commonreality.object.identifier.ISensoryIdentifier;
import org.commonreality.participant.addressing.IAddressingInformation;
import org.commonreality.participant.impl.AbstractParticipant;
import org.commonreality.participant.impl.ack.SessionAcknowledgements;
import org.commonreality.reality.CommonReality;
import org.commonreality.reality.IReality;
import org.commonreality.reality.impl.handler.DefaultHandlers;
import org.commonreality.time.IClock;
import org.commonreality.time.impl.OwnedClock;

/**
 * @author developer
 */
/*
 * No longer Exposes system property "defaultReality.ioMaxThreads" (default max)
 * to permit tuning of threading behavior. Also exposes
 * "defaultReality.useSharedThreads" to use the shared io thread common to all
 * participants.
 */
public class DefaultReality extends AbstractParticipant implements IReality
{

  static public final String              MESSAGE_TTL                 = "MessageTTL";

  static public final String              ACK_TIMEOUT_PARAM           = "AcknowledgementTimeout";

  static public final String              DISCONNECT_PARAM            = "DisconnectAllOnTimeout";

  /**
   * logger definition
   */
  static private final Log                LOGGER                      = LogFactory
                                                                          .getLog(DefaultReality.class);

  private OwnedClock                      _masterClock;

  private long                            _timeout                    = 10000;

  // private ExecutorService _centralExecutor;

  private boolean                         _disconnectAllOnMissedState = false;

  final private StateAndConnectionManager _manager;

  public DefaultReality()
  {
    super(IIdentifier.Type.REALITY);

    _masterClock = new OwnedClock(0.05, (newTime, ownedClock) -> {
      // send the time update whenever the clock is updated
        double timeShift = ownedClock.getAuthority().get().getLocalTimeShift();
        send(new TimeCommand(getIdentifier(), newTime - timeShift));
      });

    _manager = new StateAndConnectionManager(this, getCentralExector());
    _manager.setAcknowledgementTimeout(getTimeout());
    _manager.setPromiscuous(true); // for now, this should ultimately be removed
    CommonReality.setReality(this);
  }

  public StateAndConnectionManager getStateAndConnectionManager()
  {
    return _manager;
  }

  @Override
  protected ISessionListener createDefaultServerListener()
  {
    return new ISessionListener() {

      @Override
      public void opened(ISessionInfo<?> session)
      {
        new SessionAcknowledgements(session);// auto install ack support
        session.addExceptionHandler((s, t) -> {
          try
          {
            LOGGER.error(
                String.format("Exception caught from %s, closing ", s), t);
            if (s.isConnected() && !s.isClosing()) s.close();
          }
          catch (Exception e)
          {
            LOGGER.error(String.format("Exception from %s, closing. ", s), e);
          }
          return true;
        });
      }

      @Override
      public void destroyed(ISessionInfo<?> session)
      {

      }

      @Override
      public void created(ISessionInfo<?> session)
      {

      }

      @Override
      public void closed(ISessionInfo<?> session)
      {
        _manager.participantDisconnected(session);
      }
    };
  }

  @Override
  protected Map<Class<?>, IMessageHandler<?>> createDefaultHandlers()
  {
    return new DefaultHandlers().createHandlers(this);
  }

  public Executor getCentralExector()
  {
    return getPeriodicExecutor();
  }

  public long getTimeout()
  {
    return _timeout;
  }

  public void setTimeout(long timeout)
  {
    _timeout = timeout;
  }

  @Override
  public String getName()
  {
    return "Reality";
  }

  @Override
  public IClock getClock()
  {
    return _masterClock;
  }

  /**
   * initialize common reality. this will make sure that we can accept
   * connections. the initialize event will be sent immediately after the
   * connection is established to the participant
   * 
   * @see org.commonreality.participant.impl.AbstractParticipant#initialize()
   */
  @Override
  public void initialize() throws Exception
  {
    connect();

    setIdentifier(new BasicIdentifier(getName(), IIdentifier.Type.REALITY, null));
    setCommonRealityIdentifier(getIdentifier());

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Connected as " + getIdentifier());

    super.initialize();
  }

  @Override
  public void configure(Map<String, String> options) throws Exception
  {
    // configure is local and should not be sent out as a command
    // sendAndWaitForAcknowledgement(new ControlCommand(getIdentifier(),
    // IControlCommand.State.CONFIGURE, options), getTimeout());

    if (options.containsKey(MESSAGE_TTL))
      try
      {
        setTimeout(Long.parseLong(options.get(MESSAGE_TTL)));
      }
      catch (NumberFormatException nfe)
      {
        if (LOGGER.isWarnEnabled())
          LOGGER.warn("Could not process message ttl, using default 5000ms");
        setTimeout(5000);
      }

    if (options.containsKey(ACK_TIMEOUT_PARAM))
      try
      {
        setTimeout(Long.parseLong(options.get(ACK_TIMEOUT_PARAM)));
      }
      catch (Exception e)
      {
        if (LOGGER.isWarnEnabled())
          LOGGER.warn("Could not process message ttl, using default 5000ms");
        setTimeout(5000);
      }

    if (options.containsKey(DISCONNECT_PARAM))
      try
      {
        _disconnectAllOnMissedState = Boolean.parseBoolean(options
            .get(DISCONNECT_PARAM));
      }
      catch (Exception e)
      {
        _disconnectAllOnMissedState = false;
      }

    super.configure(options);
  }

  /**
   * disconnect and shutdown
   */
  public void cleanUp()
  {
    // if (_centralExecutor != null)
    // {
    // _centralExecutor.shutdown();
    // _centralExecutor = null;
    // }
    try
    {
      super.shutdown();
    }
    catch (Exception e)
    {
      LOGGER.error("Could not disconnect ", e);
    }
  }

  /**
   * set the state of the connected participants. For any participant that does
   * not acknowledge the correct (or any) state, we disconnect them outright
   * 
   * @param state
   */
  protected boolean setParticipantStates(State state)
  {
    boolean allResponded = true;
    StateAndConnectionManager manager = getStateAndConnectionManager();
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Setting participant states to [%s]", state));
    Collection<IIdentifier> unresponsiveParticipants = manager.setState(state);
    for (IIdentifier unresponsive : unresponsiveParticipants)
    {
      ISessionInfo session = manager.getParticipantSession(unresponsive);
      if (session == null || session.isClosing() || !session.isConnected())
        continue;

      if (LOGGER.isWarnEnabled())
        LOGGER.warn(unresponsive + " did not respond to state command ["
            + state + "], disconnecting.");
      try
      {
        session.close();
      }
      catch (Exception e)
      {
        LOGGER.error("Failed to close connection with " + unresponsive, e);
      }
      allResponded = false;
    }

    if (!allResponded && _disconnectAllOnMissedState)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn("Shutting down all due to unresponsive "
            + unresponsiveParticipants);
      for (ISessionInfo session : manager.getActiveSessions(null))
        if (!session.isClosing() && session.isConnected())
        {
          IIdentifier id = manager.getParticipantIdentifier(session);
          if (LOGGER.isWarnEnabled()) LOGGER.warn("Closing " + id);
          try
          {
            session.close();
          }
          catch (Exception e)
          {
            LOGGER.error("Failed to close connection with " + id, e);
          }
        }
    }

    return allResponded;
  }

  /**
   * @see org.commonreality.participant.impl.AbstractParticipant#reset()
   */
  @Override
  public void reset(boolean clockWillBeReset) throws Exception
  {
    checkState(State.INITIALIZED, State.STOPPED);

    // ReentrantReadWriteLock lock =
    // getStateAndConnectionManager().getStateLock();

    // try
    // {
    // lock.writeLock().lock();

      if (LOGGER.isDebugEnabled()) LOGGER.debug("reseting");
      /*
       * tell everyone to reset..
       */
      // sendAndWaitForAcknowledgement(new ControlCommand(getIdentifier(),
      // IControlCommand.State.RESET, clockWillBeReset), getTimeout());
      setParticipantStates(State.INITIALIZED);
    // }
    // finally
    // {
    // lock.writeLock().unlock();
    super.reset(clockWillBeReset);
    // }

  }

  /**
   * tell all the participants to start
   * 
   * @see org.commonreality.participant.impl.AbstractParticipant#start()
   */
  @Override
  public void start() throws Exception
  {
    checkState(State.INITIALIZED);

    // ReentrantReadWriteLock lock =
    // getStateAndConnectionManager().getStateLock();

    boolean shutdown = false;

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Starting");

    shutdown = !setParticipantStates(State.STARTED)
        && _disconnectAllOnMissedState;

    if (!shutdown)
      super.start();
    else
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Shutting down due to missed states");
      shutdown();
    }
  }

  /**
   * @see org.commonreality.participant.impl.AbstractParticipant#stop()
   */
  @Override
  public void stop() throws Exception
  {
    checkState(State.STARTED, State.SUSPENDED);

    // ReentrantReadWriteLock lock =
    // getStateAndConnectionManager().getStateLock();

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Stopping", new RuntimeException());
    // try
    // {
    // lock.writeLock().lock();

    setParticipantStates(State.STOPPED);

    // }
    // finally
    // {
    // lock.writeLock().unlock();

      super.stop();
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Stopped");
    // }

  }

  /**
   * this will actually send to EVERYONE connected - and returns a null
   * acknowldgement for now..
   * 
   * @see org.commonreality.participant.impl.AbstractParticipant#send(org.commonreality.net.message.IMessage)
   */
  @Override
  public Future<IAcknowledgement> send(IMessage message)
  {
    for (ISessionInfo session : getStateAndConnectionManager()
        .getActiveSessions(null))
      send(session, message);

    return EMPTY_ACK;
  }

  public Future<IAcknowledgement> send(Object session, IMessage message)
  {
    if (session instanceof ISessionInfo)
      return send((ISessionInfo<?>) session, message);
    else if (session instanceof IIdentifier)
      return send((IIdentifier) session, message);

    throw new IllegalArgumentException("Could not send because " + session
        + " was neither an IIdentifier or IoSession");
  }

  protected Future<IAcknowledgement> send(ISessionInfo<?> session,
      IMessage message)
  {
    Future<IAcknowledgement> rtn = null;

    if (session != null)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("[%s] enter synch %s", getName(), session));
      synchronized (session)
      {
        if (message instanceof IRequest)
        {
          SessionAcknowledgements sa = SessionAcknowledgements
              .getSessionAcks(session);
          if (sa != null) rtn = sa.newAckFuture(message);
        }
      }

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("[%s] exit synch %s", getName(), session));

      // pulled write out..
      try
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("[%s] Writing %s to %s", getName(),
              message, session));
        session.write(message);
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("[%s] Wrote %s to %s", getName(), message,
              session));

        if (shouldFlush(message)) session.flush();
      }
      catch (Exception e)
      {
        LOGGER.error(String.format("Failed to write message %s to %s ",
            message, session), e);
      }
    }
    else if (LOGGER.isWarnEnabled()) LOGGER.warn("null session?");

    if (rtn == null && message instanceof IRequest)
      if (LOGGER.isWarnEnabled())
        LOGGER.warn("EMPTY acknowledgment for " + session);

    if (rtn == null) rtn = EMPTY_ACK;

    return rtn;
  }

  @Override
  protected boolean shouldFlush(Object message)
  {
    boolean flush = message instanceof ConnectionAcknowledgment
        || message instanceof ICommand
        || message instanceof NewIdentifierAcknowledgement
        || message instanceof NotificationMessage
        || message instanceof TimeCommand
        || message instanceof IAcknowledgement;
    return flush;
  }

  public Future<IAcknowledgement> send(IIdentifier identifier, IMessage message)
  {
    if (IIdentifier.ALL.equals(identifier)) return send(message);

    ISessionInfo session = getStateAndConnectionManager()
        .getParticipantSession(identifier);

    /*
     * the participant might not have finished the connection sequence..
     */
    if (session == null)
      session = getStateAndConnectionManager().getPendingParticipantSession(
          identifier);

    if (session != null) return send(session, message);

    if (LOGGER.isWarnEnabled())
      LOGGER.warn("No session associated with " + identifier);

    return EMPTY_ACK;
  }

  /**
   * @see org.commonreality.participant.impl.AbstractParticipant#resume()
   */
  @Override
  public void resume() throws Exception
  {
    // ReentrantReadWriteLock lock =
    // getStateAndConnectionManager().getStateLock();

    boolean shutdown = false;

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Resuming");

    // sendAndWaitForAcknowledgement(new ControlCommand(getIdentifier(),
    // IControlCommand.State.RESUME), getTimeout());

    shutdown = !setParticipantStates(State.STARTED)
        && _disconnectAllOnMissedState;

    if (shutdown)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Shutting down due to missed states");
      shutdown();
    }
    else
      resume();
  }

  /**
   * tell folks to shut down..
   * 
   * @see org.commonreality.participant.impl.AbstractParticipant#shutdown()
   */
  @Override
  public void shutdown() throws Exception
  {
    checkState(State.STOPPED, State.CONNECTED, State.INITIALIZED);

    CommonReality.setReality(null);

    if (LOGGER.isDebugEnabled()) LOGGER.debug("Shutting down");

    // setParticipantStates(State.UNKNOWN);

    cleanUp();
  }

  /**
   * @see org.commonreality.participant.impl.AbstractParticipant#suspend()
   */
  @Override
  public void suspend() throws Exception
  {
    checkState(State.STARTED);

    // ReentrantReadWriteLock lock =
    // getStateAndConnectionManager().getStateLock();

    boolean shutdown = false;
    if (LOGGER.isDebugEnabled()) LOGGER.debug("suspending");

    shutdown = !setParticipantStates(State.SUSPENDED)
        && _disconnectAllOnMissedState;

    if (shutdown)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("Shutting down due to missed states");
      shutdown();
    }
    else
      super.suspend();
  }

  /**
   * @see org.commonreality.participant.impl.AbstractParticipant#getAddressingInformation()
   */
  @Override
  public IAddressingInformation getAddressingInformation()
  {
    Collection<IAddressingInformation> info = getServerAddressInformation();
    if (info.size() == 0) return null;
    return info.iterator().next();
  }

  /**
   * @see org.commonreality.participant.impl.AbstractParticipant#getCredentials()
   */
  @Override
  public ICredentials getCredentials()
  {
    return null;
  }

  /**
   * @see org.commonreality.reality.IReality#newIdentifier(org.commonreality.participant.identifier.IParticipantIdentifier,
   *      java.lang.String)
   */
  public IIdentifier newIdentifier(IIdentifier owner, IIdentifier template)
  {
    if (template instanceof ISensoryIdentifier)
      return new BasicSensoryIdentifier(template.getName(), template.getType(),
          owner, ((ISensoryIdentifier) template).getSensor(),
          ((ISensoryIdentifier) template).getAgent());

    return new BasicIdentifier(template.getName(), template.getType(), owner);
  }

  /**
   * @see org.commonreality.reality.IReality#add(org.commonreality.net.message.credentials.ICredentials)
   */
  public void add(ICredentials credentials, boolean wantsClockControl)
  {
    getStateAndConnectionManager().grantCredentials(credentials,
        wantsClockControl);
  }

  /**
   * @see org.commonreality.reality.IReality#remove(org.commonreality.net.message.credentials.ICredentials)
   */
  public void remove(ICredentials credentials)
  {
    getStateAndConnectionManager().revokeCredentials(credentials);
  }

}
