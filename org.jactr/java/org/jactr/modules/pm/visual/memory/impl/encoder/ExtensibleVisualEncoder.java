package org.jactr.modules.pm.visual.memory.impl.encoder;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.commonreality.object.IAfferentObject;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.SortedMaps;
import org.eclipse.collections.impl.tuple.Tuples;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunk.ISymbolicChunk;
import org.jactr.core.module.declarative.IDeclarativeModule;
import org.jactr.core.slot.IMutableSlot;
import org.jactr.modules.pm.common.memory.IPerceptualMemory;
import org.jactr.modules.pm.visual.memory.IVisualMemory;

/**
 * More versitle starting point for generalized visual encoders. The constructor
 * provides the name of the chunktype to instantiate for the visual object and
 * the common reality visual.type name to match against. In other words, When a
 * visual object is detected, if its type is consistent, a chunk of chunktype is
 * created.</br>
 * You can also configure arbitrary handlers for property values
 * ({@link #addFeatureHandler(String, String, Function)}). If the visual object
 * has the common reality property, the provided function is used to get the
 * value that is assigned to the named slot.</br>
 * Finally, the common case of using marker chunks (named chunks of type chunk)
 * that map directly to string labels is supported with
 * {@link #addCachedHandler(String, String, String...)}. Specifically, if it has
 * a string common reality property the chunk matching that name is assigned to
 * the named slot value.
 * 
 * @author harrison
 */
public class ExtensibleVisualEncoder extends AbstractVisualEncoder
{

  private boolean                                                      _firstEncode     = true;

  private Collection<String[]>                                         _namesToCache    = Lists.mutable
      .empty();

  private Map<String, IChunk>                                          _namedCache      = SortedMaps.mutable
      .empty();

  private Map<Pair<String, String>, Function<IAfferentObject, Object>> _featureHandlers = Maps.mutable
      .empty();

  private String                                                       _crTypeName;

  /**
   * @param chunkTypeName
   *          the name of the chunktype to instantiate for this visual object
   * @param crTypeName
   *          the value of the visual.type field to match
   */
  public ExtensibleVisualEncoder(String chunkTypeName, String crTypeName)
  {
    super(chunkTypeName);
    _crTypeName = crTypeName;
  }

  public String getCommonRealityTypeName()
  {
    return _crTypeName;
  }

  public void addFeatureHandler(String crFeature, String slotName,
      Function<IAfferentObject, Object> mapping)
  {
    _featureHandlers.put(Tuples.pair(crFeature, slotName), mapping);
  }

  public void addFeatureHandler(String crFeature, String slotName)
  {
    addFeatureHandler(crFeature, slotName, (aff) -> {
      return aff.getProperty(crFeature);
    });
  }

  /**
   * crFeature represents a 1:1 name match to named chunks (common for marking
   * chunks).
   * 
   * @param crFeature
   * @param slotName
   * @param cachedValues
   *          names of the chunks/feature values we are expecting. These chunks
   *          are assumed to already exist in the model.
   */
  public void addCachedHandler(String crFeature, String slotName,
      String... cachedValues)
  {
    addFeatureHandler(crFeature, slotName, (aff) -> {
      return _namedCache.get(getHandler().getString(crFeature, aff));
    });
    _namesToCache.add(cachedValues);
  }

  @Override
  protected boolean canEncodeVisualObjectType(IAfferentObject afferentObject)
  {
    try
    {
      String[] types = getHandler().getTypes(afferentObject);
      for (String type : types)
        if (type.equals(_crTypeName)) return true;
      return false;
    }
    catch (Exception e)
    {
      return false;
    }
  }

  @Override
  public boolean isDirty(IAfferentObject afferentObject, IChunk oldChunk,
      IPerceptualMemory memory)
  {
    return super.isDirty(afferentObject, oldChunk, memory)
        || handlersIsDirty(afferentObject, oldChunk, memory);
  }

  private boolean handlersIsDirty(IAfferentObject afferentObject,
      IChunk oldChunk, IPerceptualMemory memory)
  {
    for (Pair<String, String> key : _featureHandlers.keySet())
    {
      /*
       * first is common reality feature name, second is slot name.
       */
      Object slotValue = oldChunk.getSymbolicChunk().getSlot(key.getTwo())
          .getValue();
      Object resolvedCRValue = _featureHandlers.get(key).apply(afferentObject);
      if (!Objects.equals(slotValue, resolvedCRValue)) return true;
    }
    return false;
  }

  @Override
  protected void updateSlots(IAfferentObject afferentObject, IChunk encoding,
      IVisualMemory memory)
  {
    if (_firstEncode) grabReferences(memory);

    super.updateSlots(afferentObject, encoding, memory);

    ISymbolicChunk sc = encoding.getSymbolicChunk();
    for (Map.Entry<Pair<String, String>, Function<IAfferentObject, Object>> entry : _featureHandlers
        .entrySet())
    {
      String featureName = entry.getKey().getOne();
      String slotName = entry.getKey().getTwo();

      if (getHandler().hasProperty(featureName, afferentObject)) try
      {
        Object value = entry.getValue().apply(afferentObject);

        ((IMutableSlot) sc.getSlot(slotName)).setValue(value);
      }
      catch (Exception e)
      {
        e.printStackTrace(System.err);
      }
    }
  }

  private void grabReferences(IVisualMemory memory)
  {
    IDeclarativeModule decM = memory.getModule().getModel()
        .getDeclarativeModule();

    for (String[] names : _namesToCache)
      for (String name : names)
        decM.getChunk(name.toLowerCase()).thenAccept((c) -> {
          _namedCache.put(name.toLowerCase(), c);
        });

    _namesToCache.clear();
    _firstEncode = false;
  }
}
