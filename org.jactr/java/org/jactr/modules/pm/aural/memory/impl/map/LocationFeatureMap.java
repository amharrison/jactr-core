package org.jactr.modules.pm.aural.memory.impl.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.modalities.vocal.VocalConstants;
import org.commonreality.object.IAfferentObject;
import org.commonreality.object.UnknownPropertyNameException;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.runtime.ACTRRuntime;
import org.jactr.core.slot.IConditionalSlot;
import org.jactr.modules.pm.aural.IAuralModule;
import org.jactr.modules.pm.common.memory.IPerceptualMemory;
import org.slf4j.LoggerFactory;

/**
 * used boolean to store externality (true) or internal (false)
 * 
 * @author harrison
 */
public class LocationFeatureMap extends AbstractAuralFeatureMap<IChunk>
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER         = LoggerFactory
      .getLogger(LocationFeatureMap.class);

  private IIdentifier                             _agentId;

  private IChunk                                  _internal, _external;

  private Map<IChunk, Collection<IIdentifier>>    _values        = new HashMap<IChunk, Collection<IIdentifier>>();

  private Map<IIdentifier, IChunk>                _inverseValues = new HashMap<>();

  public LocationFeatureMap()
  {
    super(IAuralModule.LOCATION_SLOT, null);
  }

  @Override
  public void setPerceptualMemory(IPerceptualMemory perceptualMemory)
  {
    super.setPerceptualMemory(perceptualMemory);

    _internal = ((IAuralModule) perceptualMemory.getModule())
        .getInternalChunk();
    _external = ((IAuralModule) perceptualMemory.getModule())
        .getExternalChunk();

    clearInternal();
  }

  private IIdentifier getAgentId()
  {
    if (_agentId == null) _agentId = ACTRRuntime.getRuntime().getConnector()
        .getAgent(getPerceptualMemory().getModule().getModel()).getIdentifier();
    return _agentId;
  }

  @Override
  protected IChunk extractInformation(IAfferentObject afferentObject)
  {
    try
    {
      IIdentifier id = (IIdentifier) afferentObject
          .getProperty(VocalConstants.SPEAKER);
      if (getAgentId().equals(id)) return _internal;
      return _external;
    }
    catch (UnknownPropertyNameException e)
    {
      if (LOGGER.isDebugEnabled()) LOGGER.debug(String.format(
          "Could not extract speaker, assuming external source, returning true"));
      return _external;
    }
  }

  @Override
  public void normalizeRequest(ChunkTypeRequest request)
  {
    // noop
  }

  @Override
  protected void clearInternal()
  {
    _values.clear();
    _inverseValues.clear();
    _values.put(_external, new ArrayList<>());
    _values.put(_internal, new ArrayList<>());
  }

  @Override
  protected IChunk getCurrentValue(IIdentifier identifier)
  {
    return _inverseValues.get(identifier);
  }

  @Override
  protected IChunk removeInformation(IIdentifier identifier)
  {
    return _inverseValues.remove(identifier);
  }

  @Override
  protected void addInformation(IIdentifier identifier, IChunk data)
  {
    _inverseValues.put(identifier, data);
    _values.get(data).add(identifier);
  }

  @Override
  protected void getCandidates(ChunkTypeRequest request,
      Set<IIdentifier> results)
  {
    boolean firstInsertion = true;
    String slotName = getRelevantSlotName();
    Set<IIdentifier> tmp = new HashSet<>();
    for (IConditionalSlot slot : request.getConditionalSlots())
      if (slot.getName().equalsIgnoreCase(slotName))
      {
        tmp.clear();
        IChunk value = (IChunk) slot.getValue();
        switch (slot.getCondition())
        {
          case IConditionalSlot.NOT_EQUALS:
            not(value, tmp);
            break;
          default:
            equals(value, tmp);
            break;
        }

        if (firstInsertion)
          results.addAll(tmp);
        else
          results.retainAll(tmp);

        firstInsertion = false;

        if (results.size() == 0)
        {
          if (LOGGER.isDebugEnabled())
            LOGGER.debug(this + " No possible results, returning early");
          break;
        }
      }

    
  }

  protected void equals(IChunk value, Set<IIdentifier> container)
  {
    Collection<IIdentifier> ids = _values.get(value);
    if (ids != null) container.addAll(ids);
  }

  protected void not(IChunk value, Set<IIdentifier> container)
  {
    for (IChunk key : _values.keySet())
      if (!key.equals(value))
      {
        Collection<IIdentifier> ids = _values.get(key);
        if (ids != null) container.addAll(ids);
      }
  }
}
