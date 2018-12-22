package org.commonreality.modalities.motor;

/*
 * default logging
 */
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.efferent.AbstractEfferentCommand;
import org.commonreality.efferent.ICompoundCommand;
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.identifier.IIdentifier;

public class MovementCommand extends AbstractEfferentCommand implements
    ICompoundCommand
{
  static public final String         MOVEMENT_ORIGIN  = "MovementCommand.Origin";

  static public final String         MOVEMENT_TARGET  = "MovementCommand.Target";

  static public final String         MOVEMENT_RATE    = "MovementCommand.Rate";

  /**
   * 
   */
  private static final long          serialVersionUID = -3419302731749071125L;

  /**
   * Logger definition
   */
  static private final transient Log LOGGER           = LogFactory
                                                          .getLog(MovementCommand.class);

  public MovementCommand(IIdentifier identifier)
  {
    super(identifier);
  }

  public MovementCommand(IIdentifier identifier, IIdentifier efferentId)
  {
    this(identifier);
    setEfferentIdentifier(efferentId);
  }

  public double[] getRate()
  {
    try
    {
      return MotorUtilities.getDoubles(MOVEMENT_RATE, this);
    }
    catch (Exception e)
    {
      return new double[0];
    }
  }

  public double[] getTarget()
  {
    try
    {
      return MotorUtilities.getDoubles(MOVEMENT_TARGET, this);
    }
    catch (Exception e)
    {
      return new double[0];
    }
  }

  public double[] getOrigin()
  {
    try
    {
      return MotorUtilities.getDoubles(MOVEMENT_ORIGIN, this);
    }
    catch (Exception e)
    {
      return new double[0];
    }
  }

  public Collection<IEfferentCommand> getComponents()
  {
    try
    {
      return (Collection<IEfferentCommand>) getProperty(COMPONENTS);
    }
    catch (Exception e)
    {
      return Collections.EMPTY_LIST;
    }
  }

  public void add(IEfferentCommand command)
  {
    Collection<IEfferentCommand> composite = getComponents();
    if (composite.size() == 0)
    {
      composite = new ArrayList<IEfferentCommand>();
      setProperty(COMPONENTS, composite);
      setProperty(IS_COMPOUND, Boolean.TRUE);
    }

    composite.add(command);
  }

  public boolean isCompound()
  {
    try
    {
      return (Boolean) getProperty(IS_COMPOUND);
    }
    catch (Exception e)
    {
      return false;
    }
  }

}
