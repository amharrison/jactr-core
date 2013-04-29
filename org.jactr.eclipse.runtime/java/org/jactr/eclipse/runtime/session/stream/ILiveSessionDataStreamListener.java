package org.jactr.eclipse.runtime.session.stream;

/*
 * default logging
 */
import java.util.Collection;

public interface ILiveSessionDataStreamListener<T>
{

  public void dataChanged(ILiveSessionDataStream stream, Collection<T> added,
      Collection<T> modified, Collection<T> removed);
}
