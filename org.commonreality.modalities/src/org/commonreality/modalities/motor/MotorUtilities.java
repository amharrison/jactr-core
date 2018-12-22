package org.commonreality.modalities.motor;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.ISimulationObject;

public class MotorUtilities
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(MotorUtilities.class);

  
  
  static public String getName(IEfferentObject object)
  {
    try
    {
      return (String) object.getProperty(MotorConstants.NAME);
    }
    catch(Exception e)
    {
      return null;
    }
  }
  
  static public boolean isMotor(IEfferentObject object)
  {
    try
    {
      return (Boolean) object.getProperty(MotorConstants.IS_MOTOR);
    }
    catch(Exception e)
    {
      return false;
    }
  }
  
  static public IIdentifier getParentIdentifier(IEfferentObject object)
  {
    try
    {
      return (IIdentifier) object.getProperty(MotorConstants.PARENT_IDENTIFIER);
    }
    catch(Exception e)
    {
      return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  static public Collection<IIdentifier> getChildIdentifiers(IEfferentObject object)
  {
    try
    {
      return (Collection<IIdentifier>) object.getProperty(MotorConstants.CHILD_IDENTIFIERS);
    }
    catch(Exception e)
    {
      return Collections.EMPTY_LIST;
    }
  }
  
  static public double[] getPosition(IEfferentObject object)
  {
    try
    {
      return getDoubles(MotorConstants.POSITION, object);
    }
    catch(Exception e)
    {
      return null;
    }
  }
  
  static public double[] getPositionRange(IEfferentObject object)
  {
    try
    {
      return getDoubles(MotorConstants.POSITION_RANGE, object); 
    }
    catch(Exception e)
    {
      return null;
    }
  }
  
  static public double[] getRate(IEfferentObject object)
  {
    try
    {
      return getDoubles(MotorConstants.RATE, object); 
    }
    catch(Exception e)
    {
      return null;
    }
  }
  
  static public double[] getRateRange(IEfferentObject object)
  {
    try
    {
      return getDoubles(MotorConstants.RATE_RANGE, object); 
    }
    catch(Exception e)
    {
      return null;
    }
  }
  
  static public double[] getDoubles(String propertyName, ISimulationObject object)
  {
    return copyArray(propertyName, object);
  }
  
  static private double[] copyArray(String propertyName, ISimulationObject object)
  {
    double[] rate = (double[]) object.getProperty(propertyName);
    double[] rtn = new double[rate.length];
    System.arraycopy(rate, 0, rtn, 0, rate.length);
    return rtn;
  }
}
