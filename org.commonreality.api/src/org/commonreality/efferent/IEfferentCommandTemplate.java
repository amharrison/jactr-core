package org.commonreality.efferent;

import java.io.Serializable;

import org.commonreality.agents.IAgent;
import org.commonreality.object.IEfferentObject;

/*
 * default logging
 */

public interface IEfferentCommandTemplate<E extends IEfferentCommand> extends Serializable
{

  /**
   * the name of the command
   * @return
   */
  public String getName();
  
  /**
   * 
   * @return
   */
  public String getDescription();
  
  /**
   * instantiate this template using the supplied object
   * @return
   */
  public E instantiate(IAgent agent, IEfferentObject object) throws Exception;
  
  /**
   * returns true if this {@link IEfferentCommand} is consistent with those created by
   * this template. This is used on the sensor side to make sure that the {@link IEfferentCommand}
   * supplied is acting upon an {@link IEfferentObject} that can be acted upon
   * @param command
   * @return
   */
  public boolean isConsistent(IEfferentCommand command);
}
