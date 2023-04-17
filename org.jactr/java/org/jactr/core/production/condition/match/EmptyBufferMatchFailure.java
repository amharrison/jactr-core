package org.jactr.core.production.condition.match;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.production.condition.IBufferCondition;

public class EmptyBufferMatchFailure extends AbstractMatchFailure
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(EmptyBufferMatchFailure.class);

  public EmptyBufferMatchFailure(IBufferCondition condition)
  {
    super(condition);
  }

  @Override
  public String toString()
  {
    return String.format("%s is empty, cannot match.",
        ((IBufferCondition) getCondition()).getBufferName());
  }
}
