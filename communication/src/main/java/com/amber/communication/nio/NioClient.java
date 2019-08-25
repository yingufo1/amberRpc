package com.amber.communication.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author yangying
 * @since 2019/8/10.
 */
@Slf4j
public class NioClient {
    private int port;
    private String hostIp;
    private BlockingQueue<String> messageBlockingQueue = new LinkedBlockingQueue<String>();

    public NioClient(String hostIp, int port) {
        this.hostIp = hostIp;
        this.port = port;
    }

    public void startUp() {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            Selector selector = Selector.open();
            log.info("client start up");
            new Thread(new SendRunner(socketChannel, selector)).start();
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
        private Selector selector;

        public SendRunner(SocketChannel socketChannel, Selector selector) {
            this.socketChannel = socketChannel;
            this.selector = selector;
        }

        public void run() {
            System.out.println(Thread.currentThread().getName() + ":start up client thread");
            SelectionKey selectionKey = null;
            try {
                doConnect();
                sendMsg();
                socketChannel.register(selector, SelectionKey.OP_READ);
                try {
                    selector.select(30000);
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();
                    if (iterator.hasNext()) {
                        selectionKey = iterator.next();
                        handleRead(selectionKey);
                        iterator.remove();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (selectionKey != null)
                        selectionKey.cancel();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if(selectionKey!=null)
                    selectionKey.cancel();
                if (socketChannel.isConnected()) {
                    try {
                        socketChannel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * connect to a remote server
         *
         * @throws InterruptedException
         */
        private void doConnect() throws IOException {
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(NioClient.this.hostIp, NioClient.this.port));
            if (!socketChannel.finishConnect()) {
                System.out.println(Thread.currentThread().getName() + "server not ready");
                throw new RuntimeException("server not ready");
            }
            System.out.println(Thread.currentThread().getName() + ":socket channel connect success");
        }

        private void sendMsg() throws InterruptedException, IOException {
            String message = NioClient.this.messageBlockingQueue.take();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            byteBuffer.put(message.getBytes());
            byteBuffer.flip();
            System.out.println(Thread.currentThread().getName() + ":send a message:" + message);
            while (byteBuffer.hasRemaining()) {
                socketChannel.write(byteBuffer);
            }
        }

        private void handleRead(SelectionKey selectionKey) throws IOException {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            int readBytes = socketChannel.read(readBuffer);
            if (readBytes > 0) {
                readBuffer.flip();
                byte[] bytes = readBuffer.array();
                System.out.println(Thread.currentThread().getName() + ":read msg:" + new String(bytes));
            }
            ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
            writeBuffer.put(" world".getBytes());
            socketChannel.write(writeBuffer);
        }
    }

    public static void main(String[] args) {
        NioClient nioClient = new NioClient("127.0.0.1", 9999);
        nioClient.startUp();
        nioClient.send("hello");
    }
}
