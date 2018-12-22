package org.commonreality.sensors.speech;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.efferent.IEfferentCommandTemplate;
import org.commonreality.executor.InlineExecutor;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.vocal.VocalConstants;
import org.commonreality.modalities.vocal.VocalizationCommand;
import org.commonreality.modalities.vocal.VocalizationCommandTemplate;
import org.commonreality.net.message.command.object.IObjectCommand;
import org.commonreality.net.message.request.object.ObjectCommandRequest;
import org.commonreality.net.message.request.object.ObjectDataRequest;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.IRealObject;
import org.commonreality.object.delta.FullObjectDelta;
import org.commonreality.object.manager.IRequestableEfferentObjectManager;
import org.commonreality.object.manager.impl.EfferentObject;
import org.commonreality.sensors.AbstractSensor;
import org.commonreality.sensors.aural.AuralUtilities;
import org.commonreality.sensors.handlers.EfferentCommandHandler;
import org.commonreality.sensors.handlers.ICommandHandlerDelegate;
import org.commonreality.sensors.handlers.ICommandTimingEquation;
import org.commonreality.time.impl.BasicClock;

/**
 * Default speech generation sensor. For all connected agents, it creates an
 * {@link IEfferentObject} that corresponds to the agent's mouth. That
 * {@link IEfferentObject} contains at least one
 * {@link IEfferentCommandTemplate} that is a
 * {@link VocalizationCommandTemplate} which can be used to instantiate a
 * specific {@link IEfferentCommand}, that is a {@link VocalizationCommand}.
 * This sensor will process all such commands, execute them, and place an
 * {@link IRealObject} into the simulation which will likely be picked up by an
 * aural sensor to make the percept available.
 * 
 * @author harrison
 */
public class DefaultSpeechSensor extends AbstractSensor implements ISpeaker
{
  /**
   * 
   */
  private static final long                    serialVersionUID               = -4235554591675582713L;

  /**
   * Logger definition
   */
  static private final transient Log           LOGGER                         = LogFactory
                                                                                  .getLog(DefaultSpeechSensor.class);

  static public final String                   VOCALIZATION_DURATION_EQUATION = "DurationEquation";

  static public final String                   SPEAKER                        = "Speaker";

  private Map<String, String>                  _options;

  private ICommandTimingEquation               _durationEquation;

  @SuppressWarnings("unchecked")
  private Collection<IEfferentCommandTemplate> _vocalizationTemplate;

  private Runnable                             _cycle;

  private ExecutorService                      _executor;

  private EfferentCommandHandler               _handler;

  private AuralUtilities                       _auralUtilities;

  private TreeMap<Double, IEfferentCommand>    _completeCommandsAt;

  private ISpeaker                             _actualSpeaker;

  @SuppressWarnings("unchecked")
  public DefaultSpeechSensor()
  {
    IEfferentCommandTemplate template = new VocalizationCommandTemplate(
        "Vocalize", "Creates a vocalization command");
    _vocalizationTemplate = Collections.unmodifiableCollection(Collections
        .singleton(template));

    _completeCommandsAt = new TreeMap<Double, IEfferentCommand>();

    _options = new TreeMap<String, String>();

    _auralUtilities = new AuralUtilities(this);

    /**
     * this actually control the flow of the sensor
     */
    _cycle = new Runnable() {
      public void run()
      {
        /*
         * at the top of each clock cycle we check for speech events that have
         * yet to be started or have expired
         */
        double now = getClock().getTime();
        _auralUtilities.update(now);

        /*
         * you may be wondering where we handle new command requests, that is
         * done by the EfferentCommandHandler
         */
        Iterator<IEfferentCommand> commands = _completeCommandsAt.headMap(now)
            .values().iterator();
        if (commands.hasNext())
        {
          _handler.completed(commands.next(), null);
          commands.remove();
        }

        IEfferentCommand command = _completeCommandsAt.remove(now);
        if (command != null) _handler.completed(command, null);

        try
        {
          /*
           * let's wait for the clock to update
           */
          getClock().getAuthority().get().requestAndWaitForChange(null).get();
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

  /**
   * default name for the speech sensor
   */
  @Override
  public String getName()
  {
    return "speech";
  }

  /**
   * we can accept only a very basic configuration option. The
   * {@value #VOCALIZATION_DURATION_EQUATION} computes how long it will take to
   * speak a string. If none is provided, a default instance is created that
   * looks for a parameter CharactersPerSecond which will be used to compue
   * duration times
   */
  @Override
  public void configure(Map<String, String> options) throws Exception
  {
    String durationClass = options.get(VOCALIZATION_DURATION_EQUATION);
    try
    {
      _durationEquation = (ICommandTimingEquation) getClass().getClassLoader()
          .loadClass(durationClass).newInstance();
    }
    catch (Exception e)
    {
      /**
       * Error : couldn't load the specified duration equation
       */
      if (durationClass != null && durationClass.trim().length() != 0)
        LOGGER.error("Could not load " + durationClass + " using default. ", e);

      Map<String, String> equationOptions = new TreeMap<String, String>(options);
      _durationEquation = new DefaultVocalizationTimingEquation(equationOptions);
    }

    if (options.containsKey(SPEAKER))
      try
      {
        ISpeaker speaker = (ISpeaker) getClass().getClassLoader()
            .loadClass(options.get(SPEAKER)).newInstance();
        speaker.configure(this, new TreeMap<String, String>(options));

        _actualSpeaker = speaker;
      }
      catch (Exception e)
      {
        LOGGER.warn(
            "Could not create actual speaker from " + options.get(SPEAKER), e);
      }

    _options.putAll(options);
  }

  /**
   * let's initialize. this is called before start but after configure. here we
   * create the executor that handles the asynchronous aspects (runs the
   * {@link Runnable} _cycle). We also create the default
   * {@link EfferentCommandHandler} here.
   */
  @Override
  public void initialize() throws Exception
  {
    /*
     * create and name the executor
     */
    _executor = Executors.newSingleThreadExecutor(getCentralThreadFactory());

    /*
     * this handler will be run in the same executor as above. this means that
     * when the system is running (post start()) the efferent command events
     * will be queued up with the cycle, in other words, the commands will only
     * be processed once per clock cycle.
     */

    ICommandHandlerDelegate delegate = new VocalizationCommandHandler(
        _durationEquation, this);

    _handler = new EfferentCommandHandler(this);
    _handler.add(delegate);

    getEfferentCommandManager().addListener(_handler, InlineExecutor.get());

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
    if (_executor != null) _executor.shutdownNow();
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

  /**
   * actually speak by creating a new aural event, and posting it. The aural
   * sensor (if installed) will catch the event and create the appropriate
   * {@link IAfferentObject} for the connecte agents
   * 
   * @param vocalCommand
   */
  public void speak(final IAgentObject speaker,
      final VocalizationCommand vocalCommand)
  {
    final String text = vocalCommand.getText();
    final double onset = vocalCommand.getRequestedStartTime();
    final double duration = vocalCommand.getEstimatedDuration();

    /**
     * we have to do this via a runner on the executor because this request
     * typically arrives on the io processor. since new vocalization will likely
     * block on the request of a new real object, we need to do this on another
     * thread
     */
    Runnable runner = new Runnable() {
      public void run()
      {
        IRealObject vocalization = _auralUtilities.newVocalization(text, onset,
            duration, speaker.getIdentifier());
        _auralUtilities.queueSound(vocalization);

        _auralUtilities.update(onset);
        if (_actualSpeaker != null)
          _actualSpeaker.speak(speaker, vocalCommand);
      }
    };

    double end = BasicClock.constrainPrecision(onset + duration);
    if (_completeCommandsAt.containsKey(end))
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn("Colliding end time @ " + end + " for "
            + vocalCommand.getIdentifier());
      _handler.completed(_completeCommandsAt.remove(end), null);
    }

    _completeCommandsAt.put(end, vocalCommand);

    _executor.execute(runner);
  }

  /**
   * when we get a new agent, immediately given them a mouth
   */
  @Override
  protected void agentAdded(final IAgentObject agent)
  {
    super.agentAdded(agent);

    /*
     * this is pushed to the executor because the create mouth method will block
     * if no efferent objects are available, and they wont be since agentAdded
     * performs the prefetch
     */
    Runnable runner = new Runnable() {
      public void run()
      {

        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Creating mouth for " + agent.getIdentifier());

        IRequestableEfferentObjectManager reom = getEfferentObjectManager();
        IIdentifier mouthId = reom.requestIdentifier(agent.getIdentifier());

        EfferentObject mouth = new EfferentObject(mouthId);
        mouth.setProperty(IEfferentObject.COMMAND_TEMPLATES,
            _vocalizationTemplate);
        mouth.setProperty(VocalConstants.CAN_VOCALIZE, Boolean.TRUE);

        IIdentifier src = getIdentifier();
        IIdentifier dest = agent.getIdentifier();

        send(new ObjectDataRequest(src, dest,
            Collections.singleton(new FullObjectDelta(mouth))));

        send(new ObjectCommandRequest(src, dest, IObjectCommand.Type.ADDED,
            Collections.singleton(mouthId)));
      }
    };

    _executor.execute(runner);
  }

  public void configure(DefaultSpeechSensor sensor, Map<String, String> options)
  {
    // noop
  }
}
