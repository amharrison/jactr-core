package org.commonreality.sensors.aural;

/*
 * default logging
 */
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.agents.IAgent;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.IRealObject;
import org.commonreality.sensors.AbstractSensor;
import org.commonreality.sensors.aural.GeneralAuralProcessor.IAuralMutator;
import org.commonreality.time.IAuthoritativeClock;

/**
 * quick and easy way to get sounds out to the system is to extend this sensor
 * and use {@link #queueSound(IRealObject)} with the results from
 * {@link #newSound(String[], String, double, double)} or
 * {@link #newVocalization(String, double, double, IIdentifier)}. This will
 * create {@link IRealObject}s representing the sounds, which will be detected
 * by the {@link GeneralAuralProcessor} which transforms them into
 * {@link IAfferentObject}s for each of the registered {@link IAgent}s. Further
 * customization of the sounds can be done using
 * {@link GeneralAuralProcessor.IAuralMutator}, which can be added to the sensor
 * with a prefixed classname property "IAuralMutator."
 * 
 * @author harrison
 */
public class DefaultAuralSensor extends AbstractSensor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER         = LogFactory
                                                        .getLog(DefaultAuralSensor.class);

  static public final String         MUTATOR_PREFIX = "IAuralMutator.";

  /*
   * this class handles the creation and tracking of aural events
   */
  private AuralUtilities             _auralUtilities;

  /*
   * and this class handles the creation and tracking of the aural percepts for
   * each of the connected agents
   */
  private GeneralAuralProcessor      _auralProcessor;

  private ExecutorService            _executor;

  private Runnable                   _cycle;

  public DefaultAuralSensor()
  {
    _auralUtilities = new AuralUtilities(this);
    _auralProcessor = new GeneralAuralProcessor(this);

    _cycle = new Runnable() {
      public void run()
      {
        /*
         * at the top of each clock cycle we check for speech events that have
         * yet to be started or have expired
         */

        try
        {
          double currentTime = getClock().getTime();
          double nextTime = _auralUtilities.update(currentTime);
          _auralProcessor.update(currentTime);
          /*
           * let's wait for the clock to update
           */
          IAuthoritativeClock auth = getClock().getAuthority().get();

          if (LOGGER.isDebugEnabled()) LOGGER.debug("Waiting");

          if (Double.isNaN(nextTime) || nextTime <= currentTime)
            auth.requestAndWaitForChange(null).get();
          else
            auth.requestAndWaitForTime(nextTime, null).get();

          if (LOGGER.isDebugEnabled()) LOGGER.debug("Resuming");

        }
        catch (InterruptedException e)
        {
          LOGGER.warn("Interrupted, expecting termination ", e);
          /*
           * this isn't an error.. if we're interrupted we shouldn't continue
           * running
           */
          Thread.interrupted(); // reset
        }
        catch (ExecutionException ee)
        {
          LOGGER.error(ee);
        }

        /*
         * we're still running, requeue
         */
        if (stateMatches(State.STARTED)) _executor.execute(this);
      }
    };
  }

  @Override
  public void configure(Map<String, String> options) throws Exception
  {
    for (Map.Entry<String, String> entry : options.entrySet())
      if (entry.getKey().startsWith(MUTATOR_PREFIX))
        try
        {
          /*
           * is this a mutator?
           */
          String className = entry.getValue();
          Class c = getClass().getClassLoader().loadClass(className);
          IAuralMutator mutator = (IAuralMutator) c.newInstance();
          _auralProcessor.add(mutator);
        }
        catch (Exception e)
        {
          if (LOGGER.isWarnEnabled())
            LOGGER.warn("Could not load IAuralMutator " + entry.getValue()
                + " ", e);
        }
  }

  @Override
  public void initialize() throws Exception
  {
    /*
     * create and name the executor
     */
    _executor = Executors.newSingleThreadExecutor(getCentralThreadFactory());

    getRealObjectManager().addListener(_auralProcessor, _executor);

    super.initialize();
  }

  /**
   * Start it up by queueing the {@link Runnable} _cycle on the {@link Executor}
   * 
   * @see org.commonreality.participant.impl.AbstractParticipant#start()
   */
  @Override
  public void start() throws Exception
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Starting " + getName() + " sensor");

    _executor.execute(_cycle);

    super.start();
  }

  @Override
  public void shutdown() throws Exception
  {
    _executor.shutdownNow();
    _executor = null;
    super.shutdown();
  }

  @Override
  public void resume() throws Exception
  {
    checkState(State.SUSPENDED);
    _executor.execute(_cycle);
    super.resume();
  }

  @Override
  public String getName()
  {
    return "aural";
  }

  public IRealObject newSound(String[] types, String content, double onset,
      double duration)
  {
    return _auralUtilities.newSound(types, content, onset, duration);
  }

  public IRealObject newVocalization(String content, double onset,
      double duration, IIdentifier agent)
  {
    return _auralUtilities.newVocalization(content, onset, duration, agent);
  }

  public void queueSound(IRealObject aural)
  {
    _auralUtilities.queueSound(aural);

    // local sound source, we transform it immediately and locally
    _auralProcessor.addAural(aural);
  }

}
