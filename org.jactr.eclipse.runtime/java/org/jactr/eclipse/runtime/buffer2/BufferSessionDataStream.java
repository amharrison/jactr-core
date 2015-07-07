package org.jactr.eclipse.runtime.buffer2;

/*
 * default logging
 */
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jactr.eclipse.runtime.session.data.ISessionData;
import org.jactr.eclipse.runtime.session.stream.AbstractRollingSessionDataStream;

public class BufferSessionDataStream extends
    AbstractRollingSessionDataStream<BufferData, BufferData> implements
    IBufferSessionDataStream
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(BufferSessionDataStream.class);

  public BufferSessionDataStream(ISessionData sessionData, int windowSize)
  {
    super("buffer", sessionData, windowSize);
  }

  @Override
  protected double getTime(BufferData data)
  {
    return data.getTime();
  }

  @Override
  protected Collection<BufferData> toOutputData(BufferData input)
  {
    return Collections.singleton(input);
  }

}
