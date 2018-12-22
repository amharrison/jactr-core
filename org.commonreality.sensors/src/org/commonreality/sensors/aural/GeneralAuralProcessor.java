package org.commonreality.sensors.aural;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.aural.DefaultAuralPropertyHandler;
import org.commonreality.modalities.aural.IAuralPropertyHandler;
import org.commonreality.net.message.command.object.IObjectCommand;
import org.commonreality.net.message.request.object.ObjectCommandRequest;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.IRealObject;
import org.commonreality.object.manager.IRequestableAfferentObjectManager;
import org.commonreality.object.manager.event.IObjectEvent;
import org.commonreality.object.manager.event.IRealObjectListener;
import org.commonreality.object.manager.impl.AfferentObject;
import org.commonreality.object.manager.impl.RealObjectManager;
import org.commonreality.sensors.ISensor;
import org.commonreality.sensors.handlers.AddRemoveTracker;

/**
 * the general aural processor is a component that monitors all the realobjects
 * looking for anything that is audible. Any sound is output to the simulation
 * as an {@link IRealObject} with the {@link IAuralPropertyHandler} properties.
 * It will then transform that aural into an {@link IAfferentObject} for each of
 * the connected agents. Calls to {@link #update(double)} will remove any
 * {@link IAfferentObject} that have exceeded their duration.<br>
 * <br>
 * If additional properties need to be set on a percept level (i.e. spatial
 * localization of the sound), this can be done by adding an
 * {@link IAuralMutator} via
 * {@link #add(org.commonreality.sensors.aural.GeneralAuralProcessor.IAuralMutator)}
 * <br>
 * <br>
 * This should be attached to the sensors {@link RealObjectManager} on an
 * executor other than the sensors IO executor (since it may have to block to
 * create new {@link IAfferentObject}s)<br>
 * 
 * @author harrison
 */
public class GeneralAuralProcessor implements IRealObjectListener
{
  /**
   * Logger definition
   */
  static private final transient Log  LOGGER        = LogFactory
                                                        .getLog(GeneralAuralProcessor.class);

  private AddRemoveTracker            _tracker      = new AddRemoveTracker();

  private ISensor                     _sensor;

  private DefaultAuralPropertyHandler _auralHandler = new DefaultAuralPropertyHandler();

  private Collection<IAuralMutator>   _mutators     = new ArrayList<IAuralMutator>();

  public GeneralAuralProcessor(ISensor sensor)
  {
    _sensor = sensor;
  }

  public void add(IAuralMutator mutator)
  {
    _mutators.add(mutator);
  }

  /**
   * update all {@link IAfferentObject}s representing percepts of hearable aural
   * objects, removing any of those that have elapsed.
   * 
   * @param currentTime
   * @return next expiration time or Double.NaN
   */
  public double update(double currentTime)
  {
    return _tracker.update(currentTime, _sensor);
  }

  protected void addAural(IRealObject auralObject)
  {
    double onset = _auralHandler.getOnset(auralObject);
    double duration = _auralHandler.getDuration(auralObject);
    IRequestableAfferentObjectManager raom = _sensor.getAfferentObjectManager();

    Map<String, Object> propertyMap = auralObject.getPropertyMap();
    /*
     * for each connected agent.. create the afferent
     */
    for (IIdentifier agentId : _sensor.getAgentObjectManager().getIdentifiers())
    {
      AfferentObject percept = (AfferentObject) raom.request(agentId);
      percept.setProperty(IAuralPropertyHandler.AURAL_MODALITY, Boolean.TRUE);

      /*
       * we do a straight up copy of all the properties so that if there is any
       * localization information, it can be passed directly..
       */
      for (Map.Entry<String, Object> entry : propertyMap.entrySet())
        percept.setProperty(entry.getKey(), entry.getValue());

      for (IAuralMutator mutator : _mutators)
        mutator.mutate(percept, auralObject, _sensor);

      _tracker.add(percept, onset, onset + duration);
    }

    double now = _sensor.getClock().getTime();
    if (onset >= now)
    {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format(
            "onset time of aural has passed @ %.2f, forcing update", now));
      _tracker.update(now, _sensor);
    }
  }

  protected void removeAural(IAfferentObject aural)
  {
    _sensor.send(new ObjectCommandRequest(_sensor.getIdentifier(), aural
        .getIdentifier().getAgent(), IObjectCommand.Type.REMOVED, Collections
        .singleton((IIdentifier) aural.getIdentifier())));
  }

  public void objectsAdded(IObjectEvent<IRealObject, ?> addEvent)
  {
    /*
     * we only process audibles from outside. if DefaultAuralSensor.queue was
     * used, the local sound will be handled automatically.
     */
    for (IRealObject object : addEvent.getObjects())
      if (_auralHandler.isAudible(object)) if (!object.getIdentifier().getOwner().equals(_sensor.getIdentifier()))
        addAural(object);
  }

  /**
   * this ignores the removal of the original {@link IRealObject} that launched
   * the {@link IAfferentObject} percept of the sound, since the percept is
   * internal
   */
  public void objectsRemoved(IObjectEvent<IRealObject, ?> removeEvent)
  {
    // Noop
  }

  /**
   * aural events don't change once they've been detected
   * 
   * @param updateEvent
   * @see org.commonreality.object.manager.event.IObjectListener#objectsUpdated(org.commonreality.object.manager.event.IObjectEvent)
   */
  public void objectsUpdated(IObjectEvent<IRealObject, ?> updateEvent)
  {
    // IGNORE, aural events dont change
  }

  /**
   * interface used to dynamically configure aural percepts
   * 
   * @author harrison
   */
  static public interface IAuralMutator
  {
    public void mutate(IAfferentObject auralPercept, IRealObject auralSource,
        ISensor sensor);
  }
}
