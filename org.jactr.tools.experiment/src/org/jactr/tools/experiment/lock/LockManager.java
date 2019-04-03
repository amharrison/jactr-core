package org.jactr.tools.experiment.lock;

/*
 * default logging
 */
import java.util.Map;
import java.util.TreeMap;

 
import org.slf4j.LoggerFactory;

public class LockManager
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(LockManager.class);

  private Map<String, Boolean>       _locks;

  public LockManager()
  {
    _locks = new TreeMap<String, Boolean>();
  }

  synchronized public boolean lockExists(String name)
  {
    return _locks.containsKey(name.toLowerCase());
  }

  synchronized public boolean isLocked(String name)
  {
    name = name.toLowerCase();
    if (!lockExists(name)) return true;

    return _locks.get(name.toLowerCase());
  }

  synchronized public void lock(String name)
  {
    _locks.put(name.toLowerCase(), Boolean.TRUE);
  }

  synchronized public void unlock(String name)
  {
    _locks.put(name.toLowerCase(), Boolean.FALSE);
  }
}
