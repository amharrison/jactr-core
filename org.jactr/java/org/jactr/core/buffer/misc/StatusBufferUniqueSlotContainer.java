package org.jactr.core.buffer.misc;

import java.util.ArrayList;
import java.util.Collection;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.buffer.six.IStatusBuffer;
import org.jactr.core.slot.ISlot;
import org.jactr.core.slot.IUniqueSlotContainer;

public class StatusBufferUniqueSlotContainer implements IUniqueSlotContainer
{
  /**
  * Logger definition
  */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
      .getLogger(StatusBufferUniqueSlotContainer.class);

  private IStatusBuffer              _buffer;

  private IUniqueSlotContainer       _delegate;

  public StatusBufferUniqueSlotContainer(IStatusBuffer buffer,
      IUniqueSlotContainer delegateContainer)
  {
    _delegate = delegateContainer;
    _buffer = buffer;
  }

  @Override
  public Collection<? extends ISlot> getSlots()
  {
    return getSlots(new ArrayList<>());
  }

  @Override
  public Collection<ISlot> getSlots(Collection<ISlot> container)
  {
    if (container == null) container = new ArrayList<>();
    _buffer.getSlots(container);
    _delegate.getSlots(container);
    return container;
  }

  @Override
  public void addSlot(ISlot slot)
  {
    throw new UnsupportedOperationException(
        "cannot add to statusBufferUniqueSlotContainer");

  }

  @Override
  public void removeSlot(ISlot slot)
  {
    throw new UnsupportedOperationException(
        "cannot remove from statusBufferUniqueSlotContainer");

  }

  @Override
  public ISlot getSlot(String slotName)
  {
    if (slotName.startsWith(":")) return _buffer.getSlot(slotName.substring(1));

    return _delegate.getSlot(slotName);
  }

  @Override
  public boolean hasSlot(String slotName)
  {
    if (slotName.startsWith(":")) return _buffer.hasSlot(slotName.substring(1));

    return _delegate.hasSlot(slotName);
  }

  @Override
  public String toString()
  {
    return _buffer.getName();
  }

}
