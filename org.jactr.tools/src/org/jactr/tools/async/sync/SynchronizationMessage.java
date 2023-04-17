package org.jactr.tools.async.sync;

/*
 * default logging
 */
import java.io.Serializable;

 
import org.slf4j.LoggerFactory;
import org.jactr.tools.async.controller.RemoteInterface;
import org.jactr.tools.async.message.BaseMessage;
import org.jactr.tools.async.shadow.ShadowController;

/**
 * message sent to the {@link ShadowController} by the {@link RemoteInterface}
 * when it is desired to have the two sync up. Unless debugging, the two will
 * run asynchronously, which means the {@link RemoteInterface} may provide data
 * too fast to the {@link ShadowController}, resulting in buffer saturation and
 * other unpleasant things. <br/>
 * <br/>
 * When the {@link ShadowController} receives it, it replies immediately. Mean
 * while, the {@link RemoteInterface} can block the model(s)
 * 
 * @author harrison
 */
public class SynchronizationMessage extends BaseMessage implements Serializable
{
  /**
   * 
   */
  private static final long          serialVersionUID = 8743082997542518386L;

  /**
   * Logger definition
   */
  static private final transient org.slf4j.Logger LOGGER = LoggerFactory
                                                .getLogger(SynchronizationMessage.class);

  /**
   * the id we are replying to, if this is a response.
   */
  private long                       _syncPointId = -1;

  public SynchronizationMessage()
  {
  }

  public SynchronizationMessage(SynchronizationMessage point)
  {
    _syncPointId = point.getID();
  }

  public long inResponseTo()
  {
    return _syncPointId;
  }
}
