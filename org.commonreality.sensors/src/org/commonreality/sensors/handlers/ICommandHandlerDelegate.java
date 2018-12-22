package org.commonreality.sensors.handlers;

/*
 * default logging
 */
import org.commonreality.efferent.IEfferentCommand;
import org.commonreality.object.IAgentObject;

/**
 * delegatable command handler that processes specific types of
 * IEfferentCommands. This in combination with the
 * {@link EfferentCommandHandler} allows one to more readily extend the range of
 * commands handled
 * 
 * @author harrison
 */
public interface ICommandHandlerDelegate
{
  /**
   * return true if this delegate should process this type of command
   * 
   * @param command
   * @return
   */
  public boolean isInterestedIn(IEfferentCommand command);

  /**
   * return Boolean.TRUE if the command should be accepted for the specified
   * agent. Any other value will be used as the explanation for the rejection of
   * the command
   * 
   * @param command
   * @param agent
   * @param handler
   * @return
   */
  public Object shouldAccept(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler handler);

  /**
   * return Boolean.TRUE if the command should be started. Any other value will
   * be returned as the explanation for the rejection of the command start. If
   * this returns Boolean.TRUE, the {@link EfferentCommandHandler} will next
   * call {@link #start(IEfferentCommand, IAgentObject, EfferentCommandHandler)}
   * and then send a state update for the command noting that the actual state
   * is running.
   * 
   * @param command
   * @param agent
   * @param handler
   * @return
   */
  public Object shouldStart(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler handler);

  /**
   * return Boolean.TRUE if the command should abort (it will already be
   * running). If true, {@link EfferentCommandHandler} will next call
   * {@link #abort(IEfferentCommand, IAgentObject, EfferentCommandHandler)} and
   * send a state update for the command with its actual state as ABORTED
   * 
   * @param command
   * @param agent
   * @param handler
   * @return
   */
  public boolean shouldAbort(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler handler);

  /**
   * return the timing equation used
   * 
   * @param command
   * @param agent
   * @param handler
   * @return
   */
  public ICommandTimingEquation getTimingEquation(IEfferentCommand command,
      IAgentObject agent, EfferentCommandHandler handler);

  /**
   * actually start the processing of the command. The
   * {@link EfferentCommandHandler} is passed along as well so that <b>later</b>
   * {@link EfferentCommandHandler#completed(IEfferentCommand, Object)} may be
   * called. However, it should <b>not</b> be called from within here. The
   * reason is simple, after returning from start,
   * {@link EfferentCommandHandler} will set the state of the command as
   * RUNNING. If you call
   * {@link EfferentCommandHandler#completed(IEfferentCommand, Object)} from
   * within start, the states will be inconsistent. <b>Any</b> state changes
   * must occur <b>after</b> start returns.
   * 
   * @param command
   * @param agent
   * @param parent
   */
  public void start(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler parent);

  /**
   * called once the start is complete
   * 
   * @param command
   * @param agent
   * @param parent
   */
  public void started(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler parent);

  /**
   * called to abort the command
   * 
   * @param command
   * @param agent
   * @param parent
   */
  public void abort(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler parent);

  /**
   * called when the command abort is completed
   * 
   * @param command
   * @param agent
   * @param parent
   */
  public void aborted(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler parent);

  public void rejected(IEfferentCommand command, IAgentObject agent,
      EfferentCommandHandler parent);
}
