package org.jactr.tools.tracer.transformer.buffer;

/*
 * default logging
 */
import org.antlr.runtime.tree.CommonTree;
 
import org.slf4j.LoggerFactory;
import org.jactr.tools.tracer.transformer.AbstractTransformedEvent;

public class BulkBufferEvent extends AbstractTransformedEvent
{
  /**
   * 
   */
  private static final long          serialVersionUID = 802821762699655390L;

  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(BulkBufferEvent.class);

  private boolean                    _isPostConflict  = false;

  public BulkBufferEvent(String modelName, double simulationTime,
      CommonTree ast, boolean postConflict)
  {
    super(modelName, modelName, System.currentTimeMillis(), simulationTime, ast);
    _isPostConflict = postConflict;
  }

  public boolean isPostConflictResolution()
  {
    return _isPostConflict;
  }

}
