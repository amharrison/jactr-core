package org.jactr.eclipse.runtime.log2;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.AbstractRollingSessionDataStream;
import org.jactr.tools.tracer.transformer.logging.BulkLogEvent;

public class LogSessionDataStream extends
    AbstractRollingSessionDataStream<LogData, LogData> implements
    ILogSessionDataStream
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(LogSessionDataStream.class);

  public LogSessionDataStream(ISessionData sessionData, int windowSize)
  {
    super("log", sessionData, windowSize);
  }

  @Override
  protected double getTime(LogData data)
  {
    return data.getTime();
  }

  @Override
  protected Collection<LogData> toOutputData(LogData input)
  {
    return Collections.singleton(input);
  }

  public void update(BulkLogEvent ble)
  {
    LogData ld = getLastData();
    for (Map.Entry<String, String> entry : ble.getLogData().entrySet())
      ld.append(entry.getKey(), entry.getValue());

    _eventManager.notify(new Object[] { Collections.EMPTY_LIST,
        Collections.singleton(ld), Collections.EMPTY_LIST });
  }
}
