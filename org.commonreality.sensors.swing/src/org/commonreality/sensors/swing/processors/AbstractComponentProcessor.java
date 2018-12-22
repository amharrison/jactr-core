package org.commonreality.sensors.swing.processors;

/*
 * default logging
 */
import java.awt.Component;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.commonreality.sensors.base.IObjectProcessor;
import org.commonreality.sensors.base.PerceptManager;
import org.commonreality.sensors.swing.key.AWTObjectKey;

public abstract class AbstractComponentProcessor implements
    IObjectProcessor<AWTObjectKey>
{

  private WeakHashMap<Component, Boolean> _componentHasChanged = new WeakHashMap<Component, Boolean>();

  private ReentrantReadWriteLock          _mapLock             = new ReentrantReadWriteLock();

  public AbstractComponentProcessor()
  {
  }

  protected void markAsChanged(Component source)
  {
    try
    {
      _mapLock.writeLock().lock();
      _componentHasChanged.put(source, Boolean.TRUE);
    }
    finally
    {
      _mapLock.writeLock().unlock();
    }
  }

  protected void clearChanged(Component source)
  {
    try
    {
      _mapLock.writeLock().lock();

      if (!_componentHasChanged.containsKey(source))
      {
        // add the listener
        attachListener(source);
      }

      _componentHasChanged.put(source, Boolean.FALSE);
    }
    finally
    {
      _mapLock.writeLock().unlock();
    }
  }

  abstract void attachListener(Component component);

  abstract void detachListener(Component component);

  public void deleted(AWTObjectKey objectKey)
  {
    Component component = objectKey.getComponent();
    try
    {
      _mapLock.writeLock().lock();
      _componentHasChanged.remove(component);

      detachListener(component);
    }
    finally
    {
      _mapLock.writeLock().unlock();
    }
  }

  protected boolean hasChanged(Component source)
  {
    try
    {
      _mapLock.readLock().lock();
      return !_componentHasChanged.containsKey(source)
          || _componentHasChanged.get(source);
    }
    finally
    {
      _mapLock.readLock().unlock();
    }
  }

  public void installed(PerceptManager manager)
  {
    // TODO Auto-generated method stub

  }

  public void uninstalled(PerceptManager manager)
  {
    // TODO Auto-generated method stub

  }

  public void configure(Map<String, String> options)
  {
    // noop
  }

}