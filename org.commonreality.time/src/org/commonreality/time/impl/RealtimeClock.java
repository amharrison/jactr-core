package org.commonreality.time.impl;

/*
 * default logging
 */
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * realtime clock that provides no authority. The #
 * 
 * @author harrison
 */
public class RealtimeClock extends BasicClock
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER          = LogFactory
                                                         .getLog(RealtimeClock.class);

  private ScheduledExecutorService   _executor;

  private long                       _clockStartTime = -1;

  /**
   * @param executor
   *          may be null if {@link #waitForChange()},
   *          {@link #waitForTime(double)} are never called. (or their
   *          authorative versions)
   */
  public RealtimeClock(ScheduledExecutorService executor)
  {
    super(false, 0.05);
    _executor = executor;
  }

  @Override
  protected void setGlobalTime(final double globalTime)
  {
    // noop
  }

  @Override
  protected void setLocalTime(final double currentLocalTime)
  {
    // noop
  }

  /**
   * start the realtime clock. That is, make the current time equal 0
   */
  public void start()
  {
    _clockStartTime = System.currentTimeMillis();
  }

  /**
   * overridden to avoid any locks..
   */
  @Override
  public double getTime()
  {
    /*
     * get both the global time and the time shift from within lock
     */
    double[] timeVars = BasicClock.runLocked(
        getLock(),
        () -> {
          if (_clockStartTime < 0)
            _clockStartTime = System.currentTimeMillis();

          double globalTime = BasicClock.constrainPrecision((System
              .currentTimeMillis() - _clockStartTime) / 1000.0);
          double timeShift = getTimeShift();

          return new double[] { globalTime, timeShift };
        });

    return timeVars[0] + timeVars[1];
  }

  @Override
  public CompletableFuture<Double> waitForChange()
  {
    CompletableFuture<Double> rtn = newFuture(Double.NaN, getTime());
    fireExpiredFutures(getTime());
    return rtn;
  }

  @Override
  public CompletableFuture<Double> waitForTime(double triggerTime)
  {
    final double fTriggerTime = BasicClock.constrainPrecision(triggerTime);
    CompletableFuture<Double> rtn = newFuture(
        BasicClock.constrainPrecision(triggerTime), getTime());

    double secondsInFuture = triggerTime - getTime();
    long ms = (long) Math.max(secondsInFuture * 1000, 0);

    /**
     * due to inaccuracies in the timer, we don't fire expired futures using
     * getTime(), but the trigger Time
     */
    _executor.schedule(() -> {
      fireExpiredFutures(fTriggerTime);
    }, ms, TimeUnit.MILLISECONDS);
    return rtn;
  }

}
