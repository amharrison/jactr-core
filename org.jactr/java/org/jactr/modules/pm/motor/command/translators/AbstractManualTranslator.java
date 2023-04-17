package org.jactr.modules.pm.motor.command.translators;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;

import org.commonreality.modalities.motor.MotorUtilities;
import org.commonreality.object.IEfferentObject;
import org.jactr.core.chunk.IChunk;
import org.jactr.core.chunktype.IChunkType;
import org.jactr.core.model.IModel;
import org.jactr.core.production.request.ChunkTypeRequest;
import org.jactr.core.slot.DefaultMutableSlot;
import org.jactr.core.slot.IMutableSlot;
import org.jactr.core.slot.ISlot;
import org.jactr.modules.pm.motor.IMotorModule;
import org.slf4j.LoggerFactory;

public abstract class AbstractManualTranslator extends AbstractTranslator
{
  static public final String                PECK_FITTS_COEFFICIENT = "PeckFittsCoefficient";

  static public final String                MINIMUM_FITTS_TIME     = "MinimumFittsTime";

  static public final String                MINIMUM_MOVEMENT_TIME  = "MinimumMovementTime";

  static private final double               LOG_2                  = Math
                                                                       .log(2);

  /**
   * Logger definition
   */
  static final transient org.slf4j.Logger LOGGER                 = LoggerFactory
                                                                       .getLogger(AbstractManualTranslator.class);

  private double                            _motorBurstTime        = Double.NaN;

  private double                            _minimumFittsTime      = Double.NaN;

  private double                            _peckFittsCoeff        = Double.NaN;
  
  protected Collection<ISlot>             _recycledSlotContainer = new ArrayList<ISlot>(
      4);

  public AbstractManualTranslator()
  {
    
  }

  protected boolean handles(String commandChunkType, ChunkTypeRequest request)
  {
    try
    {
      IChunkType actual = request.getChunkType();
      IChunkType handToHome = actual.getModel().getDeclarativeModule()
          .getChunkType(commandChunkType).get();

      return actual.equals(handToHome);
    }
    catch (Exception e)
    {
      /**
       * Error :
       */
      LOGGER.error("Failed to get " + commandChunkType + " chunk type ", e);
      return false;
    }
  }

  /**
   * translates a set of slot values into a {@link IEfferentObject} that
   * represents a muscle defined within the pattern. Since many ACT-R movement
   * commands use multiple slots to define a muscle, this collapses them. (i.e.
   * translates hand right finger index into right-index). In addition to the
   * returned {@link IEfferentObject} this method should also ensure that the
   * slots used to define the muscle are nulled out and the muscle slot is
   * specified.
   * 
   * @param request
   * @param model
   * @return
   * @throws IllegalArgumentException
   */
  public IEfferentObject getMuscle(ChunkTypeRequest request, IModel model)
      throws IllegalArgumentException
  {
    /*
     * special processing to handle finger,hand,device
     */
    IChunk hand = null;
    IChunk finger = null;
    IChunk device = null;
    ISlot muscleSlot = null;
    
    _recycledSlotContainer.clear();
    request.getChunkType().getSymbolicChunkType()
        .getSlots(_recycledSlotContainer); // grab the defaults
    _recycledSlotContainer = request.getSlots(_recycledSlotContainer);

    for (ISlot slot : _recycledSlotContainer)
    {
      String slotName = slot.getName();
      if (slotName.equalsIgnoreCase("hand"))
        hand = (IChunk) slot.getValue();
      else if (slotName.equalsIgnoreCase("finger"))
        finger = (IChunk) slot.getValue();
      else if (slotName.equalsIgnoreCase("device"))
        device = (IChunk) slot.getValue();
      else if (slotName.equalsIgnoreCase("muscle")) muscleSlot = slot;
    }

    String muscleName = null;
    if (hand != null)
    {
      muscleName = hand.getSymbolicChunk().getName();

      if (finger != null)
        muscleName += "-" + finger.getSymbolicChunk().getName();
    }
    else if (device != null)
      muscleName = device.getSymbolicChunk().getName();
    else if (muscleSlot != null) muscleName = "" + muscleSlot.getValue();

    IEfferentObject muscle = getMuscle(muscleName, model);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("muscle named : " + muscleName
          + " matches to efferent object " + muscle.getIdentifier());

    if (muscle == null)
    {
      muscleName = null;
      if (LOGGER.isWarnEnabled())
        LOGGER.warn("No muscle found, available : " + getCachedMuscleNames());
    }

    /**
     * finally we set the muscle slot value since that is what we will actually
     * be using further down stream in the processing
     */
    if (muscleSlot == null)
    {
      muscleSlot = new DefaultMutableSlot("muscle", muscleName);
      request.addSlot(muscleSlot);
    }
    else
      ((IMutableSlot) muscleSlot).setValue(muscleName);

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Final movement pattern : " + request);

    return muscle;
  }

  protected double getMotorBurstTime(IMotorModule module)
  {
    if (Double.isNaN(_motorBurstTime))
      try
      {
        _motorBurstTime = Double.parseDouble(module
            .getParameter(MINIMUM_MOVEMENT_TIME));
      }
      catch (Exception e)
      {
        _motorBurstTime = 0.05;
      }
    return _motorBurstTime;
  }

  protected double getMinimumFittsTime(IMotorModule module)
  {
    if (Double.isNaN(_minimumFittsTime))
      try
      {
        _minimumFittsTime = Double.parseDouble(module
            .getParameter(MINIMUM_FITTS_TIME));
      }
      catch (Exception e)
      {
        _minimumFittsTime = 0.1;
      }
    return _minimumFittsTime;
  }

  protected double getPeckFittsCoefficient(IMotorModule module)
  {
    if (Double.isNaN(_peckFittsCoeff))
      try
      {
        _peckFittsCoeff = Double.parseDouble(module
            .getParameter(PECK_FITTS_COEFFICIENT));
      }
      catch (Exception e)
      {
        _peckFittsCoeff = 0.075;
      }
    return _peckFittsCoeff;
  }

  protected double computeFitts(double fittsCoeff, double distance, double width)
  {
    return fittsCoeff * Math.log(distance / width + 0.5) / LOG_2;
  }

  protected double[] computeRate(double[] origin, double[] target,
      double duration)
  {
    double[] rate = new double[origin.length];
    for (int i = 0; i < rate.length; i++)
    {
      rate[i] = (target[i] - origin[i]) / duration;
      if (Double.isNaN(rate[i])) rate[i] = 0;
    }
    return rate;
  }

  protected double computeDistance(double[] origin, double[] target)
  {
    double rtn = 0;
    for (int i = 0; i < origin.length; i++)
      rtn += Math.abs(origin[i] - target[i]) * Math.abs(origin[i] - target[i]);

    return Math.sqrt(rtn);
  }

  protected boolean rightHandIsOnHome(IModel model)
  {
    IEfferentObject rightHand = getMuscle("right", model);
    double[] position = MotorUtilities.getPosition(rightHand);
    double tolerance = 0.1;
    // 7,4 is 'J'
    return tolerance >= Math.abs(position[0] - 7)
        && tolerance >= Math.abs(position[1] - 4);
  }

  protected boolean rightHandIsOnMouse(IModel model)
  {
    IEfferentObject rightHand = getMuscle("right", model);
    double[] position = MotorUtilities.getPosition(rightHand);
    double tolerance = 0.1;
    // 28,2 is left mouse button
    return tolerance >= Math.abs(position[0] - 28)
        && tolerance >= Math.abs(position[1] - 2);
  }
}