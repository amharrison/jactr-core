package org.commonreality.sensors.motor.interpolator;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.motor.MotorConstants;
import org.commonreality.modalities.motor.MotorUtilities;
import org.commonreality.modalities.motor.MovementCommand;
import org.commonreality.net.message.command.object.IObjectCommand;
import org.commonreality.net.message.request.object.ObjectCommandRequest;
import org.commonreality.net.message.request.object.ObjectDataRequest;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.delta.DeltaTracker;
import org.commonreality.object.delta.IObjectDelta;
import org.commonreality.sensors.ISensor;
import org.commonreality.sensors.handlers.EfferentCommandHandler;
import org.commonreality.sensors.motor.IActuator;

/**
 * @author harrison
 */
public class BasicInterpolator implements IInterpolator
{
  /**
   * Logger definition
   */
  static private final transient Log      LOGGER = LogFactory
                                                     .getLog(BasicInterpolator.class);

  private EfferentCommandHandler          _handler;

  private Collection<InterpolatorEvent>   _recycledEventCollection;

  private Map<IIdentifier, IObjectDelta>  _recycledDeltaMap;

  private IActuatorCompletion             _completion;

  private IActuator                       _actualActuator;

  private Map<IIdentifier, AgentCommands> _agentCommands;

  public BasicInterpolator(EfferentCommandHandler handler,
      IActuator actualActuator, IActuatorCompletion completion)
  {
    _handler = handler;
    _recycledEventCollection = new ArrayList<InterpolatorEvent>();
    _recycledDeltaMap = new HashMap<IIdentifier, IObjectDelta>();
    _completion = completion;
    _actualActuator = actualActuator;
    _agentCommands = new HashMap<IIdentifier, AgentCommands>();
  }

  public void abort(IAgentObject agent, MovementCommand command)
  {
    AgentCommands agentCommands = getAgentCommands(agent.getIdentifier(), false);
    if (agentCommands == null) return;

    /*
     * we just flag this event as being aborted. it wont actually be aborted
     * until the update call is made
     */
    InterpolatorEvent event = agentCommands.getEvent(command.getIdentifier());
    if (event != null)
    {
      event.abort();
      /*
       * is this composite?
       */
      if (command.isCompound())
        for (IEfferentCommand component : command.getComponents())
          abort(agent, (MovementCommand) component);
    }
    else if (LOGGER.isWarnEnabled())
      LOGGER.warn("No pending command found " + command.getIdentifier());
  }

  protected AgentCommands getAgentCommands(IIdentifier agentId, boolean create)
  {
    synchronized (_agentCommands)
    {
      AgentCommands commands = _agentCommands.get(agentId);
      if (commands == null)
      {
        commands = new AgentCommands(agentId);
        _agentCommands.put(agentId, commands);
      }
      return commands;
    }
  }

  @SuppressWarnings("unchecked")
  protected Collection<IIdentifier> getRelevantAgentIdentifiers()
  {
    synchronized (_agentCommands)
    {
      if (_agentCommands.size() == 0) return Collections.EMPTY_LIST;
      return new ArrayList<IIdentifier>(_agentCommands.keySet());
    }
  }

  public void start(IAgentObject agent, MovementCommand command)
  {
    startInternal(agent, command, 0);
  }

  /**
   * @param agent
   * @param command
   * @param startTimeShift
   *          if this is for a component of a composite command, the
   *          startTimeShift will be the start time of the parent.
   */
  protected void startInternal(final IAgentObject agent,
      final MovementCommand command, double startTimeShift)
  {
    /*
     * queue it up..
     */
    final AgentCommands agentCommands = getAgentCommands(agent.getIdentifier(),
        true);

    double startTime = command.getRequestedStartTime() + startTimeShift;
    double endTime = startTime + command.getEstimatedDuration();
    InterpolatorEvent event = new InterpolatorEvent(command, startTime, endTime) {

      @Override
      protected void startInternal(double currentTime)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("starting " + command.getIdentifier() + " @ "
              + currentTime);
        _actualActuator.start(agent, command, _handler);
      }

      @Override
      protected void abortInternal(double currentTime)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("aborting " + command.getIdentifier() + " @ "
              + currentTime);
        _actualActuator.abort(agent, command, _handler);
      }

      @Override
      protected void updateInternal(double currentTime)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("updating " + command.getIdentifier() + " @ "
              + currentTime);
        DeltaTracker<IMutableObject> tracker = updateEvent(this, agentCommands,
            currentTime);
        if (tracker != null) _completion.updated(agent, command, tracker);
      }
    };

    agentCommands.add(event);

    /*
     * is this composite?
     */
    if (command.isCompound())
      for (IEfferentCommand component : command.getComponents())
        startInternal(agent, (MovementCommand) component, startTime);
  }

  public double update(double currentTime)
  {
    ISensor sensor = _handler.getSensor();
    double minimumTime = Double.MAX_VALUE;

    for (IIdentifier agentId : getRelevantAgentIdentifiers())
    {
      _recycledEventCollection.clear();
      _recycledDeltaMap.clear();

      AgentCommands agentCommands = getAgentCommands(agentId, false);
      agentCommands.getEvents(_recycledEventCollection);

      for (InterpolatorEvent event : _recycledEventCollection)
      {
        event.update(currentTime);

        /*
         * completed?
         */
        if (event.hasAborted() || event.hasCompleted())
        {
          agentCommands.remove(event);

          if (event.hasAborted())
            _completion.aborted(sensor.getAgentObjectManager().get(agentId),
                event.getCommand(), null);
          else
            _completion.completed(sensor.getAgentObjectManager().get(agentId),
                event.getCommand(), null);
        }
        else if (event.hasStarted())
          minimumTime = Math.min(minimumTime, event.getEndTime());
        else
          minimumTime = Math.min(minimumTime, event.getStartTime());
      }

      agentCommands.getDeltas(_recycledDeltaMap);
      if (_recycledDeltaMap.size() != 0)
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("Sending updates for " + _recycledDeltaMap.size()
              + " efferent objects");

        sensor.send(new ObjectDataRequest(sensor.getIdentifier(), agentId,
            _recycledDeltaMap.values()));
        sensor.send(new ObjectCommandRequest(sensor.getIdentifier(), agentId,
            IObjectCommand.Type.UPDATED, _recycledDeltaMap.keySet()));
      }
    }

    if (minimumTime == Double.MAX_VALUE) minimumTime = Double.NaN;

    return minimumTime;
  }

  /**
   * this is the code that actually updates the effernt objects
   * 
   * @param event
   * @param agentCommands
   * @param currentTime
   */
  protected DeltaTracker<IMutableObject> updateEvent(InterpolatorEvent event,
      AgentCommands agentCommands, double currentTime)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Updating " + event.getCommand().getIdentifier() + " @ "
          + currentTime);

    if (!event.hasStarted()) return null;

    if (event.hasCompleted()) currentTime = event.getEndTime();

    double deltaTime = Math.max(0, currentTime
        - Math.min(event.getLastUpdateTime(), event.getEndTime()));

    double[] rate = event.getCommand().getRate();

    IIdentifier efferent = event.getCommand().getEfferentIdentifier();
    DeltaTracker<IMutableObject> tracker = agentCommands.getTracker(efferent);

    double[] position = MotorUtilities.getDoubles(MotorConstants.POSITION,
        tracker);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Before update " + position(efferent, position) + " delta "
          + deltaTime);

    for (int i = 0; i < rate.length; i++)
      position[i] += rate[i] * deltaTime;

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("After update " + position(efferent, position));

    tracker.setProperty(MotorConstants.POSITION, position);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("tracker for " + tracker.getIdentifier() + " has changed "
          + tracker.hasChanged());

    return tracker;
  }

  private String position(IIdentifier identifier, double[] position)
  {
    StringBuilder sb = new StringBuilder(identifier.toString());
    sb.append(" is at ");
    for (double coord : position)
      sb.append(coord).append(" ");
    return sb.toString();
  }

  private class AgentCommands
  {
    private IIdentifier                                    _agent;

    private Collection<InterpolatorEvent>                  _events;

    private Map<IIdentifier, DeltaTracker<IMutableObject>> _deltaTrackers;

    public AgentCommands(IIdentifier agent)
    {
      _agent = agent;
      _events = new ArrayList<InterpolatorEvent>();
      _deltaTrackers = new HashMap<IIdentifier, DeltaTracker<IMutableObject>>();
    }

    synchronized public InterpolatorEvent getEvent(IIdentifier commandId)
    {
      for (InterpolatorEvent event : _events)
        if (event.getCommand().getIdentifier().equals(commandId)) return event;
      return null;
    }

    synchronized public DeltaTracker<IMutableObject> getTracker(
        IIdentifier efferentId)
    {
      DeltaTracker<IMutableObject> rtn = _deltaTrackers.get(efferentId);
      if (rtn == null)
      {
        rtn = new DeltaTracker<IMutableObject>(_handler.getSensor()
            .getEfferentObjectManager().get(efferentId));
        _deltaTrackers.put(efferentId, rtn);
      }
      return rtn;
    }

    synchronized public void add(InterpolatorEvent event)
    {
      _events.add(event);
    }

    synchronized public void remove(InterpolatorEvent event)
    {
      _events.remove(event);
    }

    synchronized public void getEvents(Collection<InterpolatorEvent> events)
    {
      events.addAll(_events);
    }

    synchronized public void getDeltas(Map<IIdentifier, IObjectDelta> deltas)
    {
      for (DeltaTracker<IMutableObject> tracker : _deltaTrackers.values())
        if (tracker.hasChanged())
        {
          IObjectDelta delta = tracker.getDelta();
          deltas.put(delta.getIdentifier(), delta);
        }
    }
  }
}
