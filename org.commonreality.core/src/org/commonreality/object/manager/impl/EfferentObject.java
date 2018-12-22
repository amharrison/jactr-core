/*
 * Created on May 11, 2007 Copyright (C) 2001-2007, Anthony Harrison
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
package org.commonreality.object.manager.impl;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.efferent.IEfferentCommandTemplate;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.identifier.ISensoryIdentifier;

/**
 * @author developer
 */
public class EfferentObject extends BasicObject implements IEfferentObject
{
  /**
   * 
   */
  private static final long serialVersionUID = 2803302359428416692L;

  /**
   * logger definition
   */
  static private final Log    LOGGER            = LogFactory
                                                    .getLog(EfferentObject.class);

  /**
   * @param identifier
   */
  public EfferentObject(IIdentifier identifier)
  {
    super(identifier);
    setProperty(COMMAND_TEMPLATES, Collections.EMPTY_LIST);    
    setProperty(CURRENT_COMMAND, null);
  }


  @Override
  public ISensoryIdentifier getIdentifier()
  {
    return (ISensoryIdentifier) super.getIdentifier();
  }

  @SuppressWarnings("unchecked")
  public Collection<IEfferentCommandTemplate> getCommandTemplates()
  {
    return (Collection<IEfferentCommandTemplate>) getProperty(COMMAND_TEMPLATES);
  }

  public IEfferentCommand getCurrentCommand()
  {
    return (IEfferentCommand) getProperty(CURRENT_COMMAND);
  }

}
