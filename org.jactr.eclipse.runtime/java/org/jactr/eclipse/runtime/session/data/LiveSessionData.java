package org.jactr.eclipse.runtime.session.data;

/*
 * default logging
 */
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.ISession;
import org.jactr.eclipse.runtime.session.impl.AbstractSession;
import org.jactr.eclipse.runtime.session.stream.ISessionDataStream;

public class LiveSessionData implements ISessionData
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(LiveSessionData.class);

  private final ISession                        _session;

  private final Map<String, ISessionDataStream> _dataStreams;

  private double                                _startTime = Double.MAX_VALUE;

  private double                                _endTime   = Double.MIN_VALUE;

  private final String                          _modelName;

  private boolean                               _isOpen    = true;

  public LiveSessionData(ISession session, String modelName)
  {
    _modelName = modelName;
    _session = session;
    _dataStreams = new TreeMap<String, ISessionDataStream>();
  }

  public ISession getSession()
  {
    return _session;
  }

  public UUID getSessionId()
  {
    return _session.getSessionId();
  }

  public String getModelName()
  {
    return _modelName;
  }

  public void open() throws Exception
  {
    // noop, already open

  }

  public void close() throws Exception
  {
    // noop, already open
    _isOpen = false;
  }

  public void delete() throws Exception
  {
    // clears all data
    for (ISessionDataStream stream : _dataStreams.values())
      stream.clear();
    _dataStreams.clear();
    _isOpen = false;
  }

  public boolean isOpen()
  {
    return _isOpen;
  }

  public boolean isLive()
  {
    return _session.getController().isRunning();
  }

  protected void timeUpdate(double simulationTime)
  {
    if (simulationTime > _startTime) _startTime = simulationTime;
    if (simulationTime < _endTime) _endTime = simulationTime;
  }

  public double getEndTime()
  {
    return _endTime;
  }

  public double getStartTime()
  {
    return _startTime;
  }

  public Set<String> getAvailableStreams()
  {
    return Collections.unmodifiableSet(_dataStreams.keySet());
  }

  public ISessionDataStream getDataStream(String streamName)
  {
    return _dataStreams.get(streamName);
  }

  public void setStreamData(String streamName, ISessionDataStream dataStream)
  {
    _dataStreams.put(streamName, dataStream);
    // fire the event.
    if (_session instanceof AbstractSession)
      ((AbstractSession) _session).newDataStreamAdded(dataStream);
  }
}
