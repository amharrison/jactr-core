package org.jactr.eclipse.runtime.session.data;

/*
 * default logging
 */
import java.util.Set;
import java.util.UUID;

import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;

/**
 * A source for model session data. This is returned (potentially) by all
 * sessions. The session data can be backed by live network or rerecorded data
 * 
 * @author harrison
 */
public interface ISessionData
{

  /**
   * unique id for the session
   * 
   * @return
   */
  public UUID getSessionId();

  public ISession getSession();

  public String getModelName();

  /**
   * open the session data. Possibly opening files or connections
   * 
   * @throws Exception
   */
  public void open() throws Exception;

  public boolean isOpen();

  /**
   * 
   */
  public void close() throws Exception;

  /**
   * delete the data if applicable
   * 
   * @throws Exception
   */
  public void delete() throws Exception;

  /**
   * returns true if the session data is coming across live.
   * 
   * @return
   */
  public boolean isLive();

  /**
   * @return Double.NaN if the data is currently live
   */
  public double getEndTime();

  public double getStartTime();

  /**
   * return the list of named data streams that are available
   * 
   * @return
   */
  public Set<String> getAvailableStreams();

  public ISessionDataStream getDataStream(String streamName);
}
