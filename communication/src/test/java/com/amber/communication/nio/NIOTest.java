package com.amber.communication.nio;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author yangying
 * @since 2019/8/7.
 */
public class NIOTest {

    @Test
    public void test(){
        int port = 9999;
        Server server = new Server(port);
        server.startUp();

        Client client = new Client("127.0.0.1",port);
        client.startUp();
        client.send("hello");
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
