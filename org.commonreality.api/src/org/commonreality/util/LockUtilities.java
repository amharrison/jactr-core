package org.commonreality.util;

/*
 * default logging
 */
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class LockUtilities
{

  static private boolean             _useInfiniteLocks = !Boolean
                                                           .getBoolean("org.commonreality.lockUtilities.useLimitedLocks");

  static private Map<Lock, LockInfo> _recentLocks      = Collections
                                                           .synchronizedMap(new WeakHashMap<Lock, LockInfo>());

  /**
   * more diagnostics
   * 
   * @return
   */
  static public String getLockInfo()
  {
    StringBuilder sb = new StringBuilder("LockUtilities, tracked locks: \n");
    synchronized (_recentLocks)
    {
      _recentLocks.entrySet().forEach(
          (e) -> {
            sb.append("  lock(").append(e.getKey().getClass().getSimpleName());
            sb.append(")[").append(e.getKey().hashCode())
                .append("] last acquired by ").append(e.getValue())
                .append("\n");
          });
    }

    return sb.toString();
  }

  static public void runLocked(Lock lock, Runnable runnable)
      throws InterruptedException
  {
    boolean locked = false;
    try
    {
      locked = attemptLock(lock, runnable);
      if (locked)
        runnable.run();
      else
        throw new InterruptedException(String.format(
            "%s failed to acquire lock %s, recently acquired by %s",
            Thread.currentThread(), lock, _recentLocks.get(lock)));
    }
    finally
    {
      if (locked) attemptUnlock(lock, runnable);
    }
  }

  static public <T> T runLocked(Lock lock, Callable<T> callable)
      throws InterruptedException, Exception
  {
    boolean locked = false;
    try
    {
      locked = attemptLock(lock, callable);
      if (locked)
        return callable.call();
      else
        throw new InterruptedException(String.format(
            "%s failed to acquire lock %s, recently acquired by %s",
            Thread.currentThread(), lock, _recentLocks.get(lock)));
    }
    finally
    {
      if (locked) attemptUnlock(lock, callable);
    }
  }

  static protected void attemptUnlock(Lock lock, Object with)
  {
    lock.unlock();
    LockInfo li = _recentLocks.remove(lock);
    if (li != null) li.close();
  }

  static protected boolean attemptLock(Lock lock, Object with)
      throws InterruptedException
  {
    boolean rtn = false;
    if (_useInfiniteLocks)
    {
      lock.lock();
      rtn = true;
    }
    else
      rtn = lock.tryLock(1, TimeUnit.MINUTES);
    if (rtn) _recentLocks.put(lock, new LockInfo());
    return rtn;
  }

  static private class LockInfo
  {
    Thread _thread;

    long   _entryTime;

    long   _exitTime;

    public LockInfo()
    {
      _thread = Thread.currentThread();
      _entryTime = System.nanoTime();
    }

    public void close()
    {
      _exitTime = System.nanoTime();
    }

    @Override
    public String toString()
    {
      return String.format("%s enter: %d  exit :%d", _thread, _entryTime,
          _exitTime);
    }
  }
}
