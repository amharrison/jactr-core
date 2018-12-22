/*
 * Created on May 10, 2007 Copyright (C) 2001-2007, Anthony Harrison
 * anh23@pitt.edu (jactr.org) This library is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version. This library is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.object;

import java.util.Collection;

import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.efferent.IEfferentCommandTemplate;

/**
 * any object in the simulation that is actually a part of the agent and can be
 * controlled by using {@link IEfferentCommand}
 * 
 * @author developer
 */
public interface IEfferentObject extends ISensoryObject
{

  public static final String COMMAND_TEMPLATES = "IEfferentObject.commandTemplates";
  public static final String CURRENT_COMMAND = "IEfferentObject.currentCommand";

  /**
   * return a collection of all the command templates that can be applied to
   * this efferent object.
   * 
   * @return
   */
  @SuppressWarnings("unchecked")
  public Collection<IEfferentCommandTemplate> getCommandTemplates();

  /**
   * returns the currently active command. if no command is active (successfully
   * completed or interrupted) this will be null
   * 
   * @return
   */
  public IEfferentCommand getCurrentCommand();
}
