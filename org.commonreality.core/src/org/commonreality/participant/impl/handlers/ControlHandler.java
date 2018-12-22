/*
 * Created on Apr 15, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
 * (jactr.org) This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License,
 * or (at your option) any later version. This library is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.commonreality.participant.impl.handlers;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.message.command.control.ControlAcknowledgement;
import org.commonreality.net.message.command.control.ControlCommand;
import org.commonreality.net.message.command.control.IControlCommand;
import org.commonreality.net.message.command.control.IControlCommand.State;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.participant.IParticipant;
import org.commonreality.participant.impl.AbstractParticipant;

/**
 * @author developer
 */
public class ControlHandler implements IMessageHandler<ControlCommand>
{
  /**
   * logger definition
   */
  static private final Log    LOGGER = LogFactory.getLog(ControlHandler.class);

  private AbstractParticipant _participant;

  public ControlHandler(AbstractParticipant participant)
  {
    _participant = participant;
  }

  /**
   * @param state
   */
  protected void setState(State state, Object extra) throws Exception
  {
    switch (state)
    {
      case INITIALIZE:
        /*
         * can happen if we get the initialize before the connection is
         * complete. this doesn't seem possible, but when running entirely
         * locally, it is possible that this message is received before
         * IParticipant.connect() returns
         */
        if (_participant.stateMatches(IParticipant.State.UNKNOWN))
        {
          if (LOGGER.isErrorEnabled())
            LOGGER
                .error("Initialization message received before connection was completed. Waiting for connection completion before continuing with initialization.");
          _participant.waitForState(IParticipant.State.CONNECTED);
        }

        if (!_participant.stateMatches(IParticipant.State.CONNECTED))
        {
          if (LOGGER.isWarnEnabled())
            LOGGER
                .warn("Already initialized [" + _participant.getState() + "]");
          break;
        }

        _participant.initialize();
        break;
      case START:
        if (_participant.stateMatches(IParticipant.State.STARTED))
        {
          if (LOGGER.isWarnEnabled())
            LOGGER.warn("Already started. Double start issued?");
          break;
        }
        if (!_participant.stateMatches(IParticipant.State.INITIALIZED))
          throw new RuntimeException("Has not been initialized");
        _participant.start();
        break;
      case SUSPEND:
        if (_participant.stateMatches(IParticipant.State.SUSPENDED))
        {
          if (LOGGER.isWarnEnabled())
            LOGGER.warn("Already suspended. Double suspend issued?");
          break;
        }
        if (!_participant.stateMatches(IParticipant.State.STARTED))
          throw new RuntimeException("Is not running");
        _participant.suspend();
        break;
      case RESUME:
        if (!_participant.stateMatches(IParticipant.State.SUSPENDED))
          throw new RuntimeException("Is not suspended");
        _participant.resume();
        break;
      case STOP:
        if (!_participant.stateMatches(IParticipant.State.STARTED,
            IParticipant.State.SUSPENDED))
          throw new RuntimeException("Is not running or suspended");
        _participant.stop();
        break;
      case RESET:
        if (_participant.stateMatches(IParticipant.State.STARTED))
          throw new RuntimeException("Is still running");
        if (!_participant.stateMatches(IParticipant.State.STOPPED,
            IParticipant.State.INITIALIZED))
          throw new RuntimeException("Hasnt been stopped or initialized");
        _participant.reset((Boolean) extra);
        break;
      case SHUTDOWN:
        if (_participant.stateMatches(IParticipant.State.STARTED,
            IParticipant.State.SUSPENDED))
        {
          if (LOGGER.isWarnEnabled())
            LOGGER.warn("Has not been stopped. Stopping");
          _participant.stop();
        }
        /*
         * we don't actually shutdown here, but defer it until after we send the
         * response
         */
        break;
      case CONFIGURE:

        if (_participant.stateMatches(IParticipant.State.STARTED,
            IParticipant.State.SUSPENDED))
          throw new RuntimeException("Is still running");
        _participant.configure((Map<String, String>) extra);
        break;
    }
  }

  // public void handleMessage(IoSession session, IControlCommand command)
  // throws Exception
  // {
  // State state = command.getState();
  // if (LOGGER.isDebugEnabled())
  // LOGGER.debug("Attempting to set " + _participant + " state to " + state);
  //
  // try
  // {
  // setState(state, command.getData());
  //
  // if (LOGGER.isDebugEnabled())
  // LOGGER.debug("State has been set to " + state
  // +", acknowleding "+command.getMessageId());
  //
  // session.write(
  // new ControlAcknowledgement(_participant.getIdentifier(), command
  // .getMessageId(), state)).awaitUninterruptibly();
  //
  // /*
  // * we have to acknowledge shutdown before we actually do it.
  // */
  // if (state == IControlCommand.State.SHUTDOWN) _participant.shutdown();
  // }
  // catch (Exception e)
  // {
  // session.write(
  // new ControlAcknowledgement(_participant.getIdentifier(), command
  // .getMessageId(), e)).awaitUninterruptibly();
  // LOGGER.error("Failed to set state " + state, e);
  // throw e;
  // }
  //
  // }

  @Override
  public void accept(ISessionInfo<?> t, ControlCommand command)
  {
    State state = command.getState();
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Attempting to set " + _participant + " state to " + state);

    try
    {
      setState(state, command.getData());

      if (LOGGER.isDebugEnabled())
        LOGGER.debug("State has been set to " + state + ", acknowleding "
            + command.getMessageId());

      t.write(new ControlAcknowledgement(_participant.getIdentifier(),
 command
          .getMessageId(), _participant.getState()));

      t.flush();
      /*
       * we have to acknowledge shutdown before we actually do it.
       */
      if (state == IControlCommand.State.SHUTDOWN) _participant.shutdown();
    }
    catch (Exception e)
    {
      try
      {
        t.write(new ControlAcknowledgement(_participant.getIdentifier(),
            command.getMessageId(), e));
        t.flush();
      }
      catch (Exception e2)
      {
        LOGGER.error("Failed to handle exception gracefully ", e);
      }

      LOGGER.error("Failed to set state " + state, e);
      throw new RuntimeException(e);
    }

  }
}
