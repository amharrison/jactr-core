package org.commonreality.modalities.vocal;

/*
 * default logging
 */
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.IRealObject;

public class VocalUtilities
{

  static public boolean canVocalize(IEfferentObject efferentObject)
  {
    if (efferentObject.hasProperty(VocalConstants.CAN_VOCALIZE))
      return (Boolean) efferentObject.getProperty(VocalConstants.CAN_VOCALIZE);
    return false;
  }

  static public String getVocalization(IEfferentObject efferentObject)
  {
    if (efferentObject.hasProperty(VocalConstants.VOCALIZATON))
      return (String) efferentObject.getProperty(VocalConstants.VOCALIZATON);
    return null;
  }

  static public IIdentifier getSpeaker(IRealObject realObject)
  {
    if (realObject.hasProperty(VocalConstants.SPEAKER))
      return (IIdentifier) realObject.getProperty(VocalConstants.SPEAKER);
    return null;
  }
}
