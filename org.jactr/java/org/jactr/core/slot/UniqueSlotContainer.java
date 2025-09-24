package org.jactr.core.slot;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

public class UniqueSlotContainer implements IUniqueSlotContainer
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER   = LoggerFactory
      .getLogger(UniqueSlotContainer.class);

  final private Map<String, ISlot>                _slotMap = new TreeMap<String, ISlot>();

  final protected boolean                         _useMutable;

  private boolean                                 _encoded = false;

  public UniqueSlotContainer()
  {
    this(false);
  }

  public UniqueSlotContainer(boolean useMutableSlots)
  {
    _useMutable = useMutableSlots;
  }

  public ISlot getSlot(String slotName)
  {
    return _slotMap.get(slotName);
  }

  protected ISlot createSlot(ISlot slot)
  {
    if (_useMutable)
      return new DefaultMutableSlot(slot.getName(), slot.getValue());

    return new BasicSlot(slot.getName(), slot.getValue());
  }

  public void addSlot(ISlot slot)
  {
    if (!_encoded) _slotMap.put(slot.getName(), createSlot(slot));
  }

  public Collection<? extends ISlot> getSlots()
  {
    return getSlots(null);
  }

  public Collection<ISlot> getSlots(Collection<ISlot> slots)
  {
    if (slots == null)
      slots = new ArrayList<ISlot>(_slotMap.values());
    else
      slots.addAll(_slotMap.values());
    return slots;
  }

  public Collection<IMutableSlot> getMutableSlots()
  {
    if (!_useMutable) return Collections.emptyList();
    if (_encoded) return Collections.emptyList();

    Collection<IMutableSlot> rtn = new ArrayList<IMutableSlot>(_slotMap.size());

    for (ISlot slot : _slotMap.values())
      rtn.add((IMutableSlot) slot);

    return rtn;
  }

  public void removeSlot(ISlot slot)
  {
    if (!_encoded) _slotMap.remove(slot.getName());
  }

  public boolean hasSlot(String slotName)
  {
    return _slotMap.containsKey(slotName);
  }
  
  /**
   * makes readonly and optimizes
   */
  protected void encode() {
    if(_encoded) return;
    
    var slots = getSlots();
    _slotMap.clear();
    
    for(ISlot slot : slots)
    {
      String intern = slot.getName().intern();
      _slotMap.put(intern, new OptimizedSlot(intern, slot.getValue()));
    }
    
    _encoded = true;
  }

  /**
   * clear the slots
   */
  public void clear()
  {
    if (!_encoded) _slotMap.clear();
  }

  /**
   * this is retained for BasicSymbolicChunk down the inheritance line. This
   * exposes the default Object.hashCode
   *
   * @return
   */
//  protected int originalHashCode()
//  {
//    return super.hashCode();
//  }

  @Override
  public int hashCode()
  {
    return Objects.hash(_slotMap, _useMutable, _encoded);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    UniqueSlotContainer other = (UniqueSlotContainer) obj;
    return Objects.equals(_slotMap, other._slotMap)
        && _useMutable == other._useMutable 
        && _encoded == other._encoded;
  }

}
