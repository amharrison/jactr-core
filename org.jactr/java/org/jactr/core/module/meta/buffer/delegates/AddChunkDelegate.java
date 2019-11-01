package org.jactr.core.module.meta.buffer.delegates;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.buffer.delegate.IRequestDelegate;
import org.jactr.core.buffer.meta.IMetaBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunk.ISymbolicChunk;
import org.jactr.core.logging.IMessageBuilder;
import org.jactr.core.logging.Logger;
import org.jactr.core.logging.impl.MessageBuilderFactory;
import org.jactr.core.model.IModel;
import org.jactr.core.module.meta.MetaModule;
import org.jactr.core.production.request.ChunkRequest;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.production.request.IRequest;
import org.jactr.core.production.request.SlotBasedRequest;
import org.jactr.core.slot.DefaultConditionalSlot;
import org.jactr.core.slot.IConditionalSlot;
import org.jactr.core.slot.ISlot;
import org.jactr.core.utils.collections.FastCollectionFactory;

public class AddChunkDelegate implements IRequestDelegate
{

  public AddChunkDelegate()
  {

  }

  protected IConditionalSlot createConditionalSlotFromCondition(
      IChunk condition, MetaModule mm)
  {
    ISymbolicChunk sc = condition.getSymbolicChunk();

    String slotName = (String) sc.getSlot("slot").getValue();
    Object value = sc.getSlot("value").getValue();
    IChunk conditionValue = (IChunk) sc.getSlot("condition").getValue();
    int conditionIndex = IConditionalSlot.EQUALS;

    if (conditionValue.equals(mm.getNotEquals()))
      conditionIndex = IConditionalSlot.NOT_EQUALS;
    else if (conditionValue.equals(mm.getLessThan()))
      conditionIndex = IConditionalSlot.LESS_THAN;
    else if (conditionValue.equals(mm.getLessThanEquals()))
      conditionIndex = IConditionalSlot.LESS_THAN_EQUALS;
    else if (conditionValue.equals(mm.getGreaterThan()))
      conditionIndex = IConditionalSlot.GREATER_THAN;
    else if (conditionValue.equals(mm.getGreaterThanEquals()))
      conditionIndex = IConditionalSlot.GREATER_THAN_EQUALS;

    return new DefaultConditionalSlot(slotName, conditionIndex, value);
  }

  @Override
  public boolean request(IRequest request, IActivationBuffer buffer,
      double requestTime)
  {
    ChunkRequest ctr = (ChunkRequest) request;
    IMetaBuffer buff = (IMetaBuffer) buffer;
    MetaModule mm = (MetaModule) buffer.getModule();

    if (ctr.getChunkType().isA(mm.getConditionType()))
    {
      /*
       * we assume a chunktype request already exists..
       */
      IRequest existingRequest = (IRequest) buff.getContents();
      /*
       * assemble the slot condition based on the chunk condition.
       */
      IConditionalSlot conditionalSlot = createConditionalSlotFromCondition(
          ctr.getChunk(), mm);

      /*
       * if no request exists, make it slot based
       */
      if (existingRequest == null)
      {
        existingRequest = new SlotBasedRequest();
        ((SlotBasedRequest) existingRequest).addSlot(conditionalSlot);
        buff.setContents(existingRequest);
      }
      else if (existingRequest instanceof ChunkTypeRequest)
        ((ChunkTypeRequest) existingRequest).addSlot(conditionalSlot);
      else
      {
        /*
         * if not, log the error and go on
         */
        IModel model = mm.getModel();
        IMessageBuilder mb = MessageBuilderFactory.newInstance();

        mb.append("Cannot append condition to request " + existingRequest);

        if (Logger.hasLoggers(model)) Logger.log(model, "Meta", mb);

        MessageBuilderFactory.recycle(mb);
        return false;
      }
    }
    else
    {
      /*
       * expand the request using the chunk as default values
       */
      Collection<ISlot> definedSlots = FastCollectionFactory.newInstance();
      definedSlots = ctr.getSlots(definedSlots);

      Collection<ISlot> chunkSlots = FastCollectionFactory.newInstance();
      chunkSlots = ctr.getChunk().getSymbolicChunk().getSlots(chunkSlots);

      Map<String, ISlot> uniqueSlots = new TreeMap<>();
      chunkSlots.forEach((s) -> uniqueSlots.put(s.getName(), s));

      // find missing
      definedSlots.forEach((s) -> uniqueSlots.remove(s.getName()));

      // add missing
      uniqueSlots.values().forEach(s -> ctr.addSlot(s));

      buff.setContents(ctr);

      FastCollectionFactory.recycle(definedSlots);
      FastCollectionFactory.recycle(chunkSlots);
    }

    return true;
  }

  @Override
  public boolean willAccept(IRequest request)
  {
    return request instanceof ChunkRequest;
  }

  @Override
  public void clear()
  {

  }

}
