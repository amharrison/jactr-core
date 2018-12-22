/*
 * Created on Feb 28, 2006 Copyright (C) 2001-5, Anthony Harrison anh23@pitt.edu
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
package org.commonreality.modalities;

import org.commonreality.object.IPropertyHandler;
import org.commonreality.object.ISimulationObject;
import org.commonreality.object.UnknownPropertyNameException;

public class DefaultPropertyHandler implements IPropertyHandler
{

  protected boolean check(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    if (realObject.hasProperty(propertyName)) return true;
    throw new UnknownPropertyNameException(realObject.getIdentifier(),
        propertyName);
  }

  public boolean hasProperty(String name, ISimulationObject object)
  {
    return object.hasProperty(name);
  }

  public double getDouble(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    check(propertyName, realObject);
    return ((Number) realObject.getProperty(propertyName)).doubleValue();
  }

  public double[] getDoubles(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    check(propertyName, realObject);
    double[] val = (double[]) realObject.getProperty(propertyName);
    double[] rtn = new double[val.length];
    System.arraycopy(val, 0, rtn, 0, val.length);
    return rtn;
  }

  public int getInt(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    check(propertyName, realObject);
    return ((Number) realObject.getProperty(propertyName)).intValue();
  }

  public int[] getInts(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    check(propertyName, realObject);
    int[] val = (int[]) realObject.getProperty(propertyName);
    int[] rtn = new int[val.length];
    System.arraycopy(val, 0, rtn, 0, val.length);
    return rtn;
  }

  public String getString(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    check(propertyName, realObject);
    return (String) realObject.getProperty(propertyName);
  }

  public String[] getStrings(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    check(propertyName, realObject);
    String[] val = (String[]) realObject.getProperty(propertyName);
    String[] rtn = new String[val.length];
    System.arraycopy(val, 0, rtn, 0, val.length);
    return rtn;
  }

  public boolean getBoolean(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException
  {
    check(propertyName, realObject);
    return ((Boolean) realObject.getProperty(propertyName)).booleanValue();
  }

}
