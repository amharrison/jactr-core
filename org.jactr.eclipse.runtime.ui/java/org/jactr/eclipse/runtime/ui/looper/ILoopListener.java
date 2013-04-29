package org.jactr.eclipse.runtime.ui.looper;

/*
 * default logging
 */
import java.util.List;

import org.jactr.eclipse.runtime.launching.norm.ACTRSession;

public interface ILoopListener
{

  public void loopDetected(ACTRSession session, String modelName,
      List<String> productionLoop,
      int interations);
}
