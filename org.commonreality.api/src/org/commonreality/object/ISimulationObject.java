/*
 * Created on Feb 23, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.object;

import java.util.Collection;
import java.util.Map;

import org.commonreality.identifier.IIdentifiable;

/**
 * Any object in the simulation
 * 
 * @author developer
 */
public interface ISimulationObject extends IIdentifiable
{

  public Object getProperty(String keyName) throws UnknownPropertyNameException;

  public boolean hasProperty(String keyName);

  public Collection<String> getProperties();

  public Map<String, Object> getPropertyMap();

}
