package org.commonreality.sensors.handlers;

/*
 * default logging
 */
import org.commonreality.efferent.ICompoundCommand;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.delta.DeltaTracker;

public interface ICommandTimingEquation
{

  /**
   * compute how long it will take to execute a command. If
   * the command is an {@link ICompoundCommand}, then the timings
   * and relative start times for the components should be updated as well 
   * @param command
   *
   * @return duration of the command
   */
  public double computeTimings(DeltaTracker<IMutableObject> command);
}
