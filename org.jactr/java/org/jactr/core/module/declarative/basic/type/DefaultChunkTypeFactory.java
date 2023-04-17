package org.jactr.core.module.declarative.basic.type;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.chunktype.ISubsymbolicChunkType;
import org.jactr.core.chunktype.ISymbolicChunkType;
import org.jactr.core.chunktype.basic.DefaultChunkType;
import org.jactr.core.model.IModel;

public class DefaultChunkTypeFactory implements IChunkTypeFactory
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(DefaultChunkTypeFactory.class);

  public IChunkType newChunkType(IModel model)
  {
    return new DefaultChunkType(model);
  }

  public void bind(IChunkType type, ISymbolicChunkType symbolic,
      ISubsymbolicChunkType subsymbolic)
  {
    DefaultChunkType ct = (DefaultChunkType) type;

    ct.bind(symbolic, subsymbolic);
  }

  public void unbind(IChunkType type, ISymbolicChunkType symbolic,
      ISubsymbolicChunkType subsymbolic)
  {
    DefaultChunkType ct = (DefaultChunkType) type;
    ct.bind(null, null);

  }

  public void merge(IChunkType master, IChunkType mergie)
  {
    // noop

  }

  public void dispose(IChunkType type)
  {
    type.dispose();
  }

}
