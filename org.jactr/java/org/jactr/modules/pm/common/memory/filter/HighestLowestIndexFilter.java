package org.jactr.modules.pm.common.memory.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.jactr.core.chunk.IChunk;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.slot.IConditionalSlot;

public class HighestLowestIndexFilter extends AbstractIndexFilter<Double>
{

  private IChunk  _highest, _lowest;

  private String  _relevantSlotName;

  private boolean _sortAscending = true;

  public HighestLowestIndexFilter(IChunk highest, IChunk lowest)
  {
    _highest = highest;
    _lowest = lowest;
  }

  public HighestLowestIndexFilter(String slotName, boolean sortAscending)
  {
    _relevantSlotName = slotName;
    _sortAscending = sortAscending;
  }

  @Override
  public Collection<IIndexFilter> instantiate(ChunkTypeRequest request)
  {

    Collection<IIndexFilter> rtn = new ArrayList<>();
    int weight = -1;
    for (IConditionalSlot cSlot : request.getConditionalSlots())
      // we only filter/sort if equality, otherwise it is handled
      // by abstractSortedFeatureMap's handling of highest/lowest
      if (cSlot.getCondition() == IConditionalSlot.EQUALS)
      {
        // highest
        weight++;
        HighestLowestIndexFilter hlif = null;
        if (_highest.equals(cSlot.getValue()))
          hlif = new HighestLowestIndexFilter(cSlot.getName(), false);
        else if (_lowest.equals(cSlot.getValue()))
          hlif = new HighestLowestIndexFilter(cSlot.getName(), true);

        if (hlif != null)
        {
          request.removeSlot(cSlot);
          hlif.setPerceptualMemory(getPerceptualMemory());
          hlif.setWeight(weight);
          rtn.add(hlif);
        }
      }

    return rtn;
  }

  @Override
  public boolean accept(ChunkTypeRequest template)
  {
    return true;
  }

  @Override
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

  @Override
  protected Double compute(ChunkTypeRequest request)
  {

    return (Double) request.getConditionalSlots().stream()
        .filter(c -> c.getName().equals(_relevantSlotName)).findFirst().get()
        .getValue();
  }

  @Override
  public void normalizeRequest(ChunkTypeRequest request)
  {

  }
}
