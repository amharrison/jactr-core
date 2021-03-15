package org.jactr.modules.pm.visual.memory.impl.filter;

import java.util.Collection;
import java.util.Collections;
/*
 * default logging
 */
import java.util.Comparator;

import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.slot.IConditionalSlot;
import org.jactr.modules.pm.common.memory.filter.AbstractIndexFilter;
import org.jactr.modules.pm.common.memory.filter.IIndexFilter;
import org.jactr.modules.pm.visual.IVisualModule;
import org.slf4j.LoggerFactory;

public class AttendedVisualLocationFilter extends
    AbstractIndexFilter<Boolean>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(AttendedVisualLocationFilter.class);

  @Override
  protected Boolean compute(ChunkTypeRequest request)
  {
    return null;
  }

  /**
   * if we've gotten this far, then always accept
   * @param template
   * @return
   * @see org.jactr.modules.pm.common.memory.filter.IIndexFilter#accept(org.jactr.core.production.request.ChunkTypeRequest)
   */
  public boolean accept(ChunkTypeRequest template)
  {
    return true;
  }

  /**
   * no comparator is used (how would you sort new, old, attended ?)
   * @return
   * @see org.jactr.modules.pm.common.memory.filter.IIndexFilter#getComparator()
   */
  public Comparator<ChunkTypeRequest> getComparator()
  {
    return null;
  }

  public Collection<IIndexFilter> instantiate(ChunkTypeRequest request)
  {
    /*
     * no need for an instantiated copy
     */
    return Collections.singleton((IIndexFilter) this);
  }

  @Override
  public void normalizeRequest(ChunkTypeRequest request)
  {
    /**
     * +visual-location> isa visual-location
     *  :attended null is equivalent to :attended != true
     */
    for (IConditionalSlot cSlot : request.getConditionalSlots())
      if (cSlot.getName().equals(IVisualModule.ATTENDED_STATUS_SLOT)
          && cSlot.getCondition() == IConditionalSlot.EQUALS
          && cSlot.getValue() == null)
      {
        cSlot.setValue(Boolean.TRUE);
        cSlot.setCondition(IConditionalSlot.NOT_EQUALS);
      }
  }
}
