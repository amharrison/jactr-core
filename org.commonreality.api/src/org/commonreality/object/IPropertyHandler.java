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
package org.commonreality.object;

public interface IPropertyHandler
{

  public boolean hasProperty(String propertyName, ISimulationObject realObject);

  public boolean getBoolean(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public double getDouble(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public double[] getDoubles(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public int getInt(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public int[] getInts(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public String getString(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException;

  public String[] getStrings(String propertyName, ISimulationObject realObject)
      throws UnknownPropertyNameException;
}
