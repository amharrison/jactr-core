/*
 * Created on Feb 23, 2007 Copyright (C) 2001-6, Anthony Harrison
 * amharrison@gmail.com (jactr.org) This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version. This library is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.participant.impl;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.executor.GeneralThreadFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.message.IAcknowledgement;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.command.control.ControlAcknowledgement;
import org.commonreality.net.message.notification.NotificationMessage;
import org.commonreality.net.message.request.IRequest;
import org.commonreality.net.message.request.connect.ConnectionRequest;
import org.commonreality.net.message.request.object.IObjectDataRequest;
import org.commonreality.net.message.request.object.NewIdentifierRequest;
import org.commonreality.net.message.request.time.RequestTime;
import org.commonreality.net.protocol.IProtocolConfiguration;
import org.commonreality.net.service.IClientService;
import org.commonreality.net.service.INetworkService;
import org.commonreality.net.service.IServerService;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.net.session.ISessionListener;
import org.commonreality.net.transport.ITransportProvider;
import org.commonreality.participant.IParticipant;
import org.commonreality.participant.addressing.IAddressingInformation;
import org.commonreality.participant.addressing.impl.BasicAddressingInformation;
import org.commonreality.participant.impl.ack.SessionAcknowledgements;
import org.commonreality.participant.impl.handlers.DefaultHandlers;
import org.commonreality.participant.impl.handlers.GeneralObjectHandler;
import org.commonreality.reality.impl.StateAndConnectionManager;

/**
 * Skeleton participant that handles the majority of tasks. A participants life
 * cycle works like this: Sometime after instantiation, connect will be called
 * which will establish the connection to CommonReality. When common reality is
 * ready to start, it will signal that all sensors and agents should initialize.
 * afetr initializing, start will be signaled. suspend and resume may be called
 * while running The simulation will run for some amount of time, until stop
 * will be called. then either reset or shutdown may be called. shutdown should
 * then disconnect. <br/>
 * Exposes system property "participant.ioMaxThreads" (default 1) to permit
 * tuning of threading behavior. Plus "participant.useSharedThreads" (default
 * false)
 * 
 * @author developer
 */
public abstract class AbstractParticipant extends ThinParticipant implements
    IParticipant
{
  /**
   * logger definition
   */
  static final Log                        LOGGER = LogFactory
                                                     .getLog(AbstractParticipant.class);

  static private ScheduledExecutorService _periodicExecutor;

  /**
   * return a shared periodic executor that can be useful in many circumstances
   * for periodic events
   * 
   * @return
   */
  static public ScheduledExecutorService getPeriodicExecutor()
  {
    synchronized (AbstractParticipant.class)
    {
      if (_periodicExecutor == null || _periodicExecutor.isShutdown()
          || _periodicExecutor.isTerminated())
        _periodicExecutor = Executors.newScheduledThreadPool(1,
            new GeneralThreadFactory("IParticipant-Periodic"));
      return _periodicExecutor;
    }
  }

  private IIdentifier                         _commonRealityIdentifier;

  private volatile ISessionInfo<?>            _crSession;              // if
                                                                        // connected
                                                                        // to
                                                                        // CR.

  private Map<INetworkService, SocketAddress> _services;

  private GeneralThreadFactory                _centralThreadFactory;

  private GeneralThreadFactory                _ioThreadFactory;

  private GeneralObjectHandler                _generalObjectHandler;

  public AbstractParticipant(IIdentifier.Type type)
  {
    super(type);
    _services = new HashMap<INetworkService, SocketAddress>();

    _generalObjectHandler = new GeneralObjectHandler(this);
  }

  public GeneralObjectHandler getGeneralObjectHandler()
  {
    return _generalObjectHandler;
  }

  public void setCommonRealityIdentifier(IIdentifier crId)
  {
    if (_commonRealityIdentifier != null)
      throw new IllegalStateException(
          "CommonReality identifier has already been set");
    _commonRealityIdentifier = crId;
  }

  public IIdentifier getCommonRealityIdentifier()
  {
    return _commonRealityIdentifier;
  }

  abstract public IAddressingInformation getAddressingInformation();

  abstract public String getName();

  synchronized protected GeneralThreadFactory getCentralThreadFactory()
  {
    if (_centralThreadFactory == null)
      _centralThreadFactory = new GeneralThreadFactory(getName());
    return _centralThreadFactory;
  }

  synchronized protected GeneralThreadFactory getIOThreadFactory()
  {
    if (_ioThreadFactory == null)
      _ioThreadFactory = new GeneralThreadFactory(getName() + "-IOProcessor",
          getCentralThreadFactory().getThreadGroup());
    return _ioThreadFactory;
  }

  protected void setSession(ISessionInfo<?> session)
  {
    _crSession = session;
  }

  protected ISessionInfo<?> getSession()
  {
    return _crSession;
  }

  @Override
  public void setIdentifier(IIdentifier identifier)
  {
    super.setIdentifier(identifier);

    ISessionInfo<?> session = getSession();
    if (session != null)
    {
      session.setAttribute(StateAndConnectionManager.IDENTIFIER, identifier);

      Thread.currentThread().setName(identifier.getName() + "-io");
    }
  }

  /**
   * creates and returns the normal message handles required by this
   * participant.
   * 
   * @return
   */
  protected Map<Class<?>, IMessageHandler<?>> createDefaultHandlers()
  {
    return new DefaultHandlers().createHandlers(this);
  }

  /**
   * by default, returns null.. as server side for an abstract participant is a
   * little less clear.
   * 
   * @return
   */
  protected ISessionListener createDefaultServerListener()
  {
    return new ISessionListener() {

      @Override
      public void opened(ISessionInfo<?> session)
      {
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
      public void closed(ISessionInfo<?> session)
      {

      }

      @Override
      public void created(ISessionInfo<?> session)
      {

      }

      @Override
      public void destroyed(ISessionInfo<?> session)
      {

      }

    };
  }

  protected ISessionListener createDefaultClientListener()
  {
    return new ISessionListener() {

      @Override
      public void opened(ISessionInfo<?> session)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Connection opened to %s", session));

        setSession(session);

        try
        {
          new SessionAcknowledgements(session); // self installs

          session
              .addExceptionHandler((s, t) -> {
                try
                {
                  LOGGER.error(
                      String.format("Exception caught from %s, closing ", s), t);
                  if (s.isConnected() && !s.isClosing()) s.close();
                }
                catch (Exception e)
                {
                  LOGGER.error(
                      String.format("Exception from %s, closing. ", s), e);
                }
                return true;
              });
        }
        catch (Exception e)
        {
          LOGGER.error("FAiled in connect properly? ", e);
        }

        /*
         * great big fat hack, it is possible for the connection message to be
         * sent before CR has actually received the opened callback, in which
         * case the message will be dropped. so we delay the sending of this
         * message.
         */
        AbstractParticipant.getPeriodicExecutor().schedule(new Runnable() {
          public void run()
          {
            send(new ConnectionRequest(getName(), _type, getCredentials(),
                getAddressingInformation()));

            if (LOGGER.isDebugEnabled())
              LOGGER.debug("Sent connection request");
          }
        }, 50, TimeUnit.MILLISECONDS);
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
        if (session == getSession())
          try
          {
            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format(
                  "[%s] Session closed, stopping and shuting down", getName()));

            if (stateMatches(State.STARTED, State.SUSPENDED)) stop();

            setSession(null);

            if (stateMatches(State.CONNECTED, State.INITIALIZED, State.UNKNOWN,
                State.STOPPED)) shutdown(false);

          }
          catch (Exception e)
          {
            LOGGER.error("Failed to cleanly disconnect from CR", e);
          }
        else if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format(
              "[%s] Spurious closed from another session? %s", getName(),
              session));
      }
    };
  }

  /**
   * specify what transport, protocol and address we can accessed on
   * 
   * @param service
   * @param address
   */
  public void addServerService(IServerService service,
      ITransportProvider transport, IProtocolConfiguration configuration,
      SocketAddress address)
  {
    try
    {
      service.configure(transport, configuration, createDefaultHandlers(),
          createDefaultServerListener(), getIOThreadFactory());
      // if state is anything but unknown, we need to start the service
      if (!stateMatches(State.UNKNOWN)) startService(service, address);
      _services.put(service, address);
    }
    catch (Exception e)
    {
      LOGGER.error("Could not start server service ", e);
    }
  }

  /**
   * specify what transport, protocol and address we can use to connect to
   * another participant (usually, just common reality)
   * 
   * @param service
   * @param address
   */
  public void addClientService(IClientService service,
      ITransportProvider transport, IProtocolConfiguration configuration,
      SocketAddress address)
  {
    try
    {
      service.configure(transport, configuration, createDefaultHandlers(),
          createDefaultClientListener(), getIOThreadFactory());
      // if state is anything but unknown, we need to start the service
      if (!stateMatches(State.UNKNOWN)) startService(service, address);
      _services.put(service, address);
    }
    catch (Exception e)
    {
      LOGGER.error("Could not start server service ", e);
    }
  }

  private void startService(INetworkService service, SocketAddress address)
      throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(getName() + " Starting "
          + service.getClass().getSimpleName() + " on " + address);
    service.start(address);
  }

  private void stopService(INetworkService service, SocketAddress address)
      throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Stopping " + service.getClass().getSimpleName() + " on "
          + address, new RuntimeException());
    service.stop(address);
  }

  @Override
  protected void setState(State state)
  {
    super.setState(state);
    // and send a message, if unknown, we aren't currently connected.
    // just to avoid the message.
    // if (!state.equals(State.UNKNOWN))
    send(new ControlAcknowledgement(getIdentifier(), -1, state));
  }

  /**
   * @bug this starts all services in a random order. it should be server first
   *      then clients to avoid deadlock in complex configurations
   * @see org.commonreality.participant.IParticipant#connect()
   */
  @Override
  public void connect() throws Exception
  {
    checkState(State.UNKNOWN);

    Exception differed = null;
    for (Map.Entry<INetworkService, SocketAddress> entry : _services.entrySet())
      try
      {
        startService(entry.getKey(), entry.getValue());
      }
      catch (Exception e)
      {
        LOGGER.error("Could not start service ", e);
        differed = e;
      }

    if (differed != null) throw differed;

    // moved to setIdentifier()
    // setState(State.CONNECTED);
  }

  @Override
  public void disconnect() throws Exception
  {
    disconnect(false);
  }

  @Override
  public void disconnect(boolean force) throws Exception
  {
    if (!force)
      checkState(State.CONNECTED, State.INITIALIZED, State.STOPPED,
          State.UNKNOWN);

    Exception differed = null;
    for (Map.Entry<INetworkService, SocketAddress> entry : _services.entrySet())
      try
      {
        stopService(entry.getKey(), entry.getValue());
      }
      catch (Exception e)
      {
        LOGGER.error("Could not stop service ", e);
        differed = e;
      }
    _services.clear();

    setState(State.UNKNOWN);

    if (differed != null) throw differed;
  }

  protected Collection<IAddressingInformation> getServerAddressInformation()
  {
    ArrayList<IAddressingInformation> rtn = new ArrayList<IAddressingInformation>();
    for (Map.Entry<INetworkService, SocketAddress> entry : _services.entrySet())
      if (entry.getKey() instanceof IServerService)
        rtn.add(new BasicAddressingInformation(entry.getValue()));
    return rtn;
  }

  /**
   * send a message to common reality. We also cache any data requests going out
   * and store them temporarily. they will not be applied to the respective
   * object manager until we get confirmation from CR
   * 
   * @param message
   */
  @Override
  public Future<IAcknowledgement> send(IMessage message)
  {
    // BasicParticipantIOHandler handler = (BasicParticipantIOHandler)
    // getIOHandler();

    /*
     * anytime we send data out, we store it because we wont actually set the
     * data until we get confirmation back from CR in the form of the
     * IObjectCommand. However, if the data request is going out to everyone (in
     * the case of a RealObject, AgentObject or SensorObject) we will get the
     * data sent to us, so we dont bother storing it (otherwise, we'd get double
     * data)
     */
    if (message instanceof IObjectDataRequest
        && !IIdentifier.ALL.equals(((IObjectDataRequest) message)
            .getDestination()))
      getGeneralObjectHandler().storeObjectData(
          ((IObjectDataRequest) message).getData(), message);

    Future<IAcknowledgement> rtn = null;

    ISessionInfo<?> session = getSession();
    if (session != null)
    {
      // this could be dangerous.. where else do we sync on session?
      synchronized (session)
      {
        if (message instanceof IRequest)
        {
          SessionAcknowledgements sa = SessionAcknowledgements
              .getSessionAcks(session);
          if (sa != null) rtn = sa.newAckFuture(message);
        }
      }

      try
      {
        // not too efficient..

        session.write(message);

        // flush when we send a time request?
        if (shouldFlush(message)) session.flush();
      }
      catch (Exception e)
      {
        // TODO Auto-generated catch block
        LOGGER.error("AbstractParticipant.send threw Exception : ", e);
      }
    }
    else if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("[%s] Null session, could not send %s ",
          getName(), message), new RuntimeException());

    if (rtn == null) rtn = EMPTY_ACK;

    return rtn;
  }

  protected boolean shouldFlush(Object message)
  {
    boolean flush = message instanceof RequestTime
        || message instanceof ConnectionRequest
        || message instanceof NewIdentifierRequest
        || message instanceof NotificationMessage
        || message instanceof IRequest || message instanceof IAcknowledgement;
    return flush;
  }
}
