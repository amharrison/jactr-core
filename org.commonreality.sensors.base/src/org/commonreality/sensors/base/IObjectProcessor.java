package org.commonreality.sensors.base;


import java.util.Map;

import org.commonreality.object.IMutableObject;

/**
 * processes an existing object. This allows new feature processors to be added
 * that can process existing objects.
 * 
 * @author harrison
 */
public interface IObjectProcessor<K extends IObjectKey>
{
  public boolean handles(K object);

  /**
   * @param object
   * @param simulationObject
   *          
   */
  public void process(K object, IMutableObject simulationObject);
  
  /**
   * called when the object key is deleted..
   * @param object
   */
  public void deleted(K object);
  
  public void installed(PerceptManager manager);
  
  public void uninstalled(PerceptManager manager);
  public void configure(Map<String, String> options);
}
