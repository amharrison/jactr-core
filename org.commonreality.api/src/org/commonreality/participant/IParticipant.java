/*
 * Created on Feb 23, 2007 Copyright (C) 2001-6, Anthony Harrison anh23@pitt.edu
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
package org.commonreality.participant;

import java.util.Map;
import java.util.concurrent.Future;

import org.commonreality.efferent.IEfferentCommandManager;
import org.commonreality.identifier.IIdentifiable;
import org.commonreality.net.message.IAcknowledgement;
import org.commonreality.net.message.IMessage;
import org.commonreality.notification.INotificationManager;
import org.commonreality.object.manager.IAfferentObjectManager;
import org.commonreality.object.manager.IAgentObjectManager;
import org.commonreality.object.manager.IEfferentObjectManager;
import org.commonreality.object.manager.IRealObjectManager;
import org.commonreality.object.manager.ISensorObjectManager;
import org.commonreality.time.IClock;

/**
 * Someone who is participating in the simulation.<br>
 * <br>
 * execution sequence:<br>
 * <br>
 * configure()<br>
 * initialize()<br>
 * start()<br>
 * suspend() & resume() <br>
 * stop() <br>
 * reset() <br>
 * start()<br>
 * suspend() & resume() <br>
 * stop() <br>
 * shutdown() <br>
 * 
 * @author developer
 */
public interface IParticipant extends IIdentifiable
{

  static public enum State {
    UNKNOWN, CONNECTED, INITIALIZED, STARTED, SUSPENDED, STOPPED
  };

  /**
   * return the current state of the participant, and, if initialized, the state
   * of the simulation
   * 
   * @return
   */
  public State getState();
  
  public boolean stateMatches(State ... states);

  /**
   * wait until the participant's state is one of these.
   * 
   * @param states
   * @return
   * @throws InterruptedException
   */
  public State waitForState(State... states) throws InterruptedException;

  public State waitForState(long waitTime, State... states)
      throws InterruptedException;

  /**
   * configure the participant. this may be called at while UNKNOWN, CONNECTED, INITIALIZED, or
   * STOPPED
   * 
   * @param options
   * @throws Exception
   */
  public void configure(Map<String, String> options) throws Exception;

  /**
   * initialize this participant. this is called after the participant has
   * successfully connected to common reality. If the state isnt connected, this
   * will throw an {@link IllegalStateException}
   */
  public void initialize() throws Exception;

  /**
   * start the participant, if the participant is not initialized, it will throw
   * an {@link IllegalStateException}
   */
  public void start() throws Exception;

  /**
   * suspend the participant if it is started. if not, it will throw an
   * IllegalStateException
   * 
   * @throws Exception
   */
  public void suspend() throws Exception;

  /**
   * resume a suspended participant and return state to started.
   * @throws Exception
   */
  public void resume() throws Exception;

  /**
   * stop the participant if it is started or suspended
   * @throws Exception
   */
  public void stop() throws Exception;

  /**
   * shutdown the participant if it is stopped and disconnect
   * 
   * @throws Exception
   */
  public void shutdown() throws Exception;

  /**
   * shutdown, disconnect, without checking states
   * 
   * @param force
   * @throws Exception
   */
  public void shutdown(boolean force) throws Exception;


  /**
   * reset a stopped participant, reverting to initialized
   * @param clockWillBeReset
   * @throws Exception
   */
  public void reset(boolean clockWillBeReset) throws Exception;

  /**
   * connect
   * @throws Exception
   */
  public void connect() throws Exception;

  /**
   * disconnect, reverting to unknown
   * @throws Exception
   */
  public void disconnect() throws Exception;

  /**
   * force the system to disconnect, regardless of its current state
   * 
   * @param force
   * @throws Exception
   */
  public void disconnect(boolean force) throws Exception;

  /**
   * send a message to common reality. If the message is an {@link IRequest} the
   * {@link Future} will contain the {@link IAcknowledgement} message. If the
   * message is not an {@link IRequest}, the {@link Future} will contain null
   * <br>
   * This is usually used to send object data and commands. The typical pattern
   * is to send the {@link ObjectDataRequest} followed by the
   * {@link ObjectCommandRequest} to commit the new data. While the underlying
   * communications will ensure in order delivery, that delivery is dependent
   * not upon the order of send commands, rather the order in which the messages
   * are transformed to a sendable representation (i.e. stream of bytes). This
   * is not usually an issue unless you send the data request on one thread and
   * the command request on another. Since the encoding of the data will
   * typically take longer, the command may arrive before the data. If you use
   * this model, you should probably wait for the acknowledgment of the data
   * transmission before sending the command.
   * 
   * @param message
   * @return
   */
  public Future<IAcknowledgement> send(IMessage message);
  

  public IClock getClock();

  public ISensorObjectManager getSensorObjectManager();

  public IAfferentObjectManager getAfferentObjectManager();

  public IEfferentObjectManager getEfferentObjectManager();

  public IAgentObjectManager getAgentObjectManager();

  public IRealObjectManager getRealObjectManager();

  /**
   * @return
   */
  public IEfferentCommandManager getEfferentCommandManager();

  public INotificationManager getNotificationManager();

}
