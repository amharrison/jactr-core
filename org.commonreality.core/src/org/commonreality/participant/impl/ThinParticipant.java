package org.commonreality.participant.impl;

/*
 * default logging
 */
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.IEfferentCommandManager;
import org.commonreality.efferent.impl.EfferentCommandManager;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IAcknowledgement;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.credentials.ICredentials;
import org.commonreality.notification.INotificationManager;
import org.commonreality.notification.impl.NotificationManager;
import org.commonreality.object.manager.IAfferentObjectManager;
import org.commonreality.object.manager.IAgentObjectManager;
import org.commonreality.object.manager.IEfferentObjectManager;
import org.commonreality.object.manager.IMutableObjectManager;
import org.commonreality.object.manager.IObjectManager;
import org.commonreality.object.manager.IRealObjectManager;
import org.commonreality.object.manager.ISensorObjectManager;
import org.commonreality.object.manager.impl.AfferentObjectManager;
import org.commonreality.object.manager.impl.AgentObjectManager;
import org.commonreality.object.manager.impl.EfferentObjectManager;
import org.commonreality.object.manager.impl.RealObjectManager;
import org.commonreality.object.manager.impl.SensorObjectManager;
import org.commonreality.participant.IParticipant;
import org.commonreality.time.IAuthoritativeClock;
import org.commonreality.time.IClock;

public class ThinParticipant implements IParticipant
{

  /**
   * logger definition
   */
  static final Log                                  LOGGER       = LogFactory
                                                                     .getLog(ThinParticipant.class);

  static public CompletableFuture<IAcknowledgement> EMPTY_ACK;

  static
  {
    EMPTY_ACK = new CompletableFuture<IAcknowledgement>();
    EMPTY_ACK.complete(null);
  }

  private IIdentifier                               _identifier;

  protected volatile State                          _state;

  private Lock                                      _stateLock   = new ReentrantLock();

  private Condition                                 _stateChange = _stateLock
                                                                     .newCondition();

  private IClock                                    _clock;

  protected ISensorObjectManager                    _sensorManager;

  protected IAgentObjectManager                     _agentManager;

  protected IAfferentObjectManager                  _afferentManager;

  protected IEfferentObjectManager                  _efferentManager;

  protected IRealObjectManager                      _realManager;

  protected IEfferentCommandManager                 _efferentCommandManager;

  protected INotificationManager                    _notificationManager;

  protected final IIdentifier.Type                  _type;

  private ICredentials                              _credentials;

  public ThinParticipant(IIdentifier.Type type)
  {
    _type = type;
    _sensorManager = createSensorObjectManager();
    _agentManager = createAgentObjectManager();
    _afferentManager = createAfferentObjectManager();
    _efferentManager = createEfferentObjectManager();
    _efferentCommandManager = createEfferentCommandManager();
    _realManager = createRealObjectManager();
    _notificationManager = createNotificationManager();
    _state = State.UNKNOWN;
  }

  public void setCredentials(ICredentials credentials)
  {
    _credentials = credentials;
  }

  public ICredentials getCredentials()
  {
    return _credentials;
  }

  protected ISensorObjectManager createSensorObjectManager()
  {
    return new SensorObjectManager();
  }

  protected IRealObjectManager createRealObjectManager()
  {
    return new RealObjectManager();
  }

  protected IAgentObjectManager createAgentObjectManager()
  {
    return new AgentObjectManager();
  }

  protected IAfferentObjectManager createAfferentObjectManager()
  {
    return new AfferentObjectManager();
  }

  protected IEfferentObjectManager createEfferentObjectManager()
  {
    return new EfferentObjectManager();
  }

  protected IEfferentCommandManager createEfferentCommandManager()
  {
    return new EfferentCommandManager();
  }

  protected INotificationManager createNotificationManager()
  {
    return new NotificationManager(this);
  }

  public INotificationManager getNotificationManager()
  {
    return _notificationManager;
  }

  public IEfferentCommandManager getEfferentCommandManager()
  {
    return _efferentCommandManager;
  }

  public IRealObjectManager getRealObjectManager()
  {
    return _realManager;
  }

  protected void setState(State state)
  {
    try
    {
      _stateLock.lock();
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(getClass().getSimpleName() + " Setting state to " + state
            + " from " + _state);
      _state = state;
      _stateChange.signalAll();
    }
    finally
    {
      _stateLock.unlock();
    }
  }

  public final State waitForState(State... states) throws InterruptedException
  {
    return waitForState(0, states);
  }

  private boolean matches(State... states)
  {
    for (State test : states)
      if (test == _state) return true;
    return false;
  }

  public final State waitForState(long waitTime, State... states)
      throws InterruptedException
  {
    try
    {
      _stateLock.lock();

      if (waitTime <= 0)
        while (!matches(states))
          _stateChange.await();
      else
      {
        long endTime = System.currentTimeMillis() + waitTime;
        while (!matches(states) && System.currentTimeMillis() < endTime)
          _stateChange.await(waitTime, TimeUnit.MILLISECONDS);
      }

      return _state;
    }
    finally
    {
      _stateLock.unlock();
    }
  }

  public final State getState()
  {
    try
    {
      _stateLock.lock();
      return _state;
    }
    finally
    {
      _stateLock.unlock();
    }
  }

  public boolean stateMatches(State... states)
  {
    State state = getState();
    for (State test : states)
      if (state == test) return true;
    return false;
  }

  /**
   * checks the state to see if it matches one of these, if not, it fires an
   * exception
   * 
   * @param states
   */
  protected void checkState(State... states)
  {
    State state = getState();
    StringBuilder sb = new StringBuilder("(");
    for (State test : states)
      if (test == state)
        return;
      else
        sb.append(test).append(", ");

    if (sb.length() > 1) sb.delete(sb.length() - 2, sb.length());
    sb.append(")");

    throw new IllegalStateException("Current state (" + state
        + ") is invalid, expecting " + sb);
  }

  /**
   * called after the connection has been established..
   * 
   * @param identifier
   */
  public void setIdentifier(IIdentifier identifier)
  {
    if (getIdentifier() != null)
      throw new RuntimeException("identifier is already set");
    _identifier = identifier;

    setState(State.CONNECTED);
  }

  /**
   * we don't have a valid identifier until we have connected to reality
   * 
   * @see org.commonreality.identifier.IIdentifiable#getIdentifier()
   */
  public IIdentifier getIdentifier()
  {
    return _identifier;
  }

  public void configure(Map<String, String> options) throws Exception
  {
    checkState(State.CONNECTED, State.INITIALIZED, State.STOPPED, State.UNKNOWN);
  }

  /**
   * called in response to a command from Reality to get everything ready to
   * run. we must be connected first.
   */
  public void initialize() throws Exception
  {
    checkState(State.CONNECTED);

    setState(State.INITIALIZED);
  }

  /**
   * called to actually start this participant
   */
  public void start() throws Exception
  {
    checkState(State.INITIALIZED);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Started " + getClass().getSimpleName());
    setState(State.STARTED);
  }

  /**
   * called when this participant needs to stop
   */
  public void stop() throws Exception
  {
    checkState(State.STARTED, State.SUSPENDED);
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Stopped " + getClass().getSimpleName());
    setState(State.STOPPED);
  }

  public void suspend() throws Exception
  {
    checkState(State.STARTED);
    setState(State.SUSPENDED);
  }

  public void resume() throws Exception
  {
    checkState(State.SUSPENDED);
    setState(State.STARTED);
  }

  /**
   * called when we are to reset to a post-initialize state. this impl attempts
   * to reset the clock if it is INetworked or ISettabl
   */
  public void reset(boolean clockWillBeReset) throws Exception
  {
    checkState(State.STOPPED, State.INITIALIZED);

    if (clockWillBeReset)
    {
      IClock clock = getClock();
      IAuthoritativeClock ac = clock.getAuthority().get(); // this better be
                                                           // here

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Requesting time reset"));

      // no key awareness needed
      ac.requestAndWaitForTime(0, null).handle((d, e) -> {
        if (e != null)
          LOGGER.error("Failed to reset clock ", e);
        else if (LOGGER.isDebugEnabled()) LOGGER.debug("Time reset");
        return null;
      });
    }

    setState(State.INITIALIZED);
  }

  public void shutdown(boolean force) throws Exception
  {
    if (!force)
      checkState(State.STOPPED, State.CONNECTED, State.INITIALIZED,
          State.UNKNOWN);

    try
    {
      disconnect(force);
    }
    catch (Exception e)
    {
      LOGGER.error("Exception ", e);
    }

    clearObjectManagers();

  }

  /**
   * 
   */
  public void shutdown() throws Exception
  {
    shutdown(false);
  }

  /**
   * 
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected void clearObjectManagers()
  {
    IObjectManager om = getAfferentObjectManager();
    if (om instanceof IMutableObjectManager)
      ((IMutableObjectManager) om).remove(om.getIdentifiers());

    om = getEfferentCommandManager();
    if (om instanceof IMutableObjectManager)
      ((IMutableObjectManager) om).remove(om.getIdentifiers());

    om = getEfferentObjectManager();
    if (om instanceof IMutableObjectManager)
      ((IMutableObjectManager) om).remove(om.getIdentifiers());

    om = getAgentObjectManager();
    if (om instanceof IMutableObjectManager)
      ((IMutableObjectManager) om).remove(om.getIdentifiers());

    om = getSensorObjectManager();
    if (om instanceof IMutableObjectManager)
      ((IMutableObjectManager) om).remove(om.getIdentifiers());
  }

  /**
   * return the clock that this participant has access to
   * 
   * @return
   */
  public IClock getClock()
  {
    return _clock;
  }

  public void setClock(IClock clock)
  {
    _clock = clock;
  }

  public ISensorObjectManager getSensorObjectManager()
  {
    return _sensorManager;
  }

  public IAfferentObjectManager getAfferentObjectManager()
  {
    return _afferentManager;
  }

  public IEfferentObjectManager getEfferentObjectManager()
  {
    return _efferentManager;
  }

  public IAgentObjectManager getAgentObjectManager()
  {
    return _agentManager;
  }

  @Override
  public void connect() throws Exception
  {

  }

  @Override
  public void disconnect() throws Exception
  {
    disconnect(false);
  }

  @Override
  public void disconnect(boolean force) throws Exception
  {

  }

  @Override
  public Future<IAcknowledgement> send(IMessage message)
  {
    LOGGER.debug(String.format("Noop send %s", message));
    return EMPTY_ACK;
  }

}