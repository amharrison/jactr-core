package org.commonreality.participant.impl.ack;

/*
 * default logging
 */
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.Future;

import org.commonreality.net.message.IAcknowledgement;

/**
 * deprecated in favor of CompletableFuture
 * 
 * @author harrison
 */
@Deprecated
public class AckFutureReference implements Comparable<AckFutureReference>
{
  private final long           _id;

  private Reference<AckFuture> _reference;

  public AckFutureReference(long id, AckFuture future, boolean useWeak)
  {
    _id = id;
    if (useWeak)
      _reference = new WeakReference<AckFuture>(future);
    else
      _reference = new SoftReference<AckFuture>(future);
  }

  public Future<IAcknowledgement> getFuture()
  {
    if (_reference != null) return _reference.get();
    return null;
  }

  public long getRequestId()
  {
    return _id;
  }

  /**
   * converts the soft reference to a weak reference
   */
  public void weaken()
  {
    AckFuture future = (AckFuture) getFuture();
    if (future != null) _reference = new WeakReference<AckFuture>(future);
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (_id ^ _id >>> 32);
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AckFutureReference other = (AckFutureReference) obj;
    if (_id != other._id) return false;
    return true;
  }

  public int compareTo(AckFutureReference o)
  {
    if (o == this) return 0;
    if (o._id == _id) return 0;
    if (o._id < _id) return 1;
    return -1;
  }

}