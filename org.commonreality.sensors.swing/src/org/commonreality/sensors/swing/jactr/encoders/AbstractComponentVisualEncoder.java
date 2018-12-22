package org.commonreality.sensors.swing.jactr.encoders;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.modalities.visual.IVisualPropertyHandler;
import org.commonreality.object.IAfferentObject;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.slot.IMutableSlot;
import org.jactr.modules.pm.visual.memory.IVisualMemory;
import org.jactr.modules.pm.visual.memory.impl.encoder.AbstractVisualEncoder;

/**
 * base class for generic AWT component encoders. This will create a new chunk
 * based on the chunktype name passed in the
 * {@link #AbstractComponentVisualEncoder(String)} constructor. Extenders must
 * implement {@link #canEncodeVisualObjectType(IAfferentObject)}, which will
 * usually check
 * {@link IVisualPropertyHandler#getTypes(org.commonreality.object.ISimulationObject)}
 * with the {@link IAfferentObject}, and test those strings looking for some
 * particular identifier that will associate it with this chunk type. The
 * majority of the perceptual feature processing will be handled by the default
 * implementation of
 * {@link #updateSlots(IAfferentObject, org.jactr.core.chunk.IChunk, org.jactr.modules.pm.visual.memory.IVisualMemory)}
 * including type, token, and value. any additional custom processing should be
 * done in {@link #updateComponentSlots(IAfferentObject, IChunk, IVisualMemory)}
 * .
 * 
 * @author harrison
 */
public abstract class AbstractComponentVisualEncoder extends
    AbstractVisualEncoder
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractComponentVisualEncoder.class);

  /**
   * @param chunkTypeName
   *          name of the chunk type that will be used to represent this
   *          information (must be extended from visual-object)
   */
  public AbstractComponentVisualEncoder(String chunkTypeName)
  {
    super(chunkTypeName);
  }

  /**
   * use {@link #updateComponentSlots(IAfferentObject, IChunk, IVisualMemory)}
   * to customize
   */
  final protected void updateSlots(IAfferentObject afferentObject,
      IChunk encoding, IVisualMemory memory)
  {
    super.updateSlots(afferentObject, encoding, memory);
    
    /*
     * for now we are assuming everything is enabled..
     */
    IMutableSlot enabled = (IMutableSlot) encoding.getSymbolicChunk().getSlot(
        "enabled");
    try
    {
      enabled.setValue(getHandler().getBoolean("enabled", afferentObject));
    }
    catch (Exception e)
    {
      enabled.setValue(Boolean.FALSE);
    }

    updateComponentSlots(afferentObject, encoding, memory);
  }

  /**
   * override to set additional slot values for this custom percept.
   * 
   * @param guiPercept
   * @param perceptualEncoding
   * @param visualMemory
   */
  abstract protected void updateComponentSlots(IAfferentObject guiPercept,
      IChunk perceptualEncoding, IVisualMemory visualMemory);

}
