package org.jactr.eclipse.ui.concurrent;

/*
 * default logging
 */
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.ui.UIPlugin;

public class SWTExecutor implements Executor
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SWTExecutor.class);

  public void execute(Runnable arg0)
  {
    UIPlugin.getStandardDisplay().asyncExec(arg0);
  }

}
