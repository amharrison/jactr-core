package org.commonreality.efferent.impl;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.efferent.IEfferentCommandManager;
import org.commonreality.efferent.event.IEfferentCommandListener;
import org.commonreality.executor.InlineExecutor;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.delta.IObjectDelta;
import org.commonreality.object.manager.event.IObjectEvent;
import org.commonreality.object.manager.impl.GeneralObjectManager;

public class EfferentCommandManager extends
    GeneralObjectManager<IEfferentCommand, IEfferentCommandListener> implements
    IEfferentCommandManager
{
  /**
   * Logger definition
   */
  static private final transient Log                                               LOGGER = LogFactory
                                                                                              .getLog(EfferentCommandManager.class);

  private Map<IEfferentCommand.RequestedState, Map<IIdentifier, IEfferentCommand>> _requestedStates;

  private Map<IEfferentCommand.ActualState, Map<IIdentifier, IEfferentCommand>>    _actualStates;
  
  private Map<IIdentifier, Set<IIdentifier>> _byAgent;
  private Map<IIdentifier, Set<IIdentifier>> _bySensor;

  public EfferentCommandManager()
  {
    _requestedStates = new HashMap<IEfferentCommand.RequestedState, Map<IIdentifier, IEfferentCommand>>();
    _actualStates = new HashMap<IEfferentCommand.ActualState, Map<IIdentifier, IEfferentCommand>>();
    _byAgent = new HashMap<IIdentifier, Set<IIdentifier>>();
    _bySensor = new HashMap<IIdentifier, Set<IIdentifier>>();

    /*
     * we add a listener to ourselves..
     */
    IEfferentCommandListener listener = new IEfferentCommandListener() {

      synchronized public void objectsAdded(IObjectEvent<IEfferentCommand, ?> addEvent)
      {
        for (IEfferentCommand command : addEvent.getObjects())
        {
          removeState(command);
          addState(command);
        }
      }

      synchronized public void objectsRemoved(IObjectEvent<IEfferentCommand, ?> removeEvent)
      {
        for (IEfferentCommand command : removeEvent.getObjects())
          removeState(command);
      }

      synchronized public void objectsUpdated(IObjectEvent<IEfferentCommand, ?> updateEvent)
      {
        for (IObjectDelta delta : updateEvent.getDeltas())
        {
          Collection<String> properties = delta.getChangedProperties();
          if (properties.contains(IEfferentCommand.REQUESTED_STATE)
              || properties.contains(IEfferentCommand.ACTUAL_STATE))
          {
            IEfferentCommand command = get(delta.getIdentifier());
            removeState(command);
            addState(command);
          }
        }
      }
    };
    
    addListener(listener, InlineExecutor.get());
  }
  
  protected void index(IEfferentCommand command)
  {
    IIdentifier sensor = command.getIdentifier().getSensor();
    IIdentifier agent = command.getIdentifier().getAgent();
    
    Set<IIdentifier> commands = _bySensor.get(sensor);
    if(commands==null)
    {
      commands = new HashSet<IIdentifier>();
      _bySensor.put(sensor, commands);
    }
    commands.add(command.getIdentifier());
    
    commands = _byAgent.get(agent);
    if(commands==null)
    {
      commands = new HashSet<IIdentifier>();
      _byAgent.put(agent, commands);
    }
    commands.add(command.getIdentifier());
  }
  
  protected void unindex(IEfferentCommand command)
  {
    IIdentifier sensor = command.getIdentifier().getSensor();
    IIdentifier agent = command.getIdentifier().getAgent();
    
    Set<IIdentifier> commands = _bySensor.get(sensor);
    if(commands!=null) commands.remove(command.getIdentifier());
    
    commands = _byAgent.get(agent);
    if(commands!=null) commands.remove(command.getIdentifier());
  }

  protected void removeState(IEfferentCommand command)
  {
    IEfferentCommand.ActualState actual = command.getActualState();
    IEfferentCommand.RequestedState requested = command.getRequestedState();
    IIdentifier identifier = command.getIdentifier();

    if (actual == null || requested == null)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn("IEfferentCommand states for " + identifier
            + " are invalid, ignoring");
      return;
    }

    synchronized (_actualStates)
    {
      Map<IIdentifier, IEfferentCommand> state = _actualStates.get(actual);
      if (state != null) state.remove(identifier);
    }

    synchronized (_requestedStates)
    {
      Map<IIdentifier, IEfferentCommand> state = _requestedStates.get(actual);
      if (state != null) state.remove(identifier);
    }
  }

  protected void addState(IEfferentCommand command)
  {
    IEfferentCommand.ActualState actual = command.getActualState();
    IEfferentCommand.RequestedState requested = command.getRequestedState();
    IIdentifier identifier = command.getIdentifier();

    if (actual == null || requested == null)
    {
      if (LOGGER.isWarnEnabled())
        LOGGER.warn("IEfferentCommand states for " + identifier
            + " are invalid, ignoring");
      return;
    }

    synchronized (_actualStates)
    {
      Map<IIdentifier, IEfferentCommand> state = _actualStates.get(actual);
      if (state == null)
      {
        state = new HashMap<IIdentifier, IEfferentCommand>();
        _actualStates.put(actual, state);
      }

      state.put(identifier, command);
    }

    synchronized (_requestedStates)
    {
      Map<IIdentifier, IEfferentCommand> state = _requestedStates.get(actual);
      if (state == null)
      {
        state = new HashMap<IIdentifier, IEfferentCommand>();
        _actualStates.put(actual, state);
      }

      state.put(identifier, command);
    }
  }

  public Collection<IEfferentCommand> getEfferentCommands(
      IEfferentCommand.RequestedState requestedState)
  {
    Collection<IEfferentCommand> rtn = Collections.EMPTY_LIST;

    synchronized (_requestedStates)
    {
      Map<IIdentifier, IEfferentCommand> state = _requestedStates
          .get(requestedState);
      if (state != null) rtn = new ArrayList<IEfferentCommand>(state.values());
    }

    return rtn;
  }

  public Collection<IEfferentCommand> getEfferentCommands(
      IEfferentCommand.ActualState actualState)
  {
    Collection<IEfferentCommand> rtn = Collections.EMPTY_LIST;

    synchronized (_actualStates)
    {
      Map<IIdentifier, IEfferentCommand> state = _actualStates.get(actualState);
      if (state != null) rtn = new ArrayList<IEfferentCommand>(state.values());
    }

    return rtn;
  }

  public Collection<IIdentifier> getIdentifiersByAgent(IIdentifier agentId)
  {
    if(_byAgent.containsKey(agentId))
      return new HashSet<IIdentifier>(_byAgent.get(agentId));
    return Collections.emptySet();
  }

  public Collection<IIdentifier> getIdentifiersBySensor(IIdentifier sensorId)
  {
    if(_bySensor.containsKey(sensorId))
      return new HashSet<IIdentifier>(_bySensor.get(sensorId));
    return Collections.emptySet();
  }

}
