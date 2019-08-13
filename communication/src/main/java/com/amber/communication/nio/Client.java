package com.amber.communication.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author yangying
 * @since 2019/8/10.
 */
@Slf4j
public class Client {
    private int port;
    private String hostIp;
    private BlockingQueue<String> messageBlockingQueue = new LinkedBlockingQueue<String>();

    public Client(String hostIp, int port) {
        this.hostIp = hostIp;
        this.port = port;
    }

    public void startUp() {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            log.info("client start up");
            new Thread(new SendRunner(socketChannel)).start();
            log.info("client start up finished");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        try {
            messageBlockingQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class SendRunner implements Runnable {
        private SocketChannel socketChannel;

        public SendRunner(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        public void run() {
            System.out.println(Thread.currentThread().getName() + ":start up client thread");
            try {
                socketChannel.configureBlocking(false);
                String message = Client.this.messageBlockingQueue.take();

                socketChannel.connect(new InetSocketAddress(Client.this.hostIp, Client.this.port));
                if (!socketChannel.finishConnect()) {
                    System.out.println(Thread.currentThread().getName() + "server not ready");
                    return;
                }
                System.out.println(Thread.currentThread().getName() + ":socket channel connect success");
                ByteBuffer byteBuffer = ByteBuffer.allocate(1000);
                byteBuffer.put(message.getBytes());
                byteBuffer.flip();
                System.out.println(Thread.currentThread().getName() + ":send a message:" + message);
                while (byteBuffer.hasRemaining()) {
                    socketChannel.write(byteBuffer);
                }
                OutputStream outputStream = new ByteArrayOutputStream();
                byte[] receiveByte = null;
                //byteBuffer.flip();
                while (socketChannel.read(byteBuffer) > 0) {
                    receiveByte = new byte[byteBuffer.limit()];
                    receiveByte = byteBuffer.array();
                }
                if (receiveByte != null)
                    System.out.println(Thread.currentThread().getName() + ":receive a message:" + new String(receiveByte));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (socketChannel.isConnected()) {
                    try {
                        socketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 9999);
        client.startUp();
        client.send("hello");
    }
}
