/*
 * Created on Jun 26, 2007 Copyright (C) 2001-2007, Anthony Harrison
 * anh23@pitt.edu (jactr.org) This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version. This library is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.sensors.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.net.message.IMessage;
import org.commonreality.net.message.command.object.IObjectCommand;
import org.commonreality.net.message.request.object.ObjectCommandRequest;
import org.commonreality.net.message.request.object.ObjectDataRequest;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.ISensoryObject;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.delta.FullObjectDelta;
import org.commonreality.object.delta.IObjectDelta;
import org.commonreality.object.identifier.ISensoryIdentifier;
import org.commonreality.sensors.AbstractSensor;
import org.commonreality.time.impl.RealtimeClock;

/**
 * skeletal sensor that utilizes a {@link PerceptManager} to create and process
 * percepts based on some other listening mechanism. It is up to the implementor
 * to configure the {@link PerceptManager}, by adding {@link IObjectCreator}s
 * and {@link IObjectProcessor}s. {@link BaseSensor#processPercepts()} calls
 * {@link PerceptManager#processDirtyObjects()}. <br/>
 * If you want to use a realtime clock, call
 * {@link #setRealtimeClockEnabled(boolean)} and the system will ensure that the
 * minimum cycle time is {@link #getTimeStep()} <br/>
 * If running using the standard clock cycle doesn't make sense for your system
 * (i.e. it is entirely event driven and fires infrequently), use immediate mode
 * ({@link #setImmediateModeEnabled(boolean)}) and call
 * {@link #processImmediately()} after your calls to
 * {@link PerceptManager#markAsDirty(Object)}, etc.
 * 
 * @author developer
 */
public abstract class BaseSensor extends AbstractSensor
{
  /**
   * logger definition
   */
  static private final Log                                LOGGER            = LogFactory
                                                                                .getLog(BaseSensor.class);

  private ExecutorService                                 _service;

  private Map<IIdentifier, Collection<ISimulationObject>> _toBeAdded;

  private Map<IIdentifier, Collection<IIdentifier>>       _toBeRemoved;

  private Map<IIdentifier, Collection<IObjectDelta>>      _toBeChanged;

  private Collection<IMessage>                            _delayedCommands;

  private Committer                                       _committer;

  private double                                          _timeStep         = 0.05;

  private PerceptManager                                  _perceptManager;

  /**
   * only used is isRealtimeClockEnabled()
   */
  private RealtimeClock                                   _realtimeClock;

  private boolean                                         _useRealtimeClock = false;

  private boolean                                         _useImmediateMode = false;

  public BaseSensor()
  {
    super();
    _toBeAdded = Collections
        .synchronizedMap(new HashMap<IIdentifier, Collection<ISimulationObject>>());
    _toBeRemoved = Collections
        .synchronizedMap(new HashMap<IIdentifier, Collection<IIdentifier>>());
    _toBeChanged = Collections
        .synchronizedMap(new HashMap<IIdentifier, Collection<IObjectDelta>>());
    _delayedCommands = Collections.synchronizedList(new ArrayList<IMessage>());
    _committer = new Committer();
    _service = Executors.newSingleThreadExecutor();
    _perceptManager = createPerceptManager();
  }

  /**
   * if immediateMode is to be used, extenders are responsible for calling
   * 
   * @param immediateMode
   */
  protected void setImmediateModeEnabled(boolean immediateMode)
  {
    _useImmediateMode = immediateMode;
  }

  protected boolean isImmediateModeEnabled()
  {
    return _useImmediateMode;
  }

  protected void processImmediately()
  {
    if (!isImmediateModeEnabled())
      throw new IllegalStateException(
          "Cannot processImmediately if not using immediate mode");

    processSensorData();
    packageData();
    sendData();
  }

  /**
   * call if you want to override the perceptmanager
   * 
   * @return
   */
  protected PerceptManager createPerceptManager()
  {
    return new PerceptManager(this);
  }

  public PerceptManager getPerceptManager()
  {
    return _perceptManager;
  }

  protected void setRealtimeClockEnabled(boolean useRTClock)
  {
    _useRealtimeClock = useRTClock;
  }

  protected boolean isRealtimeClockEnabled()
  {
    return _useRealtimeClock && !_useImmediateMode;
  }

  /**
   * @see org.commonreality.participant.impl.AbstractParticipant#getName()
   */
  @Override
  public String getName()
  {
    return "ProgrammaticSensor";
  }

  /**
   * timestep used for a shared clock or the minimum cycle time is using
   * realtime
   * 
   * @param timeStep
   */
  public void setTimeStep(double timeStep)
  {
    _timeStep = timeStep;
  }

  public double getTimeStep()
  {
    return _timeStep;
  }

  @Override
  public void initialize() throws Exception
  {
    super.initialize();
    _service = Executors.newSingleThreadExecutor();
    _service.execute(new Runnable() {

      public void run()
      {
        Thread.currentThread().setName(getName());
      }

    });
  }

  @Override
  public void shutdown() throws Exception
  {
    if (LOGGER.isDebugEnabled()) LOGGER.debug("Shuttingdown");
    try
    {
      if (stateMatches(State.STARTED)) stop();
    }
    finally
    {
      _service.shutdownNow();
      _service = null;
      super.shutdown();
    }

  }

  @Override
  public void start() throws Exception
  {
    try
    {
      _realtimeClock = new RealtimeClock(Executors.newScheduledThreadPool(1));
      super.start();
      if (LOGGER.isDebugEnabled()) LOGGER.debug("Executing committer");
      execute(_committer);
    }
    catch (Exception e)
    {
      LOGGER.error("Failed to start properly", e);
      throw e;
    }
  }

  @Override
  protected void agentAdded(IAgentObject agent)
  {
    super.agentAdded(agent);

    IIdentifier agentId = agent.getIdentifier();
    if (!_toBeAdded.containsKey(agentId))
    {
      _toBeAdded.put(agentId, new ArrayList<ISimulationObject>());
      _toBeChanged.put(agentId, new ArrayList<IObjectDelta>());
      _toBeRemoved.put(agentId, new ArrayList<IIdentifier>());
    }
  }

  @Override
  protected void agentRemoved(IAgentObject agent)
  {
    super.agentRemoved(agent);
    IIdentifier agentId = agent.getIdentifier();
    /*
     * if agents come in/out frequently, we may want to recycle the fast lists
     */

    _toBeAdded.remove(agentId);
    _toBeChanged.remove(agentId);
    _toBeRemoved.remove(agentId);

  }

  public void add(IMutableObject object)
  {
    synchronized (_toBeAdded)
    {
      _toBeAdded.get(((ISensoryIdentifier) object.getIdentifier()).getAgent())
          .add(object);
    }
  }

  /**
   * all percepts must be for the same agent
   * 
   * @param objects
   */
  public void add(Collection<IMutableObject> objects)
  {
    if (objects.size() == 0) return;

    IIdentifier agent = ((ISensoryIdentifier) objects.iterator().next()
        .getIdentifier()).getAgent();

    synchronized (_toBeAdded)
    {
      Collection<ISimulationObject> container = _toBeAdded.get(agent);

      container.addAll(objects);
    }
  }

  public void remove(ISensoryObject object)
  {
    synchronized (_toBeRemoved)
    {
      _toBeRemoved.get(object.getIdentifier().getAgent()).add(
          object.getIdentifier());
    }
  }

  public void remove(Collection<ISensoryObject> objects)
  {
    if (objects.size() == 0) return;

    IIdentifier agent = objects.iterator().next().getIdentifier().getAgent();
    synchronized (_toBeRemoved)
    {
      Collection<IIdentifier> container = _toBeRemoved.get(agent);

      for (ISensoryObject obj : objects)
        container.add(obj.getIdentifier());
    }
  }

  public void removeIdentifiers(Collection<ISensoryIdentifier> objectIdentifiers)
  {
    synchronized (_toBeRemoved)
    {
      for (ISensoryIdentifier sId : objectIdentifiers)
        _toBeRemoved.get(sId.getAgent()).add(sId);
    }
  }

  public void update(IObjectDelta delta)
  {
    synchronized (_toBeChanged)
    {
      _toBeChanged.get(((ISensoryIdentifier) delta.getIdentifier()).getAgent())
          .add(delta);
    }
  }

  public void update(Collection<IObjectDelta> deltas)
  {
    if (deltas.size() == 0) return;

    IIdentifier agent = ((ISensoryIdentifier) deltas.iterator().next()
        .getIdentifier()).getAgent();
    synchronized (_toBeChanged)
    {
      Collection<IObjectDelta> container = _toBeChanged.get(agent);

      container.addAll(deltas);
    }
  }

  /**
   * commit all the changes..
   */
  private void process(IIdentifier agentId)
  {
    IIdentifier sId = getIdentifier();

    Collection<IObjectDelta> deltas = new ArrayList<IObjectDelta>();
    Collection<IIdentifier> identifiers = new ArrayList<IIdentifier>();

    synchronized (_toBeAdded)
    {
      Collection<ISimulationObject> add = _toBeAdded.get(agentId);

      if (add.size() > 0)
      {
        for (ISimulationObject object : add)
        {
          identifiers.add(object.getIdentifier());
          deltas.add(new FullObjectDelta(object));
        }

        /*
         * we send the data immediately.. but queue up the notification
         */
        _delayedCommands.add(new ObjectDataRequest(sId, agentId, deltas));
        _delayedCommands.add(new ObjectCommandRequest(sId, agentId,
            IObjectCommand.Type.ADDED, identifiers));
      }
      add.clear();
    }

    identifiers.clear();
    deltas.clear();

    synchronized (_toBeChanged)
    {
      Collection<IObjectDelta> change = _toBeChanged.get(agentId);
      if (change.size() != 0)
      {
        /*
         * and the modify
         */
        for (IObjectDelta delta : change)
        {
          deltas.add(delta);
          identifiers.add(delta.getIdentifier());
        }

        _delayedCommands.add(new ObjectDataRequest(sId, agentId, deltas));
        _delayedCommands.add(new ObjectCommandRequest(sId, agentId,
            IObjectCommand.Type.UPDATED, identifiers));
      }
      change.clear();
    }

    deltas.clear();
    identifiers.clear();

    synchronized (_toBeRemoved)
    {
      Collection<IIdentifier> remove = _toBeRemoved.get(agentId);
      if (remove.size() != 0)
        _delayedCommands.add(new ObjectCommandRequest(sId, agentId,
            IObjectCommand.Type.REMOVED, remove));

      remove.clear();
    }

  }

  /**
   * execute some code on the main thread of the sensor
   * 
   * @param runner
   */
  public void execute(Runnable runner)
  {
    try
    {
      _service.execute(runner);
    }
    catch (RejectedExecutionException rjee)
    {
      /*
       * perfectly natural exception in the case where the xml sensor is
       * shutdown sometime between the entrance to this method and the execution
       * request.
       */
      if (LOGGER.isInfoEnabled())
        LOGGER
            .info("Execution rejected, assuming that sensor is shutting down");
    }
  }

  private class Committer implements Runnable
  {

    double _nextTime = 0;

    public void run()
    {
      startOfCycle();

      if (isImmediateModeEnabled())
        runImmediateMode();
      else
        runTimedMode();

      endOfCycle();

    }

    private void runImmediateMode()
    {
      preClockWait(Double.NaN);

      try
      {
        postClockWait(getClock().waitForChange().get());
      }
      catch (InterruptedException e)
      {
        LOGGER.error(
            "Committer.runImmediateMode threw InterruptedException : ", e);
      }
      catch (ExecutionException ee)
      {
        LOGGER.error(ee);
      }
    }

    private void runTimedMode()
    {
      double rtStartTime = _realtimeClock.getTime();
      double waitUntil = _nextTime;

      try
      {
        // snag this time stamp. we've sent the data
        // but we'll hold on the synchronization until
        // after we start the sensor processing

        /*
         * we start the processing and use the time info
         */
        if (!isRealtimeClockEnabled()) waitUntil += getTimeStep();

        double estimatedTime = processSensorData();
        if (!Double.isNaN(estimatedTime)) waitUntil = estimatedTime;

        packageData();

        sendData();

        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Waiting until %.2f", waitUntil));

        preClockWait(waitUntil);

        postClockWait(getClock().waitForTime(waitUntil).get());

        /*
         * and repeat
         */
        if (shouldContinue()) execute(this);

        double rtEndTime = _realtimeClock.getTime();
        double rtProcessingTime = rtEndTime - rtStartTime;

        if (isRealtimeClockEnabled())
        {
          waitUntil = _realtimeClock.waitForTime(rtEndTime + getTimeStep())
              .get();
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format(
                "Processing ended @ %.2f, took %.2f, waited until %.2f",
                rtEndTime, rtProcessingTime, waitUntil));
        }

        _nextTime = waitUntil;
      }
      catch (InterruptedException e)
      {
        /*
         * perfectly normal if the sensor is interrupted
         */
        LOGGER.warn("Interrupted, expecting termination ", e);
      }
      catch (Exception e)
      {
        uncaughtException(e);
      }
    }
  }

  /**
   * if the main processing should continue, return true
   * 
   * @return
   */
  protected boolean shouldContinue()
  {
    return true;
  }

  protected void startOfCycle()
  {

  }

  protected void endOfCycle()
  {

  }

  /**
   * called before the time synch wait
   * 
   * @param waitUntilTime
   */
  protected void preClockWait(double waitUntilTime)
  {

  }

  /**
   * called after the time synch block
   * 
   * @param currentTime
   */
  protected void postClockWait(double currentTime)
  {

  }

  /**
   * called after all the pending messages have been sent out to CR
   */
  protected void messagesSent()
  {

  }

  /**
   * called after all the agent processing has been queued (but not necessarily
   * started)
   */
  protected void agentDataProcessed()
  {

  }

  /**
   * called if something isn't caught in the main thread
   * 
   * @param e
   */
  protected void uncaughtException(Exception e)
  {
    LOGGER.error("Uncaught exception ", e);
  }

  /**
   * called when the subclasses' percept managers need to be called. If during
   * the course of the perceptual processing, an estimate can be made as to when
   * new data may be available, it should be returned. this is used in event
   * based models so that time can be controlled more effectively.<br/>
   * If {@link #isRealtimeClockEnabled()} returns true, the return value is
   * ignored
   * 
   * @return time when new data might be available, or {@link Double#isNaN()} if
   *         there is no estimate.
   */
  protected double processPercepts()
  {
    _perceptManager.processDirtyObjects();

    return Double.NaN;
  }

  /**
   * process the available motor information. If during the course of the
   * perceptual processing, an estimate can be made as to when new data may be
   * available, it should be returned. this is used in event based models so
   * that time can be controlled more effectively.<br/>
   * If {@link #isRealtimeClockEnabled()} returns true, the return value is
   * ignored
   * 
   * @return time when new data might be available, or {@link Double#isNaN()} if
   *         there is no estimate.
   */
  abstract protected double processMotor();

  /**
   * @return
   */
  protected double processSensorData()
  {
    double perceptNextTime = processPercepts();
    double motorNextTime = processMotor();

    if (!Double.isNaN(perceptNextTime) && !Double.isNaN(motorNextTime))
      return Math.max(perceptNextTime, motorNextTime);

    if (!Double.isNaN(perceptNextTime)) return perceptNextTime;

    return motorNextTime;
  }

  protected void packageData()
  {
    /*
     * now queue the processor for each agent
     */
    for (IIdentifier agent : getInterfacedAgents())
      process(agent);

    agentDataProcessed();
  }

  protected void sendData()
  {
    synchronized (_delayedCommands)
    {
      if (_delayedCommands.size() > 0)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("Sending %d delayed commands",
              _delayedCommands.size()));

        for (IMessage command : _delayedCommands)
        {
          send(command);
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("Sent %s", command));

        }

        _delayedCommands.clear();
      }
    }

    messagesSent();
  }
}
