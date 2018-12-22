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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.ISimulationObject;

/**
 * @author developer
 */
public class DeltaTracker<O extends IMutableObject> implements IMutableObject
{
  /**
   * logger definition
   */
  static private final Log    LOGGER = LogFactory.getLog(DeltaTracker.class);

  private ISimulationObject   _actualObject;

  private Map<String, Object> _deltaProperties;

  public DeltaTracker(ISimulationObject object)
  {
    if (object instanceof DeltaTracker)
      throw new IllegalArgumentException(
          "Cannot wrap a delta tracker in another delta tracker");

    _actualObject = object;
    _deltaProperties = new TreeMap<String, Object>();
  }

  /**
   * @see org.commonreality.identifier.IIdentifiable#getIdentifier()
   */
  public IIdentifier getIdentifier()
  {
    return _actualObject.getIdentifier();
  }

  @SuppressWarnings("unchecked")
  public O get()
  {
    return (O) _actualObject;
  }

  public ObjectDelta getDelta()
  {
    return getDelta(true);
  }

  /**
   * compute the object delta for this tracker by actually iterating through the
   * properties and comparing them to the actual object. If clearProperties is
   * true, the changed properties will be cleared after the delta is generated.
   * In most cases, this is what you want to do, however if you are frequently
   * updating an object, possibly faster than common reality can reply, you will
   * want to keep the properties (and avoid the round-trip updates).
   * 
   * @param clearProperties
   * @return
   */
  synchronized public ObjectDelta getDelta(boolean clearProperties)
  {
    /*
     * ok, since we didn't check for actual changes before, we will now.. while
     * we're at it, we'll build the old values
     */
    Map<String, Object> oldValues = new TreeMap<String, Object>();

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Testing for changes to %s : %s",
          getIdentifier(), _deltaProperties));

    Iterator<Map.Entry<String, Object>> itr = _deltaProperties.entrySet()
        .iterator();
    while (itr.hasNext())
    {
      Map.Entry<String, Object> entry = itr.next();
      String key = entry.getKey();

      if (!isActualChange(key, entry.getValue()))
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("No change in %s new: %s current: %s",
              key, entry.getValue(), _actualObject.getProperty(key)));

        itr.remove();
      }
      else if (_actualObject.hasProperty(key))
      {
        if (LOGGER.isDebugEnabled())
          LOGGER.debug(String.format("%s has changed", key));
        oldValues.put(key, _actualObject.getProperty(key));
      }
      else
        oldValues.put(key, null);
    }

    ObjectDelta delta = new ObjectDelta(_actualObject.getIdentifier(),
        _deltaProperties, oldValues);

    // clear the deltas
    if (clearProperties) _deltaProperties.clear();

    return delta;
  }

  synchronized public boolean hasChanged()
  {
    return _deltaProperties.size() != 0;
  }

  /**
   * @see org.commonreality.object.IMutableObject#setProperty(java.lang.String,
   *      java.lang.Object)
   */
  synchronized public boolean setProperty(String keyName, Object value)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("delta." + _actualObject.getIdentifier() + " " + keyName
          + " = " + value);

    _deltaProperties.put(keyName, value);
    return true;
  }

  /**
   * @see org.commonreality.object.ISimulationObject#getProperty(java.lang.String)
   */
  synchronized public Object getProperty(String keyName)
  {
    if (_deltaProperties.containsKey(keyName))
      return _deltaProperties.get(keyName);

    return _actualObject.getProperty(keyName);
  }

  /**
   * @see org.commonreality.object.ISimulationObject#hasProperty(java.lang.String)
   */
  synchronized public boolean hasProperty(String keyName)
  {
    return _deltaProperties.containsKey(keyName)
        || _actualObject.hasProperty(keyName);
  }

  synchronized public Collection<String> getProperties()
  {
    TreeSet<String> rtn = new TreeSet<String>(_deltaProperties.keySet());
    rtn.addAll(_actualObject.getProperties());
    return rtn;
  }

  synchronized public Map<String, Object> getPropertyMap()
  {
    Map<String, Object> rtn = new TreeMap<String, Object>(_actualObject
        .getPropertyMap());
    rtn.putAll(_deltaProperties);
    return rtn;
  }

  @SuppressWarnings("unchecked")
  private boolean isActualChange(String keyName, Object newValue)
  {
    if (newValue != null)
    {
      if (!_actualObject.hasProperty(keyName)) return true;

      Object oldValue = null;

      // if (checkOnlyActualObject)
      oldValue = _actualObject.getProperty(keyName);
      // else
      // oldValue = getProperty(keyName);

      if (newValue == oldValue) return false;

      if (newValue.equals(oldValue)) return false;

      /*
       * now we need to see if they are arrays
       */
      Class newClass = newValue.getClass();
      Class oldClass = Object.class;
      if (oldValue != null) oldClass = oldValue.getClass();
      if (newClass != oldClass) return true;

      if (newClass.isArray() && oldClass.isArray())
      {
        if (newClass.getComponentType() != oldClass.getComponentType())
          return true;

        /*
         * now we have to check the elements
         */
        if (newClass.getComponentType().isPrimitive())
        {
          boolean rtn = true;
          if (newClass.getComponentType() == Float.TYPE)
            rtn = compareFloats((float[]) newValue, (float[]) oldValue);
          else if (newClass.getComponentType() == Double.TYPE)
            rtn = compareDoubles((double[]) newValue, (double[]) oldValue);
          else if (newClass.getComponentType() == Boolean.TYPE)
            rtn = compareBooleans((boolean[]) newValue, (boolean[]) oldValue);
          else if (newClass.getComponentType() == Integer.TYPE)
            rtn = compareInts((int[]) newValue, (int[]) oldValue);
          else if (LOGGER.isWarnEnabled())
            LOGGER.warn("Cannot compare arrays of "
                + newClass.getComponentType().getName());
          return rtn;
        }
        else
        {
          Object[] newArray = (Object[]) newValue;
          Object[] oldArray = (Object[]) oldValue;

          if (newArray.length != oldArray.length) return true;

          for (int i = 0; i < newArray.length; i++)
            if (!newArray[i].equals(oldArray[i])) return true;

          return false;
        }

      }
    }
    else // unsetting the property
    if (_actualObject.hasProperty(keyName)) return true;

    return true;
  }

  protected boolean compareDoubles(double[] one, double[] two)
  {
    if (one.length != two.length) return true;
    for (int i = 0; i < one.length; i++)
      if (one[i] != two[i]) return true;
    return false;
  }

  protected boolean compareFloats(float[] one, float[] two)
  {
    if (one.length != two.length) return true;
    for (int i = 0; i < one.length; i++)
      if (one[i] != two[i]) return true;
    return false;
  }

  protected boolean compareInts(int[] one, int[] two)
  {
    if (one.length != two.length) return true;
    for (int i = 0; i < one.length; i++)
      if (one[i] != two[i]) return true;
    return false;
  }

  protected boolean compareBooleans(boolean[] one, boolean[] two)
  {
    if (one.length != two.length) return true;
    for (int i = 0; i < one.length; i++)
      if (one[i] != two[i]) return true;
    return false;
  }
}
