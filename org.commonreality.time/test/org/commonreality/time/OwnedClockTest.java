package org.commonreality.time;

/*
 * default logging
 */
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.time.impl.OwnedClock;
import org.commonreality.time.impl.OwnedClock.OwnedAuthoritativeClock;
import org.junit.Assert;
import org.junit.Test;

public class OwnedClockTest
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(OwnedClockTest.class);


  protected IClock createNewClock()
  {
    return new OwnedClock(0.05);
  }


  @Test
  public void testGetTime()
  {
    IClock clock = createNewClock();
    IAuthoritativeClock ca = clock.getAuthority().get();
    ((OwnedAuthoritativeClock) ca).addOwner("a"); // single owner

    Assert.assertEquals(0, clock.getTime(), 0.001);

    ca.requestAndWaitForTime(1, "a");

    Assert.assertEquals(1, clock.getTime(), 0.001);

    CompletableFuture<Double> reached = clock.waitForTime(0.5);

    Assert.assertTrue(reached.isDone());
  }

  @Test
  public void testGetAuthority()
  {
    IClock clock = createNewClock();
    Optional<IAuthoritativeClock> auth = clock.getAuthority();

    Assert.assertNotNull(auth);
    Assert.assertTrue(auth.isPresent());
  }

  @Test
  public void testWaitForChange()
  {
    IClock clock = createNewClock();
    IAuthoritativeClock ca = clock.getAuthority().get();
    ((OwnedAuthoritativeClock) ca).addOwner("a"); // two owners
    ((OwnedAuthoritativeClock) ca).addOwner("b"); // single owner

    Assert.assertEquals(0, clock.getTime(), 0.001);

    CompletableFuture<Double> reached = clock.waitForChange(); // change
                                                               // relative to
                                                               // current
    // clock hasn't changed yet
    Assert.assertTrue(!reached.isDone());

    // update current
    CompletableFuture<Double> a = ca.requestAndWaitForTime(1, "a");
    CompletableFuture<Double> b = ca.requestAndWaitForTime(0.5, "b");

    Assert.assertTrue(b.isDone());
    Assert.assertTrue(!a.isDone());
    Assert.assertEquals(0.5, clock.getTime(), 0.001);
    Assert.assertTrue(reached.isDone());
  }

  @Test
  public void testWaitForTime()
  {
    IClock clock = createNewClock();
    IAuthoritativeClock ca = clock.getAuthority().get();
    ((OwnedAuthoritativeClock) ca).addOwner("a"); // two owners
    ((OwnedAuthoritativeClock) ca).addOwner("b"); // single owner

    Assert.assertEquals(0, clock.getTime(), 0.001);

    CompletableFuture<Double> reached = clock.waitForTime(0.75); // change
    // relative to
    // current
    // clock hasn't changed yet
    Assert.assertTrue(!reached.isDone());

    // update current
    CompletableFuture<Double> a = ca.requestAndWaitForTime(1, "a");
    CompletableFuture<Double> b = ca.requestAndWaitForTime(0.5, "b");

    Assert.assertTrue(b.isDone());
    Assert.assertTrue(!a.isDone());

    Assert.assertEquals(0.5, clock.getTime(), 0.001);
    Assert.assertTrue(!reached.isDone());

    ca.requestAndWaitForTime(1, "b");
    Assert.assertEquals(1, clock.getTime(), 0.001);

    Assert.assertTrue(reached.isDone());
  }
}
