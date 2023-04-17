package org.jactr.tools.bloom.funnels;

import org.jactr.core.production.condition.ChunkCondition;
import org.jactr.core.production.condition.ChunkTypeCondition;
import org.jactr.core.production.condition.IBufferCondition;
import org.jactr.core.production.condition.ICondition;
import org.jactr.core.production.condition.ProxyCondition;
import org.jactr.core.production.condition.VariableCondition;
import org.jactr.core.slot.ISlotContainer;
import org.jactr.scripting.condition.ScriptableCondition;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

public enum ConditionFunnel implements Funnel<ICondition> {
  INSTANCE;

  @Override
  public void funnel(ICondition from, PrimitiveSink into)
  {
    into.putUnencodedChars(from.getClass().getSimpleName());

    if (from instanceof IBufferCondition)
      bufferFunnel((IBufferCondition) from, into);

    if (from instanceof ISlotContainer) slotFunnel((ISlotContainer) from, into);

    if (from instanceof ChunkTypeCondition)
      into.putUnencodedChars(((ChunkTypeCondition) from).getChunkType()
          .getSymbolicChunkType().getName());

    if (from instanceof ChunkCondition) into.putUnencodedChars(
        ((ChunkCondition) from).getChunk().getSymbolicChunk().getName());

    if (from instanceof ProxyCondition)
      into.putUnencodedChars(((ProxyCondition) from).getDelegateClassName());

    if (from instanceof VariableCondition)
      into.putUnencodedChars(((VariableCondition) from).getVariableName());

    if (from instanceof ScriptableCondition)
      into.putUnencodedChars(((ScriptableCondition) from).getScript());
  }

  private void bufferFunnel(IBufferCondition bufferCondition,
      PrimitiveSink into)
  {
    into.putUnencodedChars(bufferCondition.getBufferName());
  }

  private void slotFunnel(ISlotContainer slotContainer, PrimitiveSink into)
  {
    slotContainer.getSlots().forEach(s -> {
      SlotFunnel.INSTANCE.funnel(s, into);
    });
  }
}
