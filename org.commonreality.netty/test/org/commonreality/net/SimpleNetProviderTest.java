package org.commonreality.net;

/*
 * default logging
 */
import java.net.SocketAddress;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.commonreality.net.handler.IMessageHandler;
import org.commonreality.net.impl.DefaultSessionListener;
import org.commonreality.net.protocol.IProtocolConfiguration;
import org.commonreality.net.provider.INetworkingProvider;
import org.commonreality.net.service.IClientService;
import org.commonreality.net.service.IServerService;
import org.commonreality.net.session.ISessionInfo;
import org.commonreality.net.session.ISessionListener;
import org.commonreality.net.transport.ITransportProvider;
import org.junit.Assert;

public class SimpleNetProviderTest
{
  /**
   * Logger definition
   */
  static private final transient Log LOGGER = LogFactory
                                                .getLog(SimpleNetProviderTest.class);




  public void testNIOSerializer(String providerName) throws Exception
  {
    LOGGER
        .warn("Not strictly a proper test. This will not fail short of an exception");

    INetworkingProvider provider = INetworkingProvider
        .getProvider(providerName);
    IServerService server = provider.newServer();
    IClientService client = provider.newClient();
    ITransportProvider transport = provider
        .getTransport(INetworkingProvider.NIO_TRANSPORT);
    IProtocolConfiguration protocol = provider
        .getProtocol(INetworkingProvider.SERIALIZED_PROTOCOL);


    SocketAddress address = transport.createAddress("localhost", "0");

    AtomicInteger messageCount = new AtomicInteger(0);

    IMessageHandler<String> handler = (s, m) -> {
      LOGGER.debug(String.format("got %s", m));
      messageCount.incrementAndGet();
    };

    ISessionListener serverListener = new DefaultSessionListener() {
      @Override
      public void opened(ISessionInfo<?> session)
      {
        super.opened(session);
        try
        {
          session.write("test from server");
          session.flush();
        }
        catch (Exception e)
        {
          // TODO Auto-generated catch block
          LOGGER.error(".opened threw Exception : ", e);
        }
      }
    };

    ISessionListener clientListener = new DefaultSessionListener() {
      @Override
      public void opened(ISessionInfo<?> session)
      {
        super.opened(session);
        try
        {
          session.write("test from client");
          session.write("test from client2");
          session.flush();
        }
        catch (Exception e)
        {
          // TODO Auto-generated catch block
          LOGGER.error(".opened threw Exception : ", e);
        }
      }
    };


    server.configure(transport, protocol,
        Collections.singletonMap(String.class, handler), serverListener,
        Executors.defaultThreadFactory());

    client.configure(transport, protocol,
        Collections.singletonMap(String.class, handler), clientListener,
        Executors.defaultThreadFactory());

    SocketAddress hostingAddress = server.start(address); // where we are
                                                          // actually listening
                                                          // too

    if (LOGGER.isDebugEnabled())
      LOGGER.debug(String.format("Server listening on %s", hostingAddress));

    client.start(hostingAddress);

    /*
     * the listeners will do the sending and listening..
     */
    Thread.sleep(1000);

    Assert.assertEquals(messageCount.get(), 3);
  }
}
