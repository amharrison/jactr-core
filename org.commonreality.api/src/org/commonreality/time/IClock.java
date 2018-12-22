package org.commonreality.time;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Clock interface that permits notifications of changes or time triggers.
 * 
 * @author harrison
 */
public interface IClock
{

  /**
   * the current time according to this clock
   * 
   * @return
   */
  public double getTime();

  /**
   * Most clock instances are strictly passive, however some clock instances
   * need to be controlled, allowing the caller to set the time to specific
   * values. The authoritative clock (if available) makes that possible.
   * 
   * @return
   */
  public Optional<IAuthoritativeClock> getAuthority();

  /**
   * Return a future representing the future change of the clock from its value
   * at the time of calling. Callers may use this synchronously
   * {@link CompletableFuture#get()} or with a listener e.g.
   * {@link CompletableFuture#thenAccept(java.util.function.Consumer)}
   * 
   * @return
   */
  public CompletableFuture<Double> waitForChange();

  /**
   * Return a future representing when this clock reaches or passes the trigger
   * time. No guarantee is made that this will be fired at the trigger time. It
   * ultimately depends upon the underlying implementation. Callers may use this
   * synchronously {@link CompletableFuture#get()} or with a listener e.g.
   * {@link CompletableFuture#thenAccept(java.util.function.Consumer)}
   * 
   * @param triggerTime
   * @return
   */
  public CompletableFuture<Double> waitForTime(double triggerTime);
}
