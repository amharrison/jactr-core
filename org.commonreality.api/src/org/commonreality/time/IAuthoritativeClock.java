package org.commonreality.time;

import java.util.concurrent.CompletableFuture;

/*
 * default logging
 */

/**
 * A clock instance that permits the setting of the clock as well. "Setting" is
 * not the correct term, rather "requesting", as the behavior ultimately depends
 * upon the implementation and other interfacing agents. The idea is that a time
 * request is made to set the time to a value (or wait for a change) before the
 * wait futures are returned.<br/>
 * For instance, the shared clock instance (where multiple clocks move in
 * lock-step based on the smallest time increment requested) uses the requested
 * values to determine the smallest time step. <br/>
 * The authority clock also has support for local timeshifting, permitting
 * consistent time across different systems.
 * 
 * @author harrison
 */
public interface IAuthoritativeClock extends IClock
{

  /**
   * send request before attempting {@link #waitForChange()}
   * 
   * @param key
   *          is an authorization key, if it is required (i.e. to signify true
   *          ownership)
   * @return
   */
  public CompletableFuture<Double> requestAndWaitForChange(Object key);

  /**
   * send request before attemtping {@link #waitForTime(double)}
   * 
   * @param key
   *          is an authorization key, if it is required (i.e. to signify true
   *          ownership)
   * @param targetTime
   * @return
   */
  public CompletableFuture<Double> requestAndWaitForTime(double targetTime,
      Object key);

  public double getLocalTimeShift();

  public void setLocalTimeShift(double timeShift);
}
