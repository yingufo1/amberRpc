package com.amber.communication.nio;

import java.util.Date;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author yangying
 * @since 2019/8/7.
 */
@Slf4j
/**
 *
 */
public class Server {
    private ServerSocketChannel serverSocketChannel;
    private Integer port;
    private ThreadPoolExecutor threadPoolExecutor;
    private final int defaultThreadPoolCoreSize = 10;
    private final int defaultMaxThreadPoolCoreSize = 10;
    private final BlockingQueue blockingQueue = new LinkedBlockingQueue(1000);

    public static void main(String[] args) {
        Server server = new Server(9999);
        server.startUp();
    }

    public Server(Integer port) {
        if (port < 1024 && port > 0) {
            throw new RuntimeException("error server argument");
        }
        this.port = port;
        this.threadPoolExecutor = new ThreadPoolExecutor(defaultThreadPoolCoreSize, defaultMaxThreadPoolCoreSize, 100, TimeUnit.MILLISECONDS, blockingQueue);
    }

    public Server(Integer port, int threadPoolCoreSize, int maxThreadPoolCoreSize) {
        if (port > 1024 || port < 0) {
            throw new RuntimeException("error server argument");
        }
        this.port = port;
        this.threadPoolExecutor = new ThreadPoolExecutor(threadPoolCoreSize, maxThreadPoolCoreSize, 100, TimeUnit.MILLISECONDS, blockingQueue);
    }

    /**
     *
     */
    public void startUp() {
        threadPoolExecutor.execute(new ServerRunner());
        log.info("server start up");
    }

    /**
     *
     */
    private class ServerRunner implements Runnable {
        private ServerSocketChannel serverSocketChannel;
        private Selector selector;

        public void run() {
            try {
                serverSocketChannel = ServerSocketChannel.open();
                selector = Selector.open();
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.socket().bind(new InetSocketAddress(Server.this.port));
                System.out.println("server start mulitexp select");
                SelectionKey acceptKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                SelectionKey selectionKey = null;
                try {
                    selector.select(1000);
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectedKeys.iterator();
                    if (iterator.hasNext()) {
                        selectionKey = iterator.next();
                        handleEvent(selectionKey);
                        iterator.remove();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (selectionKey != null)
                        selectionKey.cancel();
                }
            }

        }
    }

    private void handleEvent(SelectionKey selectionKey){
        try {
            if (!selectionKey.isValid()) {
                return;
            }
            if (selectionKey.isAcceptable()) {
                handleAccepted(selectionKey);
            }
            if (selectionKey.isReadable()) {
                handleRead(selectionKey);
            }
            if (selectionKey.isWritable()) {
                handWrite(selectionKey,"111");
            }
            if (selectionKey.isConnectable()) {
                log.info("connected ");
            }
        } catch (IOException e) {
            log.error("error:", e);
        } finally {
            if (selectionKey != null)
                selectionKey.cancel();
            if (selectionKey.channel() != null) {
                try {
                    selectionKey.channel().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleAccepted(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        System.out.println(Thread.currentThread().getName() + ":server start listen connect:" + serverSocketChannel.socket().getLocalPort());
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
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

    private void handWrite(SelectionKey selectionKey,String message) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        byte[] messageBytes = message.getBytes();
        ByteBuffer writeBuffer = ByteBuffer.allocate(messageBytes.length);
        writeBuffer.put(messageBytes);
        socketChannel.write(writeBuffer);
    }

    /**
     * 多线程处理SelectKey存在问题，即selectKey如何共享
     */
    private class SocketEventHandler implements Runnable {
        private SelectionKey selectionKey;

        public SocketEventHandler(SelectionKey selectionKey) {
            this.selectionKey = selectionKey;
        }

        public void run() {
            try {
                if (!selectionKey.isValid()) {
                    return;
                }
                if (selectionKey.isAcceptable()) {
                    handleAccepted();
                }
                if (selectionKey.isReadable()) {
                    handleRead();
                }
                if (selectionKey.isWritable()) {
                    handWrite("111");
                }
                if (selectionKey.isConnectable()) {
                    log.info("connected ");
                }
            } catch (IOException e) {
                log.error("error:", e);
            } finally {
                if (selectionKey != null)
                    selectionKey.cancel();
                if (selectionKey.channel() != null) {
                    try {
                        selectionKey.channel().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void handleAccepted() throws IOException {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            System.out.println(Thread.currentThread().getName() + ":server start listen connect:" + serverSocketChannel.socket().getLocalPort());
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
        }

        private void handleRead() throws IOException {
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

        private void handWrite(String message) throws IOException {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            byte[] messageBytes = message.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(messageBytes.length);
            writeBuffer.put(messageBytes);
            socketChannel.write(writeBuffer);
        }
    }
}
