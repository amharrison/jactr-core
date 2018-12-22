package org.commonreality.efferent;

/*
 * default logging
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.identifier.ISensoryIdentifier;
import org.commonreality.object.manager.impl.BasicObject;

public class AbstractEfferentCommand extends BasicObject implements
    IEfferentCommand
{
  /**
   * 
   */
  private static final long          serialVersionUID = 326165665134897323L;

  /**
   * Logger definition
   */
  static private final transient Log LOGGER           = LogFactory
                                                          .getLog(AbstractEfferentCommand.class);

  public AbstractEfferentCommand(IIdentifier identifier)
  {
    super(identifier);
    setProperty(COMMAND_CLASS_NAME, getClass().getName());
    setProperty(REQUESTED_STATE, RequestedState.SUBMIT);
    setProperty(ACTUAL_STATE, ActualState.UNKNOWN);
    setProperty(ESTIMATED_DURATION, new Double(0));
    setProperty(REQUESTED_START_TIME, new Double(0));
    setProperty(IS_ADJUSTABLE, Boolean.FALSE);
  }
  
  public void setEfferentIdentifier(IIdentifier identifier)
  {
    setProperty(EFFERENT_ID, identifier);
  }
  

  @Override
  public ISensoryIdentifier getIdentifier()
  {
    return (ISensoryIdentifier) super.getIdentifier();
  }

  public ISensoryIdentifier getEfferentIdentifier()
  {
    return (ISensoryIdentifier) getProperty(EFFERENT_ID);
  }

  public Object getResult()
  {
    if (hasProperty(RESULT)) return getProperty(RESULT);

    return null;
  }

  public ActualState getActualState()
  {
    return (ActualState) getProperty(ACTUAL_STATE);
  }

  public RequestedState getRequestedState()
  {
    return (RequestedState) getProperty(REQUESTED_STATE);
  }

  public double getEstimatedDuration()
  {
    return (Double) getProperty(ESTIMATED_DURATION);
  }
  
  public double getRequestedStartTime()
  {
    return (Double) getProperty(REQUESTED_START_TIME);
  }

  @Override
  synchronized public boolean setProperty(String name, Object value)
  {
    boolean rtn = super.setProperty(name, value);

    if (ACTUAL_STATE.equals(name)) notifyAll();

    return rtn;
  }

  synchronized public ActualState waitForActualStateChange(
      ActualState waitWhileState) throws InterruptedException
  {
    while (waitWhileState == getActualState())
      wait();

    return getActualState();
  }

  public boolean isAdjustable()
  {
    return (Boolean) getProperty(IS_ADJUSTABLE);
  }
  
}
