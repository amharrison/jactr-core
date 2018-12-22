package org.commonreality.sensors.aural;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.aural.IAuralPropertyHandler;
import org.commonreality.modalities.aural.ICommonTypes;
import org.commonreality.modalities.vocal.VocalConstants;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.IRealObject;
import org.commonreality.object.manager.IRequestableRealObjectManager;
import org.commonreality.sensors.ISensor;
import org.commonreality.sensors.handlers.AddRemoveTracker;
import org.commonreality.time.impl.BasicClock;

/**
 * static utility class that facilitates aural event creation
 * 
 * @author harrison
 */
public class AuralUtilities
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AuralUtilities.class);

  private ISensor                    _sensor;

  private AddRemoveTracker           _tracker;

  public AuralUtilities(ISensor sensor)
  {
    _sensor = sensor;
    _tracker = new AddRemoveTracker();
  }

  /**
   * request and configure a new sound. this call may block on the IO thread
   * 
   * @param types
   * @param content
   * @param onset
   * @param duration
   * @return
   */
  public IRealObject newSound(String[] types, String content, double onset,
      double duration)
  {
    IMutableObject aural = (IMutableObject) ((IRequestableRealObjectManager) _sensor
        .getRealObjectManager()).request(_sensor.getIdentifier());

    aural.setProperty(IAuralPropertyHandler.AURAL_MODALITY, Boolean.TRUE);
    aural.setProperty(IAuralPropertyHandler.IS_AUDIBLE, Boolean.TRUE);
    aural.setProperty(IAuralPropertyHandler.ONSET,
        BasicClock.constrainPrecision(onset));
    aural.setProperty(IAuralPropertyHandler.DURATION,
        BasicClock.constrainPrecision(duration));
    aural.setProperty(IAuralPropertyHandler.TOKEN, content);
    aural.setProperty(IAuralPropertyHandler.TYPE, types);

    return (IRealObject) aural;
  }

  public IRealObject newVocalization(String content, double onset,
      double duration, IIdentifier agent)
  {
    IRealObject aural = newSound(new String[] { ICommonTypes.SPEECH }, content,
        onset, duration);
    ((IMutableObject) aural).setProperty(VocalConstants.SPEAKER, agent);
    return aural;
  }

  public void queueSound(IRealObject aural)
  {
    double onset = getOnsetTime(aural);
    double expiration = getExpirationTime(aural);

    _tracker.add(aural, onset, expiration);

    double now = _sensor.getClock().getTime();
    if (onset >= now) _tracker.update(now, _sensor);
  }

  public double update(double time)
  {
    return _tracker.update(time, _sensor);
  }

  protected double getOnsetTime(IRealObject object)
  {
    return (Double) object.getProperty(IAuralPropertyHandler.ONSET);
  }

  protected double getExpirationTime(IRealObject object)
  {
    return (Double) object.getProperty(IAuralPropertyHandler.ONSET)
        + (Double) object.getProperty(IAuralPropertyHandler.DURATION);
  }

}
