package org.jactr.core.slot;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

public class UniqueSlotContainer implements IUniqueSlotContainer
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER      = LoggerFactory
                                                     .getLogger(UniqueSlotContainer.class);

  private Map<String, ISlot>         _slotMap;

  protected boolean                  _useMutable = false;

  public UniqueSlotContainer()
  {
    this(false);
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (_slotMap == null ? 0 : _slotMap.hashCode());
    result = prime * result + (_useMutable ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    UniqueSlotContainer other = (UniqueSlotContainer) obj;
    if (_slotMap == null)
    {
      if (other._slotMap != null) return false;
    }
    else if (!_slotMap.equals(other._slotMap)) return false;
    if (_useMutable != other._useMutable) return false;
    return true;
  }

  public UniqueSlotContainer(boolean useMutableSlots)
  {
    _slotMap = new TreeMap<String, ISlot>();
    _useMutable = useMutableSlots;
  }

  public UniqueSlotContainer(IUniqueSlotContainer container,
      boolean useMutableSlots)
  {
    this(useMutableSlots);
    for (ISlot slot : container.getSlots())
      addSlot(slot);
  }

  public ISlot getSlot(String slotName)
  {
    synchronized (_slotMap)
    {
      return _slotMap.get(slotName.toLowerCase());
    }
  }

  protected ISlot createSlot(ISlot slot)
  {
    if (_useMutable)
      return new DefaultMutableSlot(slot.getName(), slot.getValue());

    return new BasicSlot(slot.getName(), slot.getValue());
  }

  public void addSlot(ISlot slot)
  {
    synchronized (_slotMap)
    {
      _slotMap.put(slot.getName().toLowerCase(), createSlot(slot));
    }
  }

  public Collection<? extends ISlot> getSlots()
  {
    return getSlots(null);
  }

  public Collection<ISlot> getSlots(Collection<ISlot> slots)
  {
    if (slots == null) slots = new ArrayList<ISlot>(_slotMap.size());
    synchronized (_slotMap)
    {
      slots.addAll(_slotMap.values());
    }
    return slots;
  }

  public Collection<IMutableSlot> getMutableSlots()
  {
    if (!_useMutable) return Collections.EMPTY_LIST;
    Collection<IMutableSlot> rtn = new ArrayList<IMutableSlot>(_slotMap.size());
    synchronized (_slotMap)
    {
      for (ISlot slot : _slotMap.values())
        rtn.add((IMutableSlot) slot);
    }
    return rtn;
  }

  public void removeSlot(ISlot slot)
  {
    synchronized (_slotMap)
    {
      _slotMap.remove(slot.getName().toLowerCase());
    }
  }

  public boolean hasSlot(String slotName)
  {
    return _slotMap.containsKey(slotName.toLowerCase());
  }

  /**
   * clear the slots
   */
  public void clear()
  {
    synchronized (_slotMap)
    {
      _slotMap.clear();
    }
  }
}
