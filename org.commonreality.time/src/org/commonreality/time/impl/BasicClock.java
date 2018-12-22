package org.commonreality.time.impl;

import java.util.ArrayList;
/*
 * default logging
 */
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.time.IAuthoritativeClock;
import org.commonreality.time.IClock;
import org.commonreality.util.LockUtilities;

public class BasicClock implements IClock
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(BasicClock.class);

  static private final double        PRECISION;

  static private final double        MINIMUM_INCREMENT;

  static
  {
    // 0.0001 1/10th millisecond
    int precisionDigits = 4;
    try
    {
      precisionDigits = Integer.parseInt(System
          .getProperty("commonreality.temporalPrecisionDigits"));
    }
    catch (Exception e)
    {
      precisionDigits = 4;
    }

    PRECISION = Math.round(Math.pow(10, precisionDigits));
    MINIMUM_INCREMENT = 1 / PRECISION;
  }

  /**
   * constrain our precision.
   * 
   * @param time
   * @return
   */
  static public double constrainPrecision(double time)
  {
    return Math.ceil(time * PRECISION) / PRECISION;
    // return time;
  }

  static public double getPrecision()
  {
    return MINIMUM_INCREMENT;
  }

  private volatile double                        _globalTime;                                // unshifted
                                                                                              // time

  private double                                 _timeShift;                                 // time
                                                                                              // correction.

  private Lock                                   _lock                 = new ReentrantLock();

  private Map<CompletableFuture<Double>, Double> _pendingCompletables;

  private IAuthoritativeClock                    _authoritative;

  private double                                 _minimumTimeIncrement = 0.05;

  /**
   * diagnostic variables for interrogation in case something goes wrong.
   */
  private volatile long                          _lastUpdateSystemTime;

  private volatile double                        _lastUpdateLocalTimeValue;

  private volatile double                        _lastRequestedLocalTimeValue;

  private volatile long                          _lastRequestSystemTime;

  private volatile Thread                        _lastRequestingThread;

  public BasicClock()
  {
    this(false, 0.05);
  }

  public BasicClock(boolean provideAuthority, final double minimumTimeIncrement)
  {
    _minimumTimeIncrement = minimumTimeIncrement;
    _pendingCompletables = new HashMap<CompletableFuture<Double>, Double>();
    if (provideAuthority) _authoritative = createAuthoritativeClock(this);
  }

  public double getMinimumTimeIncrement()
  {
    return _minimumTimeIncrement;
  }

  /**
   * hook to create an authoritative
   * 
   * @param clock
   * @return
   */
  protected IAuthoritativeClock createAuthoritativeClock(BasicClock clock)
  {
    return new BasicAuthoritativeClock(this);
  }

  /**
   * exposed for diagnostic support
   * 
   * @return
   */
  protected long getLastUpdateSystemTime()
  {
    return _lastUpdateSystemTime;
  }

  /**
   * exposed for diagnostic support
   * 
   * @return
   */
  protected double getLastUpdateLocalTime()
  {
    return _lastUpdateLocalTimeValue;
  }

  protected double getLastRequestLocalTime()
  {
    return _lastRequestedLocalTimeValue;
  }

  protected long getLastRequestSystemTime()
  {
    return _lastRequestSystemTime;
  }

  protected Thread getLastRequestingThread()
  {
    return _lastRequestingThread;
  }

  static protected void runLocked(Lock lock, Runnable r)
  {
    try
    {
      LockUtilities.runLocked(lock, r);
    }
    catch (InterruptedException e)
    {
      LOGGER.error(String.format("%s lock[%d] threw exception %s ", r,
          lock.hashCode(), e.getMessage()), e);
      LOGGER.error("Available clock information : "
          + ClockInterrogator.getAllClockDetails());
      throw new RuntimeException(e);
    }
  }

  static protected <T> T runLocked(Lock lock, Callable<T> c)
  {
    try
    {
      T rtn = LockUtilities.runLocked(lock, c);
      return rtn;
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      LOGGER.error(String.format("%s lock[%d] threw exception %s ", c,
          lock.hashCode(), e.getMessage()), e);
      LOGGER.error("Available clock information : "
          + ClockInterrogator.getAllClockDetails());
      throw new RuntimeException(e);
    }

    // T rtn = null;
    // try
    // {
    // lock.lock();
    // rtn = c.call();
    // return rtn;
    // }
    // catch (Exception e)
    // {
    // LOGGER.error(
    // "Exception occured while processing callable within clock lock ", e);
    // return null;
    // }
    // finally
    // {
    // lock.unlock();
    // }
  }

  @Override
  public double getTime()
  {
    try
    {
      return runLocked(_lock, () -> {
        return getLocalTime();
      });
    }
    catch (Exception e)
    {
      LOGGER.error(e);
      return Double.NaN;
    }
  }

  @Override
  public Optional<IAuthoritativeClock> getAuthority()
  {
    return Optional.ofNullable(_authoritative);
  }

  @Override
  public CompletableFuture<Double> waitForChange()
  {
    return newFuture(Double.NaN, getTime()).thenApply(
        (now) -> BasicClock.constrainPrecision(now + _timeShift));
  }

  @Override
  public CompletableFuture<Double> waitForTime(double triggerTime)
  {
    final double fTriggerTime = BasicClock.constrainPrecision(triggerTime);
    double now = getTime();
    boolean hasPassed = now >= fTriggerTime;

    CompletableFuture<Double> rtn = newFuture(fTriggerTime, now);

    if (hasPassed)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn(String.format("time has already elapsed, forcing firing"));
      fireExpiredFutures(now);
    }

    return rtn.thenApply((n) -> BasicClock.constrainPrecision(n + _timeShift));
  }

  /**
   * runs unlocked.
   * 
   * @param timeShift
   */
  protected void setTimeShift(double timeShift)
  {
    _timeShift = timeShift;
  }

  /**
   * returns w/o locking
   * 
   * @return
   */
  protected double getTimeShift()
  {
    return _timeShift;
  }

  /**
   * returns the local time w/o locking
   * 
   * @return
   */
  protected double getLocalTime()
  {
    return constrainPrecision(_globalTime + _timeShift);
  }

  /**
   * return global w/o locking.
   * 
   * @return
   */
  protected double getGlobalTime()
  {
    return _globalTime;
  }

  protected Lock getLock()
  {
    return _lock;
  }

  /**
   * create a new future that will be completed at or after trigger time
   * (Double.NaN if any change).
   * 
   * @param targetTime
   * @return
   */
  protected CompletableFuture<Double> newFuture(final double targetTime,
      final double requestingTime)
  {
    final CompletableFuture<Double> rtn = new CompletableFuture<Double>() {
      @Override
      public boolean complete(Double now)
      {
        if (Double.isNaN(targetTime)) /*
         * here's a question, this means that waitForChange is actually:
         * waitForIncrement. Should there be both literal methods?
         */
        // did this actually change?
        if (now <= requestingTime)
        {
          if (LOGGER.isDebugEnabled()) LOGGER.debug("",
              new RuntimeException(String.format(
                                  "%s Time hasn't incremented since %.4f, waitForAny completion. Left as pending",
                                  this, requestingTime)));
          return false;
        }
        else
          return super.complete(now);

        if (targetTime - now > MINIMUM_INCREMENT)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER
                .debug(
                    "",
                    new RuntimeException(
                        String
                            .format(
                                "%s Timed hasn't incremented, waitFor(%.5f)  current(%.5f). Left as pending",
                                this, targetTime, now)));
          return false;
        }
        else
        {
          boolean rtn = super.complete(targetTime);

          if (!rtn && LOGGER.isDebugEnabled())
            LOGGER
                .debug(String
                    .format(
                        "%s rejected completion? requestedAt:%.4f target:%.4f current:%.4f",
                        this, requestingTime, targetTime, now));

          return rtn;
        }
      }
    };

    runLocked(
        _lock,
        () -> {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("[%d].waitFor(%.4f)", rtn.hashCode(),
                targetTime));
          _pendingCompletables.put(rtn, targetTime);
        });

    return rtn;
  }

  /**
   * set the local time (indirectly setting global time) and fire completion.
   * Functionally the same as {@link #setGlobalTime(double)}
   * 
   * @param currentLocalTime
   */
  protected void setLocalTime(double currentLocalTime)
  {
    final double fCurrentLocalTime = BasicClock
        .constrainPrecision(currentLocalTime);
    double localTime = 0;
    try
    {
      localTime = runLocked(
          _lock,
          () -> {
            _lastUpdateSystemTime = System.nanoTime();
            _lastUpdateLocalTimeValue = fCurrentLocalTime;
            _globalTime = fCurrentLocalTime - _timeShift;
            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format("Time[%.2f, %.2f, %.2f]", _globalTime,
                  fCurrentLocalTime, _timeShift));
            return fCurrentLocalTime;
          });
    }
    catch (Exception e)
    {
      LOGGER.error(e);
      localTime = fCurrentLocalTime;
    }

    fireExpiredFutures(localTime);
  }

  /**
   * set the global time value, firing any pending completables.
   * 
   * @param globalTime
   */
  protected void setGlobalTime(double globalTime)
  {
    final double fCurrentGlobalTime = BasicClock.constrainPrecision(globalTime);
    double localTime = 0;
    try
    {
      localTime = runLocked(
          _lock,
          () -> {
            _lastUpdateSystemTime = System.nanoTime();
            _lastUpdateLocalTimeValue = fCurrentGlobalTime + _timeShift;
            _globalTime = fCurrentGlobalTime;
            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format("Time[%.4f, %.4f, %.4f]", _globalTime,
                  _globalTime + _timeShift, _timeShift));
            return getLocalTime();
          });
    }
    catch (Exception e)
    {
      LOGGER.error(e);
      localTime = getLocalTime(); // just in case.
    }

    fireExpiredFutures(localTime);
  }

  protected void fireExpiredFutures(double localTime)
  {
    // FastList<CompletableFuture<Double>> container = FastList.newInstance();

    // get and remove
    removeExpiredCompletables(localTime);

    // FastList.recycle(container);
  }

  protected Collection<CompletableFuture<Double>> removeExpiredCompletables(
      final double now)
  {
    final List<Map.Entry<CompletableFuture<Double>, Double>> pending = new ArrayList<>();

    // grab the pending from the lock.. we must do the processing outside of the
    // lock.
    runLocked(_lock, () -> {
      pending.addAll(_pendingCompletables.entrySet());
    });

    Iterator<Map.Entry<CompletableFuture<Double>, Double>> itr = pending
        .iterator();
    while (itr.hasNext())
    {
      Map.Entry<CompletableFuture<Double>, Double> entry = itr.next();
      double trigger = entry.getValue();
      CompletableFuture<Double> future = entry.getKey();

      if (Double.isNaN(trigger) || trigger <= now)
      {
        if (future.isDone())
        {
          // cancelations are to be expected, but not exceptionally, nor normal
          if (!future.isCancelled())
          {
            if (LOGGER.isWarnEnabled())
              LOGGER.warn(String.format(
                  "Future was already completed? [%s].trigger=%.4f @ %.4f",
                  future, trigger, now));
          }
          else if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format(
                "Future was canceled [%s].trigger=%.4f @ %.4f",
                  future, trigger, now));

          // leave it. so that we can remove it further down
        }
        else if (!future.complete(now))
        {
          if (!future.isCancelled())
          {
            itr.remove(); // we remove all but the actually compelted
            futureRejectedCompletion(future, trigger, now);
          }
        }
        else if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Completed [%s].trigger = %.4f", future,
              trigger));
      }
      else
      {
        boolean closeEnough = Math.abs(trigger - now) <= MINIMUM_INCREMENT;
        if (closeEnough)
          future.complete(trigger);
        else
        {
          futureStillPending(future, trigger, now);
          itr.remove();
        }
      }
    }

    /*
     * and remove those that completed.
     */
    runLocked(_lock, () -> {
      pending.forEach((e) -> _pendingCompletables.remove(e.getKey()));
    });

    

    return null;
  }

  protected void futureRejectedCompletion(CompletableFuture<Double> future,
      double triggerTime, double currentTime)
  {
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug(String.format("Rejected completion? t: %.5f c:%.5f [%s]",
          triggerTime, currentTime, future));
      LOGGER
          .debug(String
              .format("future didn't actually complete. Normal if time hasn't actually changed. But ideally this shouldn't happen"));
    }
  }

  protected void futureStillPending(CompletableFuture<Double> future,
      double triggerTime, double currentTime)
  {
    double delta = triggerTime - currentTime;

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format(
          "Pending completion t: %.5f c:%.5f [%s]  delta:%.9f, belowThresh=%s",
          triggerTime, currentTime, future, delta, delta < MINIMUM_INCREMENT));
  }

  /**
   * A starter authoritative for the basic clock. One can extend this by merely
   * adapting {@link #requestTimeChange(double)}
   * 
   * @author harrison
   */
  public static class BasicAuthoritativeClock extends
      AbstractAuthoritativeClock
  {

    public BasicAuthoritativeClock(BasicClock clock)
    {
      super(clock);
    }

    @Override
    public BasicClock getDelegate()
    {
      return (BasicClock) super.getDelegate();
    }

    @Override
    public void setLocalTimeShift(final double timeShift)
    {
      BasicClock bc = getDelegate();
      BasicClock.runLocked(bc._lock, () -> {
        bc.setTimeShift(timeShift);
      });
    }

    /**
     * request that the time be changed. Returns true if we can proceed and use
     * the targetTime value. This impl merely returns true.
     * 
     * @param targetTime
     * @return
     */
    protected boolean requestTimeChange(double targetTime, Object key)
    {
      BasicClock bc = getDelegate();
      bc._lastRequestedLocalTimeValue = targetTime;
      bc._lastRequestSystemTime = System.nanoTime();
      bc._lastRequestingThread = Thread.currentThread();
      return true;
    }

    @Override
    public CompletableFuture<Double> requestAndWaitForTime(double targetTime,
        final Object key)
    {
      final double fTargetTime = BasicClock.constrainPrecision(targetTime);
      BasicClock bc = getDelegate();
      CompletableFuture<Double> rtn = bc.newFuture(targetTime, bc.getTime());
      BasicClock.runLocked(bc.getLock(), () -> {
        if (requestTimeChange(fTargetTime, key)) bc.setLocalTime(fTargetTime);
      });
      return rtn.thenApply((now) -> getTime());
    }

    @Override
    public CompletableFuture<Double> requestAndWaitForChange(final Object key)
    {
      BasicClock bc = getDelegate();
      CompletableFuture<Double> rtn = bc.newFuture(Double.NaN, bc.getTime());
      BasicClock.runLocked(
          bc.getLock(),
          () -> {
            if (requestTimeChange(Double.NaN, key))
              bc.setLocalTime(getTime() + bc._minimumTimeIncrement);
          });
      return rtn.thenApply((now) -> getTime());
    }

    @Override
    public double getLocalTimeShift()
    {
      final BasicClock bc = getDelegate();
      return BasicClock.runLocked(bc._lock, () -> {
        return bc.getTimeShift();
      });
    }
  }
}
