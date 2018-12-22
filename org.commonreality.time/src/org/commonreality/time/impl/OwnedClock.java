package org.commonreality.time.impl;

import java.util.ArrayList;
/*
 * default logging
 */
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.time.IAuthoritativeClock;

/**
 * a clock that can have one or more owners, as determined by an owner object
 * (usually a thread). An authority is always generated. If no owners are set,
 * anyone can update the time.
 * 
 * @author harrison
 */
public class OwnedClock extends BasicClock
{
  /**
   * Logger definition
   */
  static private final transient Log           LOGGER = LogFactory
                                                          .getLog(OwnedClock.class);

  final private BiConsumer<Double, OwnedClock> _changeNotifier;

  public OwnedClock(double minimumTimeIncrement)
  {
    this(minimumTimeIncrement, null);
  }

  public OwnedClock(double minimumTimeIncrement,
      BiConsumer<Double, OwnedClock> universalNotifier)
  {
    super(true, minimumTimeIncrement);
    _changeNotifier = universalNotifier;
  }

  @Override
  protected IAuthoritativeClock createAuthoritativeClock(BasicClock clock)
  {
    return new OwnedAuthoritativeClock(this);
  }

  public static class OwnedAuthoritativeClock extends BasicAuthoritativeClock
  {

    final private Set<Object>         _ownerKeys;

    final private Map<Object, Double> _requestedTimes;

    final private Set<Object>         _ownersAccountedFor;

    final private Map<Object, Long>   _lastKnownAccess;

    private Object                    _lastKey;

    public OwnedAuthoritativeClock(BasicClock clock)
    {
      super(clock);
      _lastKnownAccess = new HashMap<Object, Long>();
      _ownerKeys = new HashSet<Object>();
      _requestedTimes = new HashMap<Object, Double>();
      _ownersAccountedFor = new HashSet<Object>();
    }

    @Override
    public OwnedClock getDelegate()
    {
      return (OwnedClock) super.getDelegate();
    }

    public void getOwners(final Collection<Object> owners)
    {
      BasicClock.runLocked(getDelegate().getLock(), () -> {
        owners.addAll(_ownerKeys);
      });
    }

    public void addOwner(final Object ownerKey)
    {
      BasicClock.runLocked(getDelegate().getLock(), () -> {
        _ownerKeys.add(ownerKey);
      });
    }

    public boolean hasOwner(final Object ownerKey)
    {
      return BasicClock.runLocked(getDelegate().getLock(), () -> {
        return _ownerKeys.contains(ownerKey);
      });
    }

    public boolean removeOwner(final Object ownerKey)
    {
      OwnedClock delegate = getDelegate();
      boolean wasRemoved = hasOwner(ownerKey);
      boolean mustUpdate = BasicClock
          .runLocked(
              delegate.getLock(),
              () -> {
                boolean wasOwner = _ownerKeys.size() == 0;
                if (!wasOwner)
                {
                  wasOwner = _ownerKeys.remove(ownerKey);
                  if (wasOwner && LOGGER.isDebugEnabled())
                    LOGGER.debug(String.format("[%s] owner was removed.",
                        ownerKey));
                }
                _requestedTimes.remove(ownerKey);
                _ownersAccountedFor.remove(ownerKey);
                return wasOwner && _ownersAccountedFor.containsAll(_ownerKeys);
              });

      if (mustUpdate)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Forcing update of time", ownerKey));
        BasicClock.runLocked(delegate.getLock(), () -> updateTime());

        if (delegate._changeNotifier != null)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Notifying time update "));
          delegate._changeNotifier.accept(delegate.getLocalTime(), delegate);
        }
        else if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Not notifying, no change in time"));
      }

      return wasRemoved;
    }

    public Collection<Object> getUnaccountedForOwners()
    {
      Collection<Object> container = new ArrayList<>();
      BasicClock.runLocked(getDelegate().getLock(), () -> {
        container.addAll(_ownerKeys);
        container.removeAll(_ownersAccountedFor);
      });
      return container;
    }

    public Map<Object, Long> getLastAccessTimes()
    {
      HashMap<Object, Long> container = new HashMap<Object, Long>();
      BasicClock.runLocked(getDelegate().getLock(), () -> {
        container.putAll(_lastKnownAccess);
      });
      return container;
    }

    public Object getLastRequestKey()
    {
      return _lastKey;
    }

    /**
     * run within the lock already
     */
    @Override
    protected boolean requestTimeChange(final double targetTime,
        final Object key)
    {
      _lastKey = key;
      super.requestTimeChange(targetTime, key);
      heardFrom(key, targetTime);
      return _ownersAccountedFor.containsAll(_ownerKeys);
    }

    @Override
    public CompletableFuture<Double> requestAndWaitForTime(double targetTime,
        final Object key)
    {
      _lastKnownAccess.put(key, System.nanoTime());

      final double fTargetTime = BasicClock.constrainPrecision(targetTime);
      OwnedClock bc = getDelegate();
      CompletableFuture<Double> rtn = bc.newFuture(targetTime, bc.getTime());

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Acquiring lock [%s]", bc.getLock()));

      boolean fireNotifier = BasicClock.runLocked(bc.getLock(), () -> {
            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format("Acquired lock [%s]", bc.getLock()));
        if (requestTimeChange(fTargetTime, key))
        {
          updateTime();
          return true;
        }
        return false;
      });

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Released lock [%s]", bc.getLock()));

      if (fireNotifier && bc._changeNotifier != null)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Notifying time update "));
        bc._changeNotifier.accept(bc.getLocalTime(), bc);
      }
      else if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Not notifying, %s",
            bc._changeNotifier == null ? "no one to notify" : "no change"));

      /*
       * if already elapsed, lets fire
       */
      double now = getTime();
      if (now > fTargetTime) bc.fireExpiredFutures(now);

      return rtn.thenApply((t) -> this.getTime());
    }

    @Override
    public CompletableFuture<Double> requestAndWaitForChange(final Object key)
    {
      _lastKnownAccess.put(key, System.nanoTime());

      OwnedClock bc = getDelegate();
      CompletableFuture<Double> rtn = bc.newFuture(Double.NaN, bc.getTime());
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Acquiring lock [%s]", bc.getLock()));
      boolean fireNotifier = BasicClock.runLocked(bc.getLock(), () -> {
            if (LOGGER.isDebugEnabled())
              LOGGER.debug(String.format("Acquired lock [%s]", bc.getLock()));
        if (requestTimeChange(Double.NaN, key))
        {
          updateTime();
          return true;
        }
        return false;
      });
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Releasing lock [%s]", bc.getLock()));

      // notification outside of the lock
      if (fireNotifier && bc._changeNotifier != null)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Notifying time update "));
        bc._changeNotifier.accept(bc.getLocalTime(), bc);
      }
      else if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Not notifying, %s",
            bc._changeNotifier == null ? "noone to notify" : "no change"));

      return rtn.thenApply((now) -> this.getTime());
    }

    /**
     * run in lock
     * 
     * @param clear
     * @return
     */
    private double mininumRequestedTime(final boolean clear)
    {
      double rtn = Double.POSITIVE_INFINITY;
      for (Double request : _requestedTimes.values())
        if (request < rtn) rtn = request;

      if (Double.isInfinite(rtn))
      {
        BasicClock delegate = getDelegate();
        rtn = delegate.getTime() + delegate.getMinimumTimeIncrement();
      }

      /*
       * if we assume that a request is only sent once per cycle, we need to
       * clear those that will wake up from this. We include the infinites, as
       * they will be awaking as well.
       */
      if (clear)
      {
        Iterator<Map.Entry<Object, Double>> timeItr = _requestedTimes
            .entrySet().iterator();
        while (timeItr.hasNext())
        {
          Map.Entry<Object, Double> entry = timeItr.next();
          double triggerTime = entry.getValue();
          if (triggerTime <= rtn || Double.isInfinite(triggerTime))
          {
            _ownersAccountedFor.remove(entry.getKey());
            timeItr.remove();
          }
        }
      }

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format(
            "Minimum time : %.4f, These agents won't wake up this cycle : %s",
            rtn, _requestedTimes));

      return rtn;
    }

    /**
     * actually find the smallest increment to advance, and do so, firing off
     * completions. called in lock
     */
    private void updateTime()
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Heard from all owners"));

      double minimumTime = mininumRequestedTime(true);

      if (Double.isInfinite(minimumTime))
        minimumTime = BasicClock.constrainPrecision(getTime()
            + getDelegate().getMinimumTimeIncrement());

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format("Updating time to %.4f", minimumTime));

      OwnedClock delegate = getDelegate();
      // actually update and fire things off.
      delegate.setLocalTime(minimumTime);
    }

    /**
     * in lock
     * 
     * @param key
     * @param requestedTime
     */
    private void heardFrom(Object key, double requestedTime)
    {
      if (_ownerKeys.contains(key) || _ownerKeys.size() == 0)
      {
        if (Double.isNaN(requestedTime))
          requestedTime = Double.POSITIVE_INFINITY;
        else
          requestedTime = BasicClock.constrainPrecision(requestedTime);

        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Heard from %s, requesting %.4f", key,
              requestedTime));

        _ownersAccountedFor.add(key);
        _requestedTimes.put(key, requestedTime);
      }
      else // not a proper owner.
      if (LOGGER.isDebugEnabled())
        LOGGER
            .debug(String
                .format(
                    "Ignoring: %s tried to update clock to %.2f, but is not a known owner (%s)",
                    key, requestedTime, _ownerKeys));
    }
  }
}
