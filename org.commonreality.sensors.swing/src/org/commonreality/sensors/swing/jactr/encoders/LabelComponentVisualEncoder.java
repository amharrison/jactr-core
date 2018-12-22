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

public class LabelComponentVisualEncoder extends AbstractComponentVisualEncoder
{

  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(LabelComponentVisualEncoder.class);

  public LabelComponentVisualEncoder()
  {
    super("label");
  }

  @Override
  protected void updateComponentSlots(IAfferentObject guiPercept,
      IChunk perceptualEncoding, IVisualMemory visualMemory)
  {
    /*
     * we set the text slot..
     */
    ((IMutableSlot) perceptualEncoding.getSymbolicChunk().getSlot("text"))
        .setValue(getHandler().getText(guiPercept));
  }

  @Override
  protected boolean canEncodeVisualObjectType(IAfferentObject afferentObject)
  {
    try
    {
      for (String type : getHandler().getTypes(afferentObject))
        if (type.equals("label")
            && getHandler().hasProperty(IVisualPropertyHandler.TEXT,
                afferentObject)) return true;
      return false;
    }
    catch (Exception e)
    {
      return false;
    }
  }

}
