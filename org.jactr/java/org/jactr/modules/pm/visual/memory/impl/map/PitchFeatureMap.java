package org.jactr.modules.pm.visual.memory.impl.map;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.commonreality.modalities.visual.IVisualPropertyHandler;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.UnknownPropertyNameException;
import org.jactr.core.slot.ISlot;
import org.jactr.modules.pm.visual.IVisualModule;

public class PitchFeatureMap extends AbstractSortedVisualFeatureMap<Double>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(PitchFeatureMap.class);

  public PitchFeatureMap()
  {
    super(IVisualModule.SCREEN_Y_SLOT, IVisualPropertyHandler.RETINAL_LOCATION);
  }

  @Override
  protected boolean isValidValue(ISlot slot)
  {
    return slot.getValue() instanceof Number;
  }

  @Override
  protected Double toData(ISlot slot)
  {
    return ((Number) slot.getValue()).doubleValue();
  }

  @Override
  protected Double extractInformation(IAfferentObject afferentObject)
  {
    try
    {
      return getHandler().getRetinalLocation(afferentObject).getY();
    }
    catch (UnknownPropertyNameException e)
    {
      LOGGER.error("Exception ", e);
      return null;
    }
  }

}
