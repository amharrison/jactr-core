package org.jactr.eclipse.runtime.session.stream;

/*
 * default logging
 */
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.data.ISessionData;

public abstract class AbstractSessionDataStream<T> implements
    ISessionDataStream<T>
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(AbstractSessionDataStream.class);

  private final ISessionData         _sessionData;

  private final String               _streamName;

  public AbstractSessionDataStream(String streamName, ISessionData sessionData)
  {
    _sessionData = sessionData;
    _streamName = streamName;
  }

  public UUID getSessionId()
  {
    return _sessionData.getSessionId();
  }

  public ISessionData getSessionData()
  {
    return _sessionData;
  }

  public String getStreamName()
  {
    return _streamName;
  }
}
