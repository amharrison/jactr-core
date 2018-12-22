package org.commonreality.net.netty;

import org.commonreality.net.SimpleNetProviderTest;
import org.commonreality.netty.NettyNetworkingProvider;
import org.junit.Test;

public class NettyNetProviderTest extends SimpleNetProviderTest 
{
  
  @Test
  public void testNettyNIO() throws Exception
  {
    testNIOSerializer(NettyNetworkingProvider.class.getName());
  }
 
}
