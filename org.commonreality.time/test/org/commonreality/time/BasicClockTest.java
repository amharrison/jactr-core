package org.commonreality.time;

/*
 * default logging
 */
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.time.impl.BasicClock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BasicClockTest
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(BasicClockTest.class);

  
  
  protected IClock createNewClock(boolean withAuth)
  {
    return new BasicClock(withAuth, 0.05);
  }
  
  @Before
  public void setUp() throws Exception
  {
   
  }

  @After
  public void tearDown() throws Exception
  {
  }

  @Test
  public void testGetTime()
  {
    IClock clock = createNewClock(true);
    IAuthoritativeClock ca = clock.getAuthority().get();
    
    Assert.assertEquals(0, clock.getTime(), 0.001);

    ca.requestAndWaitForTime(1, null);// basic clock doesnt need key

    Assert.assertEquals(1, clock.getTime(), 0.001);
    
    CompletableFuture<Double> reached = clock.waitForTime(0.5);
    
    Assert.assertTrue(reached.isDone());
  }

  @Test
  public void testGetAuthority()
  {
    IClock clock = createNewClock(false);
    Optional<IAuthoritativeClock> auth = clock.getAuthority();
    
    Assert.assertNotNull(auth);
    Assert.assertTrue(!auth.isPresent());
    
    clock = createNewClock(true);
    auth = clock.getAuthority();
    
    Assert.assertNotNull(auth);
    Assert.assertTrue(auth.isPresent());
  }

  @Test
  public void testWaitForChange()
  {
    IClock clock = createNewClock(true);
    IAuthoritativeClock ca = clock.getAuthority().get();

    Assert.assertEquals(0, clock.getTime(), 0.001);

    CompletableFuture<Double> reached = clock.waitForChange(); // change
                                                               // relative to
                                                               // current
    // clock hasn't changed yet
    Assert.assertTrue(!reached.isDone());

    ca.requestAndWaitForTime(0, null); // not actually changing.
    Assert.assertTrue(!reached.isDone()); // early imply would fail this

    // update current
    ca.requestAndWaitForTime(1, null); // basic clock doesnt need key

    Assert.assertEquals(1, clock.getTime(), 0.001);
    Assert.assertTrue(reached.isDone());
  }

  @Test
  public void testWaitForTime()
  {
    IClock clock = createNewClock(true);
    IAuthoritativeClock ca = clock.getAuthority().get();

    Assert.assertEquals(0, clock.getTime(), 0.001);

    CompletableFuture<Double> reached = clock.waitForTime(1); // change
                                                              // relative to
                                                              // current
    // clock hasn't changed yet
    Assert.assertTrue(!reached.isDone());

    // update current
    CompletableFuture<Double> assigned = ca.requestAndWaitForTime(1, null); // basic
                                                                            // clock
                                                                            // doesnt
                                                                            // need
                                                                            // key

    Assert.assertTrue(assigned.isDone());
    Assert.assertEquals(1, clock.getTime(), 0.001);
    Assert.assertTrue(reached.isDone());
  }

  // @Test
  // public void testTimeShift()
  // {
  // IClock[] clocks = createNewClocks();
  //
  // Assert.assertEquals(0, clocks[0].getTime(), 0.001);
  //
  // clocks[1].getAuthority().get().setLocalTimeShift(1); // "a" is 1 second old
  // // at the start of sim
  // clocks[2].getAuthority().get().setLocalTimeShift(-1); // "b" is -1 second
  // // old at the start of
  // // sim
  //
  // // clock 1 is already at 1, this should be immediate
  // CompletableFuture<Double> a = clocks[1].getAuthority().get()
  // .requestAndWaitForTime(1, "a");
  //
  // // clock 2 is at -1, so this will not have elapsed
  // CompletableFuture<Double> b = clocks[2].getAuthority().get()
  // .requestAndWaitForTime(0.5, "b");
  //
  // Assert.assertTrue(a.isDone());
  // Assert.assertTrue(!b.isDone());
  //
  // Assert.assertEquals(0.5, clocks[2].getTime(), 0.001);
  // Assert.assertTrue(!reached.isDone());
  //
  // clocks[1].getAuthority().get().requestAndWaitForChange("a");
  // clocks[2].getAuthority().get().requestAndWaitForTime(1, "b");
  // Assert.assertEquals(1, clocks[1].getTime(), 0.001);
  //
  // Assert.assertTrue(reached.isDone());
  // }

}
