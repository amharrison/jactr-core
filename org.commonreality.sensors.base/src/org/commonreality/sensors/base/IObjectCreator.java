package org.commonreality.sensors.base;

import java.util.Map;

import org.commonreality.object.IAfferentObject;
import org.commonreality.object.IAgentObject;
import org.commonreality.object.ISensoryObject;
import org.commonreality.sensors.ISensor;


/**
 * Coordinates with {@link PerceptManager} to create {@link IObjectKey}s and their associated
 * {@link ISensoryObject}s (typically a {@link IAfferentObject}).  Ultimately, this class will
 * also be responsible for the removal of the percept.
 * 
 * @author harrison
 *
 * @param <K>
 */
public interface IObjectCreator<K extends IObjectKey>
{
  /**
   * can this creator deal with this type of object
   */
  public boolean handles(Object object);

  /**
   * create the initial percept, which can later be refined by {@link IObjectProcessor}s
   * @param objectKey
   * @param object
   * @param sensor
   * @param agent
   * @return
   */
  public ISensoryObject createObject(K objectKey, Object object,
      ISensor sensor, IAgentObject agent);

  /**
   * create the key for this object
   * 
   * @param object
   * @return
   */
  public K createKey(Object object);
  
  /**
   * test to see if the object key can be destroyed, removing the percept entirely.
   * @param objectKey
   * @return
   */
  public boolean canDelete(IObjectKey objectKey);
  
  /**
   * delete the key and disconnect any listeners. The actual notification and
   * removal of the percept is handled by the base sensor
   * @param objectKey
   * @return true if the key was deleted
   */
  public boolean deleteKey(IObjectKey objectKey);
  
  public void installed(PerceptManager manager);
  public void uninstalled(PerceptManager manager);
  public void configure(Map<String, String> options);
}
