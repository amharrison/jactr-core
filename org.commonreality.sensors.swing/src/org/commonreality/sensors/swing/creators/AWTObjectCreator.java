package org.commonreality.sensors.swing.creators;

/*
 * default logging
 */
import java.awt.Component;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.visual.DefaultVisualPropertyHandler;
import org.commonreality.modalities.visual.IVisualPropertyHandler;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.ISensoryObject;
import org.commonreality.sensors.ISensor;
import org.commonreality.sensors.base.IObjectCreator;
import org.commonreality.sensors.base.IObjectKey;
import org.commonreality.sensors.swing.key.AWTObjectKey;

/**
 * abstract object creator for AWT/Swing.
 * 
 * @author harrison
 */
public abstract class AWTObjectCreator implements IObjectCreator<AWTObjectKey>
{
  /**
   * Logger definition
   */
  static private final transient Log      LOGGER    = LogFactory
                                                        .getLog(AWTObjectCreator.class);

  protected static IVisualPropertyHandler _pHandler = new DefaultVisualPropertyHandler();

  public AWTObjectKey createKey(Object object)
  {
    return new AWTObjectKey((Component) object, this);
  }

  public ISensoryObject createObject(AWTObjectKey objectKey, Object object,
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
   * set up the initial visual properties for this percept..
   * 
   * @param object
   */
  protected void initialize(AWTObjectKey objectKey, IMutableObject simObject)
  {
    /*
     * wouldn't it be great if this were better documented?
     */
    simObject.setProperty(IVisualPropertyHandler.IS_VISUAL, Boolean.TRUE);
    simObject.setProperty(IVisualPropertyHandler.TYPE, new String[] { "gui" });
  }
  
  public void configure(Map<String, String> options)
  {
    //noop
  }
  
  public boolean canDelete(IObjectKey objectKey)
  {
    return objectKey.getCreator()==this;
  }
  
  public boolean deleteKey(IObjectKey objectKey)
  {
    return true;
  }
}
