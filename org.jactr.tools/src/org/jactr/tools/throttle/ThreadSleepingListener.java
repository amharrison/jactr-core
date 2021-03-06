package org.jactr.tools.throttle;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.model.event.ModelEvent;
import org.jactr.core.model.event.ModelListenerAdaptor;

public class ThreadSleepingListener extends ModelListenerAdaptor
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ThreadSleepingListener.class);

  private final long                 _minimumTimeMS;

  private long                       _cycleStartTime;

  public ThreadSleepingListener(double miniumTime)
  {
    _minimumTimeMS = (long) (miniumTime * 1000);
  }

  @Override
  public void cycleStarted(ModelEvent me)
  {
    _cycleStartTime = me.getSystemTime();
  }

  @Override
  public void cycleStopped(ModelEvent me)
  {
    long delta = _minimumTimeMS - (me.getSystemTime() - _cycleStartTime);
    if (delta > 0)
      try
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Sleeping %d ms", delta));
        Thread.sleep(delta);
      }
      catch (Exception e)
      {

      }
  }
}
