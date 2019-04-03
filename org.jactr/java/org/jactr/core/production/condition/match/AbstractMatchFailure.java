package org.jactr.core.production.condition.match;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.production.condition.ICondition;

public abstract class AbstractMatchFailure implements IMatchFailure
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(AbstractMatchFailure.class);

  private ICondition                 _condition;

  public AbstractMatchFailure(ICondition condition)
  {
    setCondition(condition);
  }

  public void setCondition(ICondition condition)
  {
    _condition = condition;
  }

  public ICondition getCondition()
  {
    return _condition;
  }

}
