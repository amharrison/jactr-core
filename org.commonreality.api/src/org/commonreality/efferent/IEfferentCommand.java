package org.commonreality.efferent;

import java.io.Serializable;

import org.commonreality.identifier.IIdentifier;
import org.commonreality.object.IEfferentObject;
import org.commonreality.object.ISensoryObject;
import org.commonreality.object.identifier.ISensoryIdentifier;

/*
 * default logging
 */


/**
 * a command that can be issued to affect some change on an {@link IEfferentObject}.
 * It must implement a single arg constructor with {@link IIdentifier} as the identifier
 * of the command. This is so that they can be instantiated on remote sides correctly.
 * @author harrison
 *
 */
public interface IEfferentCommand extends ISensoryObject, Serializable
{
  public static enum RequestedState {SUBMIT, START, ABORT };
  
  public static enum ActualState {UNKNOWN, ACCEPTED, REJECTED, RUNNING, COMPLETED, ABORTED};
  
  
  public static final String REQUESTED_STATE = "IEfferentCommand.requestedState";
  public static final String ACTUAL_STATE = "IEfferentCommand.actualState";
  
  
  public static final String RESULT = "IEfferentCommand.result";
  public static final String EFFERENT_ID = "IEfferentCommand.efferentIdentifier";
  public static final String COMMAND_CLASS_NAME = "IEfferentCommand.className";
  public static final String ESTIMATED_DURATION = "IEfferentCommand.estimatedDuration";
  public static final String REQUESTED_START_TIME = "IEfferentCommand.requestedStartTime";

  public static final String IS_ADJUSTABLE        = "IEfferentCommand.isAdjustable";
  

  /**
   * the efferent object that this command is operating on
   * @return
   */
  public ISensoryIdentifier getEfferentIdentifier();
  
  public RequestedState getRequestedState();
  
  public ActualState getActualState();
  
  /**
   * what happend to interrupt the command or some outcome
   * of the command
   * @return
   */
  public Object getResult();
  
  public double getEstimatedDuration();
  
  public double getRequestedStartTime();
  
  /**
   * will block until the state is not waitWhileState
   * @param waitWhileState
   * @return
   * @throws InterruptedException
   */
  public ActualState waitForActualStateChange(ActualState waitWhileState) throws InterruptedException;
  
  /**
   * adjustable commands can be modified by the issuer after it has already
   * started.
   * 
   * @return
   */
  public boolean isAdjustable();
 
}
