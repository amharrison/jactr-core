package org.jactr.eclipse.runtime.ui.looper;

/*
 * default logging
 */
import java.util.List;

import org.jactr.eclipse.runtime.session.ISession;

public interface ILoopListener
{

  public void loopDetected(ISession session, String modelName,
      List<String> productionLoop,
      int interations);
}
