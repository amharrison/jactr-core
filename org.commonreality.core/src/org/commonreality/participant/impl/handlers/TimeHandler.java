/*
 * Created on May 10, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.message.command.time.TimeCommand;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.participant.IParticipant;
import org.commonreality.time.IClock;
import org.commonreality.time.impl.NetworkedClock.NetworkedAuthoritativeClock;

/**
 * @author developer
 */
public class TimeHandler implements IMessageHandler<TimeCommand>
{
  /**
   * logger definition
   */
  static private final Log LOGGER = LogFactory.getLog(TimeHandler.class);

  private IParticipant     _participant;

  public TimeHandler(IParticipant participant)
  {
    _participant = participant;
  }

  // public void handleMessage(IoSession arg0, ITimeCommand time) throws
  // Exception
  // {
  //
  // IClock clock = _participant.getClock();
  // NetworkedAuthoritativeClock auth = (NetworkedAuthoritativeClock) clock
  // .getAuthority().get();
  //
  // if (LOGGER.isDebugEnabled())
  // LOGGER.debug("Got a time command for " + time.getTime() + " setting");
  //
  // /*
  // * time arrived is global.
  // */
  // AbstractParticipant.getPeriodicExecutor().execute(
  // () -> auth.timeChangeReceived(time.getTime()));
  //
  // // if (clock instanceof INetworkedClock)
  // // {
  // // if (LOGGER.isDebugEnabled())
  // // LOGGER.debug("Got a time command for " + time.getTime() + " setting");
  // // ((INetworkedClock) clock).setCurrentTimeCommand(time);
  // // }
  //
  // }

  @Override
  public void accept(ISessionInfo<?> t, TimeCommand time)
  {
    IClock clock = _participant.getClock();
    NetworkedAuthoritativeClock auth = (NetworkedAuthoritativeClock) clock
        .getAuthority().get();

    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Got a time command for " + time.getTime() + " setting");

    /*
     * time arrived is global.
     */
    /*
     * we can pull this off of the periodic once we've confirmed Netty's
     * behavior
     */
    // AbstractParticipant.getPeriodicExecutor().execute(
    // () ->
    auth.timeChangeReceived(time.getTime());
    // );

  }

}
