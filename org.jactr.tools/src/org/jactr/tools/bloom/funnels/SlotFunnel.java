package org.jactr.tools.bloom.funnels;

import org.jactr.core.slot.IConditionalSlot;
import org.jactr.core.slot.ILogicalSlot;
import org.jactr.core.slot.ISlot;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

public enum SlotFunnel implements Funnel<ISlot> {
  INSTANCE;

  @Override
  public void funnel(ISlot from, PrimitiveSink into)
  {
    if (from instanceof ILogicalSlot)
      logicalFunnel((ILogicalSlot) from, into);
    else if (from instanceof IConditionalSlot)
      conditionalFunnel((IConditionalSlot) from, into);
    else
      defaultFunnel(from, into);
  }

  private void logicalFunnel(ILogicalSlot from, PrimitiveSink into)
  {
    into.putInt(from.getOperator());
    from.getSlots().forEach(s -> {
      funnel(s, into);
    });
  }

  private void conditionalFunnel(IConditionalSlot from, PrimitiveSink into)
  {
    into.putUnencodedChars(from.getName());
    into.putInt(from.getCondition());
    Object value = from.getValue();
    if (value == null) value = "null";
    into.putUnencodedChars(value.toString());
  }

  private void defaultFunnel(ISlot from, PrimitiveSink into)
  {
    into.putUnencodedChars(from.getName());
    Object value = from.getValue();
    if (value == null) value = "null";
    into.putUnencodedChars(value.toString());
  }
}
