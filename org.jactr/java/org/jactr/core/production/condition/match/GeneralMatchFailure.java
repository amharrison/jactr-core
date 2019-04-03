package org.jactr.core.production.condition.match;

/*
 * default logging
 */
 
import org.slf4j.LoggerFactory;
import org.jactr.core.production.condition.ICondition;

/**
 * lazy catch all explanation of a match failure. Typically this is only used
 * for one-offs, such as a query of a buffer that has no status slots or a
 * missing variable for a variable condition
 * 
 * @author harrison
 */
public class GeneralMatchFailure extends AbstractMatchFailure
{

  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(GeneralMatchFailure.class);

  private final String               _message;

  public GeneralMatchFailure(ICondition condition, String message)
  {
    super(condition);
    _message = message;
  }

  @Override
  public String toString()
  {
    return _message;
  }
}
