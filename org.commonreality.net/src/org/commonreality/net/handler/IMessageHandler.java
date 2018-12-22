package org.commonreality.net.handler;

/*
 * default logging
 */
import java.util.function.BiConsumer;

import org.commonreality.net.session.ISessionInfo;

@FunctionalInterface
public interface IMessageHandler<M> extends BiConsumer<ISessionInfo<?>, M>
{

}
