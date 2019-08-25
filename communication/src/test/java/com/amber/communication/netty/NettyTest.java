package com.amber.communication.netty;

import com.amber.communication.Server;
import org.junit.Assert;
import org.junit.Test;

public class NettyTest {
    @Test
    public void test(){
        int port = 9999;
        String localHost = "127.0.0.1";
        HelloWorldNettyServer helloWorldNettyServer = HelloWorldNettyServer.create(port);
        helloWorldNettyServer.startUp();

        HelloWorldNettyClient helloWorldNettyClient = new HelloWorldNettyClient();
        helloWorldNettyClient.connect(localHost,port);
    }
}
