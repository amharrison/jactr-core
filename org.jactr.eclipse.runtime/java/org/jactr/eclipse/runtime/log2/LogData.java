package org.jactr.eclipse.runtime.log2;

/*
 * default logging
 */
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.core.logging.Logger;


public class LogData
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory.getLog(LogData.class);

  private final double                     _time;

  private final Map<String, StringBuilder> _streamContents;

  private final LogSessionDataStream       _logStream;

  private final String                     _timeStream;

  public LogData(double time, LogSessionDataStream stream)
  {
    _time = time;
    _streamContents = new TreeMap<String, StringBuilder>();
    _logStream = stream;

    double timeInSeconds = time;
    double hours = Math.floor(timeInSeconds / 3600);
    timeInSeconds -= hours * 3600;
    double minutes = Math.floor(timeInSeconds / 60);
    timeInSeconds -= minutes * 60;

    _timeStream = String.format("%02.0f:%02.0f:%05.2f", hours, minutes,
        timeInSeconds);
  }

  public LogSessionDataStream getDataStream()
  {
    return _logStream;
  }



  public double getTime()
  {
    return _time;
  }

  public Set<String> getStreamNames()
  {
    return Collections.unmodifiableSet(_streamContents.keySet());
  }

  protected void conflictResolution()
  {
    for (StringBuilder sb : _streamContents.values())
      if (sb.length() > 0) sb.append("---\n");
  }

  protected void append(String stream, String message)
  {
    StringBuilder sb = _streamContents.get(stream);
    if (sb == null)
    {
      sb = new StringBuilder(message);
      _streamContents.put(stream, sb);
    }
    else
      sb.append(message);
  }

  public String get(String streamName)
  {
    if (streamName.equalsIgnoreCase(Logger.Stream.TIME.toString()))
      return _timeStream;

    StringBuilder sb = _streamContents.get(streamName);
    if (sb != null) return sb.toString();
    return "";
  }

  @Override
  public String toString()
  {
    return String.format("[Log (%.2f) : %s]", getTime(),
        _streamContents.keySet());
  }
}
