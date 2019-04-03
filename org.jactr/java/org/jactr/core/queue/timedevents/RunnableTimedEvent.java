package org.jactr.core.queue.timedevents;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class RunnableTimedEvent extends AbstractTimedEvent
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(RunnableTimedEvent.class);

  private final Runnable _onFire;
  private final Runnable _onAbort;
  
  
  public RunnableTimedEvent(double fireAt, Runnable onFire)
  {
    this(fireAt, onFire, null);
  }
  
  public RunnableTimedEvent(double fireAt, Runnable onFire, Runnable onAbort)
  {
    super(fireAt, fireAt);
    _onFire = onFire;
    _onAbort = onAbort;
  }
  
  @Override
  public void fire(double currentTime)
  {
    super.fire(currentTime);
    if(_onFire!=null)
      _onFire.run();
  }
  
  @Override
  public void abort()
  {
    super.abort();
    if(_onAbort!=null)
      _onAbort.run();
  }

  @Override
  public String toString()
  {
    /*
     * no need to synchornize
     */
    if (_toString == null)
      _toString = String.format("RunnableTE[%s](@ %.2f)", _onFire
          .getClass().getName(), _endTime);
    return _toString;
  }
}
