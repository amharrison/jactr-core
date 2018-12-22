package org.commonreality.time.impl;

/*
 * default logging
 */
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.time.IAuthoritativeClock;
import org.commonreality.time.IClock;

/**
 * wrapped clock is not currently working as implemented. While it should be
 * routing everything to the delegate, it is also intended to support a local
 * time shift that is not passed on to the delegate.
 * 
 * @author harrison
 */
public class WrappedClock implements IClock
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER     = LogFactory
                                                    .getLog(WrappedClock.class);

  private final IClock               _delegate;

  private double                     _timeShift = 0;

  private WrappedAuthority           _authority;

  public WrappedClock(IClock master)
  {
    _delegate = master;
    Optional<IAuthoritativeClock> auth = master.getAuthority();
    if (auth.isPresent()) _authority = new WrappedAuthority(auth.get());

  }

  public IClock getDelegate()
  {
    return _delegate;
  }

  @Override
  public double getTime()
  {
    return _delegate.getTime() + _timeShift;
  }

  @Override
  public Optional<IAuthoritativeClock> getAuthority()
  {
    if (_authority == null) return Optional.empty();
    return Optional.of(_authority);
  }

  @Override
  public CompletableFuture<Double> waitForChange()
  {
    return _delegate.waitForChange().thenApply((now) -> this.getTime());
  }

  @Override
  public CompletableFuture<Double> waitForTime(double triggerTime)
  {
    // correct for time shift..
    return _delegate.waitForTime(triggerTime - _timeShift).thenApply(
        (now) -> this.getTime());
  }

  public class WrappedAuthority implements IAuthoritativeClock
  {

    IAuthoritativeClock _delegate;

    public WrappedAuthority(IAuthoritativeClock clock)
    {
      _delegate = clock;
    }

    @Override
    public double getTime()
    {
      return _delegate.getTime() + _timeShift;
    }

    @Override
    public Optional<IAuthoritativeClock> getAuthority()
    {
      return Optional.of(this);
    }

    @Override
    public CompletableFuture<Double> waitForChange()
    {
      return _delegate.waitForChange().thenApply((now) -> this.getTime());
    }

    @Override
    public CompletableFuture<Double> waitForTime(double triggerTime)
    {
      return _delegate.waitForTime(triggerTime - _timeShift).thenApply(
          (now) -> this.getTime());
    }

    @Override
    public CompletableFuture<Double> requestAndWaitForChange(Object key)
    {
      return _delegate.requestAndWaitForChange(key).thenApply(
          (now) -> this.getTime());
    }

    @Override
    public CompletableFuture<Double> requestAndWaitForTime(double targetTime,
        Object key)
    {
      return _delegate.requestAndWaitForTime(targetTime - _timeShift, key)

      .thenApply((now) -> this.getTime());
    }

    @Override
    public double getLocalTimeShift()
    {
      return _timeShift;
    }

    @Override
    public void setLocalTimeShift(double timeShift)
    {
      _timeShift = timeShift;
    }

  }
}
