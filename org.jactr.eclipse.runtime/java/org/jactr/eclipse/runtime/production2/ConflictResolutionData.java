package org.jactr.eclipse.runtime.production2;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConflictResolutionData
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(ConflictResolutionData.class);

  private final double               _time;

  private Collection<CommonTree>     _instantiations;

  public ConflictResolutionData(double time,
      Collection<CommonTree> instantiations)
  {
    _time = time;
    _instantiations = new ArrayList<CommonTree>(instantiations);
  }

  public double getTime()
  {
    return _time;
  }

  public Collection<CommonTree> getConflictSet()
  {
    return _instantiations;
  }
}
