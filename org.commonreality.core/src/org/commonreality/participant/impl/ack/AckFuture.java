package org.commonreality.participant.impl.ack;

/*
 * default logging
 */
import java.util.concurrent.FutureTask;

import org.commonreality.net.message.IAcknowledgement;

/**
 * deprecated in favor of CompletableFuture
 * 
 * @author harrison
 */
@Deprecated
public class AckFuture extends FutureTask<IAcknowledgement>
{
  private final long _requestId;

  private boolean    _hasBeenAcknowledged;

  public AckFuture(long requestId)
  {
    this(requestId, false);
  }

  public AckFuture(long requestId, boolean isDoneAlready)
  {
    super(new Runnable() {
      public void run()
      {
      }
    }, null);
    _requestId = requestId;
    if (isDoneAlready) setAcknowledgement(null);
  }

  public long getRequestMessageId()
  {
    return _requestId;
  }

  public void setAcknowledgement(IAcknowledgement ack)
  {
    // set return value
    set(ack);
  }

  @Override
  public boolean isDone()
  {
    return super.isDone() && _hasBeenAcknowledged;
  }

  @Override
  protected void done()
  {
    super.done();
    _hasBeenAcknowledged = true;
  }

  public boolean hasBeenAcknowledged()
  {
    return _hasBeenAcknowledged;
  }
}