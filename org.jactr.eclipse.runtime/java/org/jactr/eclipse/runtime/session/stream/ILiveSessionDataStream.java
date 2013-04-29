package org.jactr.eclipse.runtime.session.stream;

import java.util.concurrent.Executor;

/*
 * default logging
 */

public interface ILiveSessionDataStream<T> extends ISessionDataStream<T>
{

  public void addListener(ILiveSessionDataStreamListener<T> listener,
      Executor executor);

  public void removeListener(ILiveSessionDataStreamListener<T> listener);
}
