package org.jactr.core.concurrent;

/*
 * default logging
 */
import java.util.concurrent.Executor;

 
import org.slf4j.LoggerFactory;
import org.jactr.core.model.ICycleProcessor;
import org.jactr.core.model.IModel;

/**
 * model cycle executor allows you to execute runnable code either before or
 * after any cycle.
 * 
 * @author harrison
 */
public class ModelCycleExecutor implements Executor
{
  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(ModelCycleExecutor.class);

  static public enum When {
    BEFORE, AFTER, ASAP
  };

  final private IModel _model;

  final private When   _when;

  public ModelCycleExecutor(IModel model, When when)
  {
    _model = model;
    _when = when;
  }

  /**
   * will call executeNow
   */
  public void execute(Runnable command)
  {
    switch (_when)
    {
      case BEFORE:
        executeBeforeCycle(command);
        break;
      case AFTER:
        executeAfterCycle(command);
        break;
      case ASAP:
        executeASAP(command);
        break;
    }
  }

  /**
   * attempts to queue this command to execute ASAP - if the model is mid-cycle,
   * it will queue to execute after the cycle, otherwise, it will queue to
   * execute before the next cycle.
   * 
   * @param command
   */
  public void executeASAP(Runnable command)
  {
    ICycleProcessor cycleProc = _model.getCycleProcessor();
    if (cycleProc.isExecuting())
      cycleProc.executeAfter(command);
    else
      cycleProc.executeBefore(command);
  }

  public void executeBeforeCycle(Runnable command)
  {
    _model.getCycleProcessor().executeBefore(command);
  }

  public void executeAfterCycle(Runnable command)
  {
    _model.getCycleProcessor().executeAfter(command);
  }
}
