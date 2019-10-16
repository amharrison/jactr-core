package org.jactr.core.module.meta;

import java.util.Collection;
import java.util.Collections;

import org.jactr.core.buffer.IActivationBuffer;
import org.jactr.core.module.AbstractModule;
import org.jactr.core.module.meta.buffer.MetaBuffer;

public class MetaModule extends AbstractModule
{

  public MetaModule()
  {
    super("meta");
  }

  @Override
  public void reset()
  {


  }

  @Override
  public void initialize()
  {


  }

  @Override
  protected Collection<IActivationBuffer> createBuffers()
  {
    return Collections.singleton((IActivationBuffer) new MetaBuffer(this));
  }
}
