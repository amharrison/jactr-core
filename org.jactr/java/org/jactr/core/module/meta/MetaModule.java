package org.jactr.core.module.meta;

import java.util.Collection;
import java.util.Collections;

import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.module.AbstractModule;
import org.jactr.core.module.meta.buffer.MetaBuffer;

public class MetaModule extends AbstractModule
{

  private MetaBuffer _buffer;

  private IChunkType _conditionType;

  private IChunk     _equals, _lessThan, _lessThanEquals, _greaterThan,
      _greaterThanEquals, _notEquals;

  public MetaModule()
  {
    super("meta");
  }

  @Override
  public void reset()
  {
    _buffer.clear();

  }

  @Override
  public void initialize()
  {

  }

  protected IChunk getNamedChunk(String name)
  {
    try
    {
      return getModel().getDeclarativeModule().getChunk(name).get();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public IChunk getEquals()
  {
    if (_equals == null) _equals = getNamedChunk("equals");
    return _equals;
  }

  public IChunk getNotEquals()
  {
    if (_notEquals == null) _notEquals = getNamedChunk("not-equals");
    return _notEquals;
  }

  public IChunk getLessThan()
  {
    if (_lessThan == null) _lessThan = getNamedChunk("less-than");
    return _lessThan;
  }

  public IChunk getLessThanEquals()
  {
    if (_lessThanEquals == null)
      _lessThanEquals = getNamedChunk("less-than-equals");
    return _lessThanEquals;
  }

  public IChunk getGreaterThan()
  {
    if (_greaterThan == null) _greaterThan = getNamedChunk("greater-than");
    return _greaterThan;
  }

  public IChunk getGreaterThanEquals()
  {
    if (_greaterThanEquals == null)
      _greaterThanEquals = getNamedChunk("greater-than-equals");
    return _greaterThanEquals;
  }

  public IChunkType getConditionType()
  {
    if (_conditionType == null) try
    {
      _conditionType = getModel().getDeclarativeModule()
          .getChunkType("condition").get();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
    return _conditionType;
  }

  @Override
  protected Collection<IActivationBuffer> createBuffers()
  {
    _buffer = new MetaBuffer(this);
    return Collections.singleton((IActivationBuffer) _buffer);
  }
}
