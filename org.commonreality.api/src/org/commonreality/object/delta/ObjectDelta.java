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
package org.commonreality.object.delta;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IMutableObject;

/**
 * @author developer
 */
public class ObjectDelta implements IObjectDelta
{
  /**
   * 
   */
  private static final long serialVersionUID = 5178224552414132913L;

  /**
   * logger definition
   */
  static private final Log      LOGGER = LogFactory.getLog(ObjectDelta.class);

  private IIdentifier           _identifier;

  protected Map<String, Object> _newValues;

  protected Map<String, Object> _oldValues;

  public ObjectDelta(IIdentifier identifier, Map<String, Object> newValues,
      Map<String, Object> oldValues)
  {
    _identifier = identifier;
    _newValues = new TreeMap<String, Object>(newValues);
    _oldValues = new TreeMap<String, Object>(oldValues);
  }
  
  public IObjectDelta copy()
  {
    return new ObjectDelta(_identifier, _newValues, _oldValues);
  }

  public void merge(IObjectDelta delta)
  {
    if (!_identifier.equals(delta.getIdentifier()))
      throw new IllegalArgumentException(
          "Object deltas do not match, expecting " + _identifier + " got "
              + delta.getIdentifier());

    for (String prop : delta.getChangedProperties())
    {
//      _oldValues.put(prop, _newValues.get(prop));
      _newValues.put(prop, delta.getNewValue(prop));
    }
  }

  /**
   * @see org.commonreality.object.delta.IObjectDelta#apply(java.lang.Object)
   */
  public void apply(IMutableObject object)
  {
    if (!_identifier.equals(object.getIdentifier()))
      throw new RuntimeException(
          "Identifier of delta does not match object, aborting change");

    for (Map.Entry<String, Object> entry : _newValues.entrySet())
    {
      String propertyName = entry.getKey();
      Object value = entry.getValue();

      object.setProperty(propertyName, value);
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(_identifier + "." + propertyName + " = " + value + "("+object.getProperty(propertyName)+")");
    }
  }

  /**
   * @see org.commonreality.object.delta.IObjectDelta#getIdentifier()
   */
  public IIdentifier getIdentifier()
  {
    return _identifier;
  }

  /**
   * @see org.commonreality.object.delta.IObjectDelta#getChangedProperties()
   */
  public Collection<String> getChangedProperties()
  {
    return Collections.unmodifiableCollection(_newValues.keySet());
  }
  
  public boolean hasChangedProperties()
  {
    return _newValues.size()!=0;
  }

  /**
   * @see org.commonreality.object.delta.IObjectDelta#getNewValue(java.lang.String)
   */
  public Object getNewValue(String propertyName)
  {
    return _newValues.get(propertyName);
  }

  /**
   * @see org.commonreality.object.delta.IObjectDelta#getOldValue(java.lang.String)
   */
  public Object getOldValue(String propertyName)
  {
    return _oldValues.get(propertyName);
  }

  public String toString()
  {
    return "[Delta:"+getIdentifier()+":"+getChangedProperties()+"]";
  }
}
