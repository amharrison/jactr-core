package org.commonreality.time.impl;

/*
 * default logging
 */
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.time.IAuthoritativeClock;

/**
 * General networked clock support. The code to send the network message for a
 * time request is generalized as a {@link Consumer} that accepts a double. When
 * your network code receives an update message, call
 * {@link NetworkedAuthoritativeClock#timeChangeReceived(double)}. This requires
 * reliable transport as the request is only sent once.
 * 
 * @author harrison
 */
public class NetworkedClock extends BasicClock
{
  /**
   * Logger definition
   */
  static private final transient Log               LOGGER = LogFactory
                                                              .getLog(NetworkedClock.class);

  final private BiConsumer<Double, NetworkedClock> _networkSendCommand;

  final private BiConsumer<Double, Double>         _ignoredTimeUpdate;

  private long                                     _preSendTime;

  private long                                     _postSendTime;

  /**
   * @param minimumTimeIncrement
   * @param networkSendCommand
   *          accepts the global (timeshift corrected) target time
   */
  public NetworkedClock(double minimumTimeIncrement,
      BiConsumer<Double, NetworkedClock> networkSendCommand)
  {
    super(true, minimumTimeIncrement);
    _networkSendCommand = networkSendCommand;
    /*
     * if the authority request time be set to trigger, an update comes in, but
     * is still not sufficient, we resent the network request.
     */
    _ignoredTimeUpdate = (trigger, current) -> {
      sendNetworkRequest(trigger);
    };
  }

  @Override
  protected IAuthoritativeClock createAuthoritativeClock(BasicClock clock)
  {
    // return ours instead
    return new NetworkedAuthoritativeClock(this);
  }

  /**
   * called by networked auth when we receive a clock update
   * 
   * @param targetTime
   */
  private void timeChangeReceived(double globalTime)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("global Time update received : %.4f",
          globalTime));
    // set global time and notify
    double currentGlobal = getGlobalTime();
    if (globalTime < currentGlobal)
      LOGGER.warn(String.format(
          "Networked clock got a global time regression! was:%.5f  now:%.5f",
          currentGlobal, globalTime));

    setGlobalTime(globalTime);
  }

  @Override
  protected void futureRejectedCompletion(CompletableFuture<Double> future,
      double triggerTime, double currentTime)
  {
    super.futureRejectedCompletion(future, triggerTime, currentTime);
    sendNetworkRequest(triggerTime);
  }

  protected long getPreSendTime()
  {
    return _preSendTime;
  }

  protected long getPostSendTime()
  {
    return _postSendTime;
  }

  private void sendNetworkRequest(double triggerTime)
  {
    try
    {
      if (_networkSendCommand != null)
      {
        _postSendTime = 0;

        double globalTime = BasicClock.constrainPrecision(triggerTime
            - getTimeShift());
        if (LOGGER.isDebugEnabled())
          LOGGER
.debug(String.format("Sending global clock request %.4f",
              globalTime));
        _preSendTime = System.nanoTime();
        _networkSendCommand.accept(globalTime, this);
        _postSendTime = System.nanoTime();
      }
    }
    catch (Exception e)
    {
      LOGGER.error("Failed to send request ", e);
    }

  }

  static public class NetworkedAuthoritativeClock extends
      BasicAuthoritativeClock
  {

    public NetworkedAuthoritativeClock(BasicClock clock)
    {
      super(clock);
    }

    /**
     * called by the networking code. global time
     * 
     * @param globalTime
     */
    public void timeChangeReceived(double globalTime)
    {
      ((NetworkedClock) getDelegate()).timeChangeReceived(globalTime);
    }

    /**
     * always return false so that we don't fire the update until after we get
     * confirmation.
     */
    @Override
    protected boolean requestTimeChange(double targetTime, Object key)
    {
      // make the necessary calls
      super.requestTimeChange(targetTime, key);

      ((NetworkedClock) getDelegate()).sendNetworkRequest(targetTime);

      return false;
    }

    @Override
    public CompletableFuture<Double> requestAndWaitForTime(double targetTime,
        final Object key)
    {
      final double fTargetTime = BasicClock.constrainPrecision(targetTime);
      BasicClock bc = getDelegate();
      CompletableFuture<Double> rtn = bc.newFuture(targetTime, bc.getTime());
      // we don't need this and we can't call from within the
      // lock w/o causing serious problems.
      // BasicClock.runLocked(bc.getLock(), () -> {
      // if (requestTimeChange(fTargetTime, key)) bc.setLocalTime(fTargetTime);
      // });
      requestTimeChange(fTargetTime, key);

      return rtn.thenApply((t) -> getDelegate().getTime());
    }

    @Override
    public CompletableFuture<Double> requestAndWaitForChange(final Object key)
    {
      BasicClock bc = getDelegate();
      CompletableFuture<Double> rtn = bc.newFuture(Double.NaN, bc.getTime());
      // BasicClock.runLocked(
      // bc.getLock(),
      // () -> {
      // if (requestTimeChange(Double.NaN, key))
      // bc.setLocalTime(getTime() + bc._minimumTimeIncrement);
      // });
      requestTimeChange(Double.NaN, key);
      return rtn.thenApply((t) -> getDelegate().getTime());
    }

  }
}
