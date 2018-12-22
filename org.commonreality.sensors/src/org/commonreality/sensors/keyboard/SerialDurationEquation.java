package org.commonreality.sensors.keyboard;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.modalities.motor.MovementCommand;
import org.commonreality.object.IMutableObject;
import org.commonreality.object.delta.DeltaTracker;
import org.commonreality.sensors.handlers.ICommandTimingEquation;

public class SerialDurationEquation implements ICommandTimingEquation
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SerialDurationEquation.class);

  public double computeTimings(DeltaTracker<IMutableObject> command)
  {
    double duration = computeTimings((IEfferentCommand) command.get(),
        ((IEfferentCommand) command.get()).getRequestedStartTime());
    
    command.setProperty(IEfferentCommand.ESTIMATED_DURATION, duration);
    return duration;
  }

  public double computeTimings(IEfferentCommand command, double startTime)
  {
    if (command instanceof MovementCommand)
    {
      MovementCommand movement = (MovementCommand) command;
      double duration = 0;

      if (movement.isCompound())
      {
        double lastStart = startTime;
        for (IEfferentCommand com : movement.getComponents())
        {
          double comDuration = computeTimings(com, lastStart);
          /*
           * component commands aren't delta tracked, so we can just change them directly..
           */
          ((IMutableObject)com).setProperty(IEfferentCommand.REQUESTED_START_TIME, lastStart);
          ((IMutableObject)com).setProperty(IEfferentCommand.ESTIMATED_DURATION, comDuration);
          duration += comDuration;
          lastStart += comDuration;
        }
      }
      else
      {
        double[] origin = movement.getOrigin();
        double[] target = movement.getTarget();
        double[] rate = movement.getRate();

        for (int i = 0; i < rate.length; i++)
        {
          double tmpDuration = Math.abs((target[i] - origin[i]) / rate[i]);

          if (LOGGER.isDebugEnabled())
            LOGGER.debug("o: " + origin[i] + " t: " + target[i] + " r: "
                + rate[i] + " duration: " + tmpDuration );

          if (!Double.isNaN(tmpDuration))
            duration = Math.max(duration, tmpDuration);
        }
      }

      if (LOGGER.isDebugEnabled())
        LOGGER.debug(movement + " starts: "+startTime+" for: "+duration);

      return duration;
    }

    throw new RuntimeException("Can only process movement commands");
  }

}
