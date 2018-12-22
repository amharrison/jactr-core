package org.commonreality.net.handler;

/*
 * default logging
 */
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.session.ISessionInfo;

/**
 * Multiplexer that uses strict class types to route messages.
 * 
 * @author harrison
 */
public class MessageMultiplexer implements IMessageHandler<Object>
{
  static private final Log                        LOGGER    = LogFactory
                                                                .getLog(MessageMultiplexer.class);

  final private Map<Class<?>, IMessageHandler<?>> _handlers = new HashMap<Class<?>, IMessageHandler<?>>();

  final private IMessageHandler<Object>           _fallThrough;

  /**
   * @param fallThrough
   *          called for any unprocessed message
   */
  public MessageMultiplexer(IMessageHandler<Object> fallThrough)
  {
    _fallThrough = fallThrough;
  }

  public void add(Class<?> clazz, IMessageHandler<?> handler)
  {
    _handlers.put(clazz, handler);
  }

  public IMessageHandler<?> remove(Class<?> clazz)
  {
    return _handlers.remove(clazz);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void accept(ISessionInfo<?> t, Object u)
  {
    IMessageHandler<Object> handler = (IMessageHandler<Object>) _handlers.get(u
        .getClass());

    if (handler == null)
    {
      Class<?> messageClass = u.getClass();
      if (LOGGER.isDebugEnabled())
        LOGGER.debug(String.format(
            "Could not find handler for %s, looking for compatible handler",
            messageClass.getName()));

      for (Map.Entry<Class<?>, IMessageHandler<?>> entry : _handlers.entrySet())
        if (entry.getKey().isAssignableFrom(messageClass))
        {
          handler = (IMessageHandler<Object>) entry.getValue();

          if (LOGGER.isDebugEnabled())
            LOGGER.debug(String.format("%s for %s appears to be compatible",
                handler.getClass().getName(), entry.getKey()));

          _handlers.put(messageClass, handler);
          break;
        }
    }

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Got %s routing to %s", u, handler));

    if (handler != null)
      handler.accept(t, u);
    else
      _fallThrough.accept(t, u);
  }

}
