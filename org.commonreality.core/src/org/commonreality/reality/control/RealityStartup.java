package org.commonreality.reality.control;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.participant.IParticipant.State;
import org.commonreality.reality.IReality;

public class RealityStartup implements Runnable
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(RealityStartup.class);

  private final IReality             _reality;

  public RealityStartup(IReality reality)
  {
    _reality = reality;
  }

  public void run()
  {
    try
    {
      _reality.start();
      _reality.waitForState(State.STARTED);

      /*
       * assuming it all works, install a shutdown hook just in case something
       * goes wrong in the normal sequence
       */
      Runtime.getRuntime().addShutdownHook(
          new Thread(new RealityShutdown(_reality, false)));
    }
    catch (Exception e)
    {
      throw new RuntimeException("Could not start common reality ", e);
    }

  }

}
