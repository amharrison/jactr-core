package org.jactr.tools.tracer.transformer.chunk;

/*
 * default logging
 */
import org.antlr.runtime.tree.CommonTree;
 
import org.slf4j.LoggerFactory;
import org.jactr.core.chunk.event.ChunkEvent;
import org.jactr.core.chunk.event.ChunkEvent.Type;
import org.jactr.tools.tracer.transformer.AbstractTransformedEvent;

public class TransformedChunkEvent extends AbstractTransformedEvent
{
  /**
   * 
   */
  private static final long serialVersionUID = -7186865231807901908L;
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(TransformedChunkEvent.class);
  
  private ChunkEvent.Type _type;

  public TransformedChunkEvent(String modelName, String source,
      long systemTime, double simulationTime, Type type, CommonTree ast)
  {
    super(modelName, source, systemTime, simulationTime, ast);
    
    _type = type;
  }

  public ChunkEvent.Type getType()
  {
    return _type;
  }
}
