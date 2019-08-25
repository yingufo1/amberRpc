package com.amber.communication.nio;

import org.junit.Test;

/**
 * @author yangying
 * @since 2019/8/7.
 */
public class NIOTest {

    @Test
    public void test(){
        int port = 9999;
        NioServer nioServer = new NioServer(port);
        nioServer.startUp();

        NioClient nioClient = new NioClient("127.0.0.1",port);
        nioClient.startUp();
        nioClient.send("hello");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
