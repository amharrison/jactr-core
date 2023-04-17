package org.jactr.modules.pm.visual.memory.impl.map;

 
import org.slf4j.LoggerFactory;
import org.commonreality.modalities.visual.IVisualPropertyHandler;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.UnknownPropertyNameException;
import org.jactr.core.slot.ISlot;
import org.jactr.modules.pm.visual.IVisualModule;

public class DistanceFeatureMap extends AbstractSortedVisualFeatureMap<Double>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(DistanceFeatureMap.class);

  public DistanceFeatureMap()
  {
    super(IVisualModule.SCREEN_Z_SLOT, IVisualPropertyHandler.RETINAL_DISTANCE);
  }

  @Override
  protected Double extractInformation(IAfferentObject afferentObject)
  {
    try
    {
      return getHandler().getRetinalDistance(afferentObject);
    }
    catch (UnknownPropertyNameException e)
    {
      LOGGER.error("exception ", e);
      return null;
    }
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

  

}
