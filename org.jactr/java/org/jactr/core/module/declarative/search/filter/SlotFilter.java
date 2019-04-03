package org.jactr.core.module.declarative.search.filter;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.slot.IConditionalSlot;
import org.jactr.core.slot.ISlot;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;

public class SlotFilter implements IChunkFilter
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(SlotFilter.class);

  private final IConditionalSlot     _slot;

  public SlotFilter(IConditionalSlot cSlot)
  {
    _slot = cSlot;
  }

  @Override
  public boolean accept(IChunk chunk)
  {
    try
    {
      ISlot chunkSlot = chunk.getSymbolicChunk().getSlot(_slot.getName());
      return _slot.matchesCondition(chunkSlot.getValue());
    }
    catch (Exception e)
    {
      LOGGER.error(e.getMessage(), e);

      return false;
    }
  }

}
