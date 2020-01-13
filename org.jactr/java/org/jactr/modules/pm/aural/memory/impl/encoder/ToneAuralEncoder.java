package org.jactr.modules.pm.aural.memory.impl.encoder;

import org.commonreality.object.IAfferentObject;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunk.ISymbolicChunk;
import org.jactr.core.slot.IMutableSlot;
import org.jactr.modules.pm.aural.IAuralModule;
import org.jactr.modules.pm.aural.memory.IAuralMemory;

/*
 * default logging
 */

import org.slf4j.LoggerFactory;

public class ToneAuralEncoder extends AbstractAuralEncoder
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(ToneAuralEncoder.class);

  public ToneAuralEncoder()
  {
    super("tone");
  }

  @Override
  protected void updateSlots(IAfferentObject afferent, IChunk encoding,
      IAuralMemory memory)
  {
    super.updateSlots(afferent, encoding, memory);

    try
    {
      ISymbolicChunk sc = encoding.getSymbolicChunk();

      if (getHandler().hasProperty("aural.pitch", afferent))
      {
        double pitch = getHandler().getDouble("aural.pitch", afferent);

        ((IMutableSlot) sc.getSlot(IAuralModule.PITCH_SLOT)).setValue(pitch);
      }
    }
    catch (Exception e)
    {
      throw new IllegalStateException("Could not set slot values for "
          + getChunkTypeName() + " encoding of " + afferent.getIdentifier(), e);
    }

  }

}
