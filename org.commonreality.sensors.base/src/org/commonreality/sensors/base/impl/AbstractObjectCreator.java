package org.commonreality.sensors.base.impl;

/*
 * default logging
 */
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.ISensoryObject;
import org.commonreality.sensors.ISensor;
import org.commonreality.sensors.base.IObjectCreator;
import org.commonreality.sensors.base.IObjectKey;
import org.commonreality.sensors.base.PerceptManager;

public abstract class AbstractObjectCreator implements IObjectCreator<DefaultObjectKey>
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractObjectCreator.class);

  public void configure(Map<String, String> options)
  {
    //noop
  }

  public DefaultObjectKey createKey(Object object)
  {
    return new DefaultObjectKey(object, this);
  }

  public ISensoryObject createObject(DefaultObjectKey objectKey, Object object,
      ISensor sensor, IAgentObject agent)
  {
    /*
     * create a visual percept that we will fill in shortly..
     */
    IAfferentObject afferentObject = sensor.getAfferentObjectManager().request(
        agent.getIdentifier());
    /*
     * because we just created it, we can safely assume its mutable..
     */
    IMutableObject mutable = (IMutableObject) afferentObject;

    initialize(objectKey, mutable);

    return afferentObject;
  }
  
  /**
   * set up default properties.. this just sets the {@link IVisualPropertyHandler#IS_VISUAL} to true
   * @param objectKey
   * @param afferentPercept
   */
  protected void initialize(DefaultObjectKey objectKey, IMutableObject afferentPercept)
  {

  }


  public void installed(PerceptManager manager)
  {
   //noop
    
  }

  public void uninstalled(PerceptManager manager)
  {
   //noop
    
  }
  
  public boolean canDelete(IObjectKey objectKey)
  {
    return objectKey.getCreator()==this;
  }
  
  /**
   * delete the key and disconnect any listeners. The actual notification and
   * removal of the percept is handled by the base sensor
   * @param objectKey
   * @return true if the key was deleted
   */
  public boolean deleteKey(IObjectKey objectKey)
  {
    return true;
  }

}
