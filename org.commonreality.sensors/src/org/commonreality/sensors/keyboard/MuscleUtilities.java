package org.commonreality.sensors.keyboard;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.IEfferentCommandTemplate;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.motor.MotorConstants;
import org.commonreality.modalities.motor.MovementCommandTemplate;
import org.commonreality.modalities.motor.TranslateCommandTemplate;
import org.commonreality.net.message.command.object.IObjectCommand;
import org.commonreality.net.message.request.object.ObjectCommandRequest;
import org.commonreality.net.message.request.object.ObjectDataRequest;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.delta.DeltaTracker;
import org.commonreality.object.delta.IObjectDelta;
import org.commonreality.sensors.ISensor;
import org.commonreality.sensors.keyboard.map.IDeviceMap;

public class MuscleUtilities
{
  /**
   * Logger definition
   */
  static private final transient Log                             LOGGER = LogFactory
                                                                            .getLog(MuscleUtilities.class);

  private ISensor                                                _sensor;

  private IAgentObject                                           _agent;

  @SuppressWarnings("unchecked")
  private Collection<IEfferentCommandTemplate> _translateTemplates;

  @SuppressWarnings("unchecked")
  private Collection<IEfferentCommandTemplate> _pressTemplates;

  @SuppressWarnings("unchecked")
  public MuscleUtilities(ISensor sensor, IAgentObject agent)
  {
    _sensor = sensor;
    _agent = agent;
   
    TranslateCommandTemplate tct = new TranslateCommandTemplate();
    PressCommandTemplate pct = new PressCommandTemplate();
    ReleaseCommandTemplate rct = new ReleaseCommandTemplate();
    MovementCommandTemplate mct = new MovementCommandTemplate("compound","compound command");
    
    _translateTemplates = new ArrayList<IEfferentCommandTemplate>();
    _pressTemplates = new ArrayList<IEfferentCommandTemplate>();
    
    _pressTemplates.add(mct);
    _pressTemplates.add(tct);
    _pressTemplates.add(pct);
    _pressTemplates.add(rct);
    _translateTemplates.add(mct);
    _translateTemplates.add(tct);
  }

  public void create(boolean createHands, boolean createMouse, IDeviceMap keyboardDevice)
  {
    Collection<DeltaTracker<IMutableObject>> muscles = null;

    if (createHands)
      muscles = createHands();
    else
      muscles = new ArrayList<DeltaTracker<IMutableObject>>();

    if (createMouse) muscles.add(createMouse("mouse", 0, 0));
    
    if(keyboardDevice!=null)
      muscles.add(createKeyboard("keyboard", keyboardDevice));
    

    Collection<IIdentifier> identifiers = new ArrayList<IIdentifier>(muscles
        .size());
    Collection<IObjectDelta> deltas = new ArrayList<IObjectDelta>(muscles
        .size());

    for (DeltaTracker<IMutableObject> tracker : muscles)
    {
      deltas.add(tracker.getDelta());
      identifiers.add(tracker.getIdentifier());
    }

    _sensor.send(new ObjectDataRequest(_sensor.getIdentifier(), _agent
        .getIdentifier(), deltas));
    _sensor.send(new ObjectCommandRequest(_sensor.getIdentifier(), _agent
        .getIdentifier(), IObjectCommand.Type.ADDED, identifiers));
  }

  protected Collection<DeltaTracker<IMutableObject>> createHands()
  {
    Collection<DeltaTracker<IMutableObject>> rtn = createHand("right", 7, 4,
        false);
    rtn.addAll(createHand("left", 4, 4, true));
    return rtn;
  }
  
  protected DeltaTracker<IMutableObject> createKeyboard(String name, IDeviceMap deviceMap)
  {
    IEfferentObject object = _sensor.getEfferentObjectManager().request(
        _agent.getIdentifier());
    DeltaTracker<IMutableObject> tracker = new DeltaTracker<IMutableObject>(
        object);

    tracker.setProperty(MotorConstants.IS_MOTOR, Boolean.TRUE);
    tracker.setProperty(MotorConstants.NAME, name);
    tracker.setProperty(MotorConstants.PARENT_IDENTIFIER, null);
    tracker.setProperty(IEfferentObject.COMMAND_TEMPLATES, Collections.EMPTY_LIST);
    tracker.setProperty(IDeviceMap.DEVICE_MAP_PROPERTY, deviceMap);
    
    return tracker;
  }

  protected DeltaTracker<IMutableObject> createMouse(String name, double x,
      double y)
  {
    IEfferentObject object = _sensor.getEfferentObjectManager().request(
        _agent.getIdentifier());
    DeltaTracker<IMutableObject> tracker = new DeltaTracker<IMutableObject>(
        object);

    tracker.setProperty(MotorConstants.IS_MOTOR, Boolean.TRUE);
    tracker.setProperty(MotorConstants.NAME, name);
    tracker.setProperty(MotorConstants.PARENT_IDENTIFIER, null);
    tracker.setProperty(MotorConstants.POSITION, new double[] { x, y });
    tracker.setProperty(MotorConstants.POSITION_RANGE,
        new double[] { Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE,
            Double.MAX_VALUE });
    tracker.setProperty(MotorConstants.RATE, new double[] { 0, 0 });
    tracker.setProperty(MotorConstants.RATE_RANGE,
        new double[] { Double.MIN_VALUE, Double.MAX_VALUE, Double.MIN_VALUE,
            Double.MAX_VALUE });
    tracker.setProperty(IEfferentObject.COMMAND_TEMPLATES, _pressTemplates);

    return tracker;
  }

  protected Collection<DeltaTracker<IMutableObject>> createHand(String name,
      double x, double y, boolean fingersDecrease)
  {
    IEfferentObject object = _sensor.getEfferentObjectManager().request(
        _agent.getIdentifier());
    DeltaTracker<IMutableObject> tracker = new DeltaTracker<IMutableObject>(
        object);

    tracker.setProperty(MotorConstants.IS_MOTOR, Boolean.TRUE);
    tracker.setProperty(MotorConstants.NAME, name);
    tracker.setProperty(MotorConstants.PARENT_IDENTIFIER, null);
    tracker.setProperty(MotorConstants.POSITION, new double[] { x, y });
    tracker.setProperty(MotorConstants.POSITION_RANGE, new double[] { x - 21,
        x + 21, y - 2, y + 2, });
    tracker.setProperty(MotorConstants.RATE, new double[] { 0, 0 });
    tracker.setProperty(MotorConstants.RATE_RANGE, new double[] { 0, 100, 0,
        100 });
    tracker.setProperty(IEfferentObject.COMMAND_TEMPLATES, _translateTemplates);

    Collection<DeltaTracker<IMutableObject>> rtn = new ArrayList<DeltaTracker<IMutableObject>>(
        6);
    Collection<IIdentifier> children = new ArrayList<IIdentifier>(5);

    double offset = 1;
    if (fingersDecrease) offset = -1;

    DeltaTracker<IMutableObject> finger = createFinger(name + "-index", object,
        x, y);
    children.add(finger.getIdentifier());
    rtn.add(finger);

    finger = createFinger(name + "-thumb", object, (x - 2 * offset), y - 2);
    children.add(finger.getIdentifier());
    rtn.add(finger);

    x += offset;
    finger = createFinger(name + "-middle", object, x, y);
    children.add(finger.getIdentifier());
    rtn.add(finger);

    x += offset;
    finger = createFinger(name + "-ring", object, x, y);
    children.add(finger.getIdentifier());
    rtn.add(finger);

    x += offset;
    finger = createFinger(name + "-pinkie", object, x, y);
    children.add(finger.getIdentifier());
    rtn.add(finger);

    tracker.setProperty(MotorConstants.CHILD_IDENTIFIERS, children);
    rtn.add(tracker);

    return rtn;
  }

  protected DeltaTracker<IMutableObject> createFinger(String name,
      IEfferentObject hand, double x, double y)
  {
    IEfferentObject object = _sensor.getEfferentObjectManager().request(
        _agent.getIdentifier());
    DeltaTracker<IMutableObject> tracker = new DeltaTracker<IMutableObject>(
        object);

    tracker.setProperty(MotorConstants.IS_MOTOR, Boolean.TRUE);
    tracker.setProperty(MotorConstants.NAME, name);
    tracker.setProperty(MotorConstants.PARENT_IDENTIFIER, hand.getIdentifier());
    tracker.setProperty(MotorConstants.POSITION, new double[] { x, y, 1});
    tracker.setProperty(MotorConstants.POSITION_RANGE, new double[] { x - 2,
        x + 2, y - 2, y + 2, 0, 1});
    tracker.setProperty(MotorConstants.RATE, new double[] { 0, 0, 0});
    tracker.setProperty(MotorConstants.RATE_RANGE, new double[] { 0, 100, 0,
        100, 0, 100});
    tracker.setProperty(IEfferentObject.COMMAND_TEMPLATES, _pressTemplates);

    return tracker;
  }
}
