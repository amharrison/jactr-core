package org.jactr.eclipse.runtime.session.stream;

/*
 * default logging
 */
import java.util.Collection;
import java.util.UUID;

import org.jactr.eclipse.runtime.session.data.ISessionData;

public interface ISessionDataStream<T>
{

  public UUID getSessionId();

  public ISessionData getSessionData();

  public String getStreamName();

  /**
   * return the number of records between start and end times.
   * 
   * @param startTime
   * @param endTime
   * @return
   */
  public long getAmountOfDataAvailable(double startTime, double endTime);

  /**
   * return all the available data for this time range, inclusive
   * 
   * @param startTime
   * @param endTime
   * @return
   */
  public Collection<T> getData(double startTime, double endTime,
      Collection<T> container);

  /**
   * get the lastest data that is less than or equal to endTime
   * 
   * @param endTime
   * @param container
   * @return
   */
  public Collection<T> getLatestData(double endTime, Collection<T> container);

  /**
   * earliest available time
   * 
   * @return
   */
  public double getStartTime();

  /**
   * latested availabel time
   * 
   * @return
   */
  public double getEndTime();

  public void clear();
}
