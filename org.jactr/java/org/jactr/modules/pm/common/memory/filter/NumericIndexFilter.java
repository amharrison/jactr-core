package org.jactr.modules.pm.common.memory.filter;

import java.util.Collection;
import java.util.Collections;
/*
 * default logging
 */
import java.util.Comparator;

import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.slot.IConditionalSlot;
import org.slf4j.LoggerFactory;

public class NumericIndexFilter extends AbstractIndexFilter<Double>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(NumericIndexFilter.class);

  private final String               _slotName;

  private final boolean              _sortAscending;

  public NumericIndexFilter(String slotName, boolean ascending)
  {
    _slotName = slotName;
    _sortAscending = ascending;
  }

  @Override
  protected Double compute(ChunkTypeRequest request)
  {
    for (IConditionalSlot cSlot : request.getConditionalSlots())
      if (cSlot.getCondition() == IConditionalSlot.EQUALS
          && _slotName.equals(cSlot.getName()))
      {
        Object value = cSlot.getValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
      }

    return null;
  }

  public boolean accept(ChunkTypeRequest template)
  {
    Double value = get(template);

    return value != null;
  }

  public Comparator<ChunkTypeRequest> getComparator()
  {
    return new Comparator<ChunkTypeRequest>() {
      public int compare(ChunkTypeRequest o1, ChunkTypeRequest o2)
      {
        if (o1 == o2) return 0;

        Double v1 = get(o1);
        Double v2 = get(o2);

        int rtn = 0;

        if (v1 < v2) rtn = -1;
        if (v1 > v2) rtn = 1;

        if (!_sortAscending) rtn *= -1;

        return rtn;
      }
    };
  }

  public Collection<IIndexFilter> instantiate(ChunkTypeRequest request)
  {
    int weight = -1;
    int count = 0;
    for (IConditionalSlot cSlot : request.getConditionalSlots())
    {
      ++count;
      if (_slotName.equals(cSlot.getName()))
      {
        weight = count;
        break;
      }
    }

    if (weight == -1) return Collections.emptyList();

    NumericIndexFilter instance = new NumericIndexFilter(_slotName,
        _sortAscending);
    instance.setWeight(weight);
    instance.setPerceptualMemory(getPerceptualMemory());

    return Collections.singleton((IIndexFilter) instance);
  }

}
