package org.jactr.modules.pm.visual.memory.impl.map;

import org.commonreality.modalities.visual.DefaultVisualPropertyHandler;
import org.commonreality.modalities.visual.IVisualPropertyHandler;
import org.commonreality.object.IAfferentObject;
import org.jactr.modules.pm.common.memory.map.AbstractFeatureMap;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public abstract class AbstractVisualFeatureMap<T> extends AbstractFeatureMap<T>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger   LOGGER           = LoggerFactory
                                                                         .getLogger(AbstractVisualFeatureMap.class);

  static private final DefaultVisualPropertyHandler _propertyHandler = new DefaultVisualPropertyHandler();

  static protected IVisualPropertyHandler getHandler()
  {
    return _propertyHandler;
  }


  public AbstractVisualFeatureMap(String requestSlotName, String crPropertyName)
  {
    super(requestSlotName, crPropertyName);
  }


  public boolean isInterestedIn(IAfferentObject object)
  {
    if (!getHandler().hasModality(object)) return false;

    String crPropertyName = getRelevantPropertyName();
    if (crPropertyName != null && object.hasProperty(crPropertyName))
      return true;

    return crPropertyName == null;
  }

  @Override
  protected void objectAdded(IAfferentObject object, T data)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(this + " Added " + object.getIdentifier() + " = " + data);
  }

  @Override
  protected void objectUpdated(IAfferentObject object, T oldData, T newData)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(this + " Updated " + object.getIdentifier() + "  " + oldData
          + " " + newData);
  }

  @Override
  protected void objectRemoved(IAfferentObject object, T data)
  {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug(this + " Removed " + object.getIdentifier() + " = " + data);
  }
  
}
