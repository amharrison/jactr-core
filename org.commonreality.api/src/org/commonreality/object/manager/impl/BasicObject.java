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
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.UnknownPropertyNameException;

/**
 * @author developer
 */
public class BasicObject implements IMutableObject
{
  /**
   * 
   */
  private static final long   serialVersionUID = -5964000146053110470L;

  /**
   * logger definition
   */
  static private final Log    LOGGER           = LogFactory
                                                   .getLog(BasicObject.class);

  private IIdentifier         _identifier;

  private Map<String, Object> _properties;


  public BasicObject(IIdentifier identifier)
  {
    _identifier = identifier;
    _properties = new TreeMap<String, Object>();
  }

  public BasicObject(BasicObject origin)
  {
    this(origin.getIdentifier());
    for (String key : origin.getProperties())
      setProperty(key, origin.getProperty(key));
  }

  synchronized public boolean setProperty(String keyName, Object value)
  {
    _properties.put(keyName, value);
    return true;
  }

  

  /**
   * @see org.commonreality.object.ISimulationObject#getProperty(java.lang.String)
   */
  final synchronized public Object getProperty(String keyName) throws UnknownPropertyNameException
  {
    if(!hasProperty(keyName))
      throw new UnknownPropertyNameException(getIdentifier(), keyName);
    
    return _properties.get(keyName);
  }

  /**
   * @see org.commonreality.object.ISimulationObject#hasProperty(java.lang.String)
   */
  final public boolean hasProperty(String keyName)
  {
    return _properties.containsKey(keyName);
  }

  /**
   * @see org.commonreality.identifier.IIdentifiable#getIdentifier()
   */
  public IIdentifier getIdentifier()
  {
    return _identifier;
  }

  /**
   * @see org.commonreality.object.ISimulationObject#getProperties()
   */
  final public Collection<String> getProperties()
  {
    return Collections.unmodifiableCollection(_properties.keySet());
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((_identifier == null) ? 0 : _identifier.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final BasicObject other = (BasicObject) obj;
    if (_identifier == null)
    {
      if (other._identifier != null) return false;
    }
    else if (!_identifier.equals(other._identifier)) return false;
    return true;
  }

  /**
   * @see org.commonreality.object.ISimulationObject#getPropertyMap()
   */
  public Map<String, Object> getPropertyMap()
  {
    return Collections.unmodifiableMap(_properties);
  }

  public String toString()
  {
    return _identifier.toString();
  }
}
