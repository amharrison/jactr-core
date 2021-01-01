package org.jactr.tools.bloom.funnels;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.production.action.AddAction;
import org.jactr.core.production.action.ExecuteAction;
import org.jactr.core.production.action.IAction;
import org.jactr.core.production.action.IBufferAction;
import org.jactr.core.production.action.OutputAction;
import org.jactr.core.production.action.ProxyAction;
import org.jactr.core.production.action.SetAction;
import org.jactr.core.slot.ISlotContainer;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

public enum ActionFunnel implements Funnel<IAction> {
  INSTANCE;

  @Override
  public void funnel(IAction from, PrimitiveSink into)
  {
    into.putUnencodedChars(from.getClass().getSimpleName());

    if (from instanceof IBufferAction) bufferFunnel((IBufferAction) from, into);

    if (from instanceof ISlotContainer) slotFunnel((ISlotContainer) from, into);

    if (from instanceof AddAction)
    {
      Object referant = ((AddAction) from).getReferant();
      if (referant instanceof IChunkType)
        referant = ((IChunkType) referant).getSymbolicChunkType().getName();
      if (referant instanceof IChunk)
        referant = ((IChunk) referant).getSymbolicChunk().getName();
      into.putUnencodedChars(referant.toString());
    }

    if (from instanceof ExecuteAction)
      into.putUnencodedChars(((ExecuteAction) from).getClassName());

    // modify and remove are taken care of by buffer & slot funnel
    if (from instanceof OutputAction)
      into.putUnencodedChars(((OutputAction) from).getText());

    if (from instanceof ProxyAction)
      into.putUnencodedChars(((ProxyAction) from).getDelegateClassName());

    if (from instanceof SetAction)
    {
      Object referant = ((SetAction) from).getReferant();
      if (referant instanceof IChunkType)
        referant = ((IChunkType) referant).getSymbolicChunkType().getName();
      if (referant instanceof IChunk)
        referant = ((IChunk) referant).getSymbolicChunk().getName();
      into.putUnencodedChars(referant.toString());
    }
  }

  private void bufferFunnel(IBufferAction bufferAction, PrimitiveSink into)
  {
    into.putUnencodedChars(bufferAction.getBufferName());
  }

  private void slotFunnel(ISlotContainer slotContainer, PrimitiveSink into)
  {
    slotContainer.getSlots().forEach(s -> {
      SlotFunnel.INSTANCE.funnel(s, into);
    });
  }
}
